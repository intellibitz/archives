"""
Dynamic Model Router & Cost Calculator for the AI Agent Orchestrator.

Invokes models via LiteLLM directly in-process or via the local LiteLLM Proxy,
handling caching telemetry, provider-specific base URLs, and exact dollar cost auditing.
"""

import os
import time
from typing import List, Dict, Any, Optional
from dataclasses import dataclass, field
import litellm

from orchestrator.config import (
    PRICING_TABLE,
    GEMINI_API_KEY,
    ANTHROPIC_API_KEY,
    OPENAI_API_KEY,
    DEEPSEEK_API_KEY,
    ZHIPU_API_KEY,
    OPENROUTER_API_KEY,
    LITELLM_API_BASE,
    LITELLM_MASTER_KEY,
    get_model_for_role,
)

# Silence litellm logs unless requested
litellm.suppress_debug_info = True


@dataclass
class ModelUsage:
    model: str
    prompt_tokens: int = 0
    completion_tokens: int = 0
    cached_tokens: int = 0
    total_tokens: int = 0
    latency_sec: float = 0.0
    estimated_cost_usd: float = 0.0


@dataclass
class AgentResponse:
    content: str
    model: str
    role: str
    usage: ModelUsage
    raw: Optional[Any] = None


class ModelRouter:
    """Universal gateway to call models with cost tracking and fallback logic."""

    def __init__(self, use_proxy: bool = False, dry_run: bool = False):
        self.use_proxy = use_proxy
        self.dry_run = dry_run
        self.session_usages: List[ModelUsage] = []

    def calculate_cost(
        self,
        model_name: str,
        prompt_tokens: int,
        completion_tokens: int,
        cached_tokens: int = 0,
    ) -> float:
        """Calculates precise cost in USD based on per-1M token rates."""
        rates = PRICING_TABLE.get(model_name)
        if not rates:
            # Fallback to approximate rates
            if "deepseek" in model_name:
                rates = {"input": 0.14, "cached_input": 0.003, "output": 0.60}
            elif "gemini" in model_name:
                rates = {"input": 0.10, "cached_input": 0.025, "output": 0.40}
            elif "claude" in model_name:
                rates = {"input": 3.00, "cached_input": 0.30, "output": 15.00}
            elif "glm" in model_name:
                rates = {"input": 0.14, "cached_input": 0.07, "output": 0.14}
            else:
                rates = {"input": 1.00, "cached_input": 0.50, "output": 2.00}

        non_cached_input = max(0, prompt_tokens - cached_tokens)
        input_cost = (non_cached_input / 1_000_000.0) * rates.get("input", 0.0)
        cached_cost = (cached_tokens / 1_000_000.0) * rates.get("cached_input", 0.0)
        output_cost = (completion_tokens / 1_000_000.0) * rates.get("output", 0.0)

        return input_cost + cached_cost + output_cost

    def _prepare_provider_kwargs(self, model: str) -> Dict[str, Any]:
        """Injects explicit base URLs and keys for BYOK providers."""
        kwargs: Dict[str, Any] = {}

        if "deepseek" in model:
            kwargs["api_key"] = DEEPSEEK_API_KEY
            kwargs["api_base"] = "https://api.deepseek.com/v1"
        elif "glm" in model:
            kwargs["api_key"] = ZHIPU_API_KEY
            kwargs["api_base"] = "https://open.bigmodel.cn/api/paas/v4"
        elif "gemini" in model:
            kwargs["api_key"] = GEMINI_API_KEY
        elif "anthropic" in model or "claude" in model:
            kwargs["api_key"] = ANTHROPIC_API_KEY
        elif "openai" in model or "o1" in model or "o3" in model:
            kwargs["api_key"] = OPENAI_API_KEY
        elif "openrouter" in model:
            kwargs["api_key"] = OPENROUTER_API_KEY

        return kwargs

    def call(
        self,
        role: str,
        messages: List[Dict[str, str]],
        temperature: float = 0.2,
        max_tokens: Optional[int] = None,
        override_model: Optional[str] = None,
    ) -> AgentResponse:
        """
        Executes a call for an assigned role, applying fallbacks and recording telemetry.
        """
        model = override_model or get_model_for_role(role)
        start_time = time.time()

        if self.dry_run:
            duration = 0.05
            content = f"[DRY-RUN MOCK RESPONSE from {model} for role: {role}]"
            usage = ModelUsage(
                model=model,
                prompt_tokens=150,
                completion_tokens=50,
                cached_tokens=0,
                total_tokens=200,
                latency_sec=duration,
                estimated_cost_usd=0.00002,
            )
            self.session_usages.append(usage)
            return AgentResponse(content=content, model=model, role=role, usage=usage)

        kwargs = self._prepare_provider_kwargs(model)
        if max_tokens:
            kwargs["max_tokens"] = max_tokens

        # If temperature is not supported by reasoning models (like o1/o3-mini or deepseek-reasoner)
        if "reasoner" in model or "o1" in model or "o3" in model:
            # Let litellm handle default or drop temperature
            pass
        else:
            kwargs["temperature"] = temperature

        try:
            if self.use_proxy:
                # Route through local LiteLLM proxy
                resp = litellm.completion(
                    model=model,
                    messages=messages,
                    api_base=LITELLM_API_BASE,
                    api_key=LITELLM_MASTER_KEY,
                    **kwargs,
                )
            else:
                # Direct in-process invocation
                resp = litellm.completion(
                    model=model,
                    messages=messages,
                    **kwargs,
                )

            latency = time.time() - start_time
            content = resp.choices[0].message.content or ""

            # Extract token counts
            raw_usage = getattr(resp, "usage", None)
            prompt_tokens = getattr(raw_usage, "prompt_tokens", 0) if raw_usage else len(str(messages)) // 4
            completion_tokens = getattr(raw_usage, "completion_tokens", 0) if raw_usage else len(content) // 4
            cached_tokens = 0

            # Inspect prompt caching telemetry if provided
            if raw_usage and hasattr(raw_usage, "prompt_tokens_details"):
                details = getattr(raw_usage, "prompt_tokens_details", None)
                if details and hasattr(details, "cached_tokens"):
                    cached_tokens = getattr(details, "cached_tokens", 0) or 0

            cost = self.calculate_cost(model, prompt_tokens, completion_tokens, cached_tokens)
            usage = ModelUsage(
                model=model,
                prompt_tokens=prompt_tokens,
                completion_tokens=completion_tokens,
                cached_tokens=cached_tokens,
                total_tokens=prompt_tokens + completion_tokens,
                latency_sec=latency,
                estimated_cost_usd=cost,
            )
            self.session_usages.append(usage)

            return AgentResponse(content=content, model=model, role=role, usage=usage, raw=resp)

        except Exception as e:
            # If the preferred model failed, attempt graceful fallback to Gemini or DeepSeek
            fallback_model = "gemini/gemini-2.0-flash" if model != "gemini/gemini-2.0-flash" else "deepseek/deepseek-chat"
            if self.dry_run or not (GEMINI_API_KEY or DEEPSEEK_API_KEY or OPENAI_API_KEY):
                raise RuntimeError(f"Model {model} failed: {e}")

            # Try fallback
            try:
                fb_kwargs = self._prepare_provider_kwargs(fallback_model)
                resp = litellm.completion(
                    model=fallback_model,
                    messages=messages,
                    **fb_kwargs,
                )
                latency = time.time() - start_time
                content = resp.choices[0].message.content or ""
                usage = ModelUsage(
                    model=f"{fallback_model} (fallback from {model})",
                    prompt_tokens=100,
                    completion_tokens=100,
                    latency_sec=latency,
                    estimated_cost_usd=0.0001,
                )
                self.session_usages.append(usage)
                return AgentResponse(content=content, model=fallback_model, role=role, usage=usage)
            except Exception as fb_err:
                raise RuntimeError(f"Primary model ({model}) failed [{e}] and fallback ({fallback_model}) also failed [{fb_err}]")

    def get_session_totals(self) -> Dict[str, Any]:
        """Returns aggregated telemetry for the current run."""
        total_tokens = sum(u.total_tokens for u in self.session_usages)
        total_cost = sum(u.estimated_cost_usd for u in self.session_usages)
        total_latency = sum(u.latency_sec for u in self.session_usages)
        calls_by_model: Dict[str, int] = {}
        for u in self.session_usages:
            calls_by_model[u.model] = calls_by_model.get(u.model, 0) + 1

        return {
            "total_calls": len(self.session_usages),
            "total_tokens": total_tokens,
            "total_cost_usd": total_cost,
            "total_latency_sec": total_latency,
            "model_breakdown": calls_by_model,
        }
