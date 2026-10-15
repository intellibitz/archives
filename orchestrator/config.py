"""
Configuration & Model Economics Engine for the AI Agent Orchestrator.

Manages credentials, active provider detection, fallback cascades,
and per-million-token cost tracking across subscriptions & pay-as-you-go tiers.
"""

import os
from pathlib import Path
from typing import Dict, Any, Optional
from dotenv import load_dotenv

# Automatically load .env if present
WORKSPACE_ROOT = Path(__file__).resolve().parent.parent
load_dotenv(WORKSPACE_ROOT / ".env")
load_dotenv(WORKSPACE_ROOT / ".env.orchestrator")

# ------------------------------------------------------------------------------
# 1. Provider Credentials & Status Detection
# ------------------------------------------------------------------------------
GEMINI_API_KEY = os.getenv("GEMINI_API_KEY", "")
ANTHROPIC_API_KEY = os.getenv("ANTHROPIC_API_KEY", "")
OPENAI_API_KEY = os.getenv("OPENAI_API_KEY", "")
DEEPSEEK_API_KEY = os.getenv("DEEPSEEK_API_KEY", "")
ZHIPU_API_KEY = os.getenv("ZHIPU_API_KEY", "")
OPENROUTER_API_KEY = os.getenv("OPENROUTER_API_KEY", "")

# LiteLLM Proxy Host/Port
LITELLM_HOST = os.getenv("LITELLM_PROXY_HOST", "127.0.0.1")
LITELLM_PORT = int(os.getenv("LITELLM_PROXY_PORT", "4000"))
LITELLM_API_BASE = f"http://{LITELLM_HOST}:{LITELLM_PORT}/v1"
LITELLM_MASTER_KEY = os.getenv("LITELLM_MASTER_KEY", "orchestrator-local-gateway")

# ------------------------------------------------------------------------------
# 2. Token Pricing Matrix (Cost per 1M tokens in USD)
# ------------------------------------------------------------------------------
PRICING_TABLE: Dict[str, Dict[str, float]] = {
    # Gemini (Antigravity subscription or free/cheap tier)
    "gemini/gemini-3.8-flash": {"input": 0.10, "cached_input": 0.025, "output": 0.40},
    "gemini/gemini-flash-latest": {"input": 0.10, "cached_input": 0.025, "output": 0.40},
    "gemini/gemini-pro-latest": {"input": 1.25, "cached_input": 0.3125, "output": 5.00},
    "gemini/gemini-2.0-flash": {"input": 0.10, "cached_input": 0.025, "output": 0.40},
    "gemini/gemini-1.5-pro": {"input": 1.25, "cached_input": 0.3125, "output": 5.00},
    
    # Anthropic Claude (Subscription / API)
    "anthropic/claude-3-5-sonnet-20241022": {"input": 3.00, "cached_input": 0.30, "output": 15.00},
    "anthropic/claude-3-7-sonnet-20250219": {"input": 3.00, "cached_input": 0.30, "output": 15.00},
    "anthropic/claude-3-5-haiku-20241022": {"input": 0.80, "cached_input": 0.08, "output": 4.00},
    
    # OpenAI / Codex (Subscription / API)
    "openai/o1": {"input": 15.00, "cached_input": 7.50, "output": 60.00},
    "openai/o3-mini": {"input": 1.10, "cached_input": 0.55, "output": 4.40},
    "openai/gpt-4o": {"input": 2.50, "cached_input": 1.25, "output": 10.00},
    "openai/gpt-4o-mini": {"input": 0.15, "cached_input": 0.075, "output": 0.60},
    
    # DeepSeek (Pay-as-you-go micro tokens)
    "deepseek/deepseek-chat": {"input": 0.14, "cached_input": 0.003, "output": 0.60},
    "deepseek/deepseek-reasoner": {"input": 0.55, "cached_input": 0.14, "output": 2.19},
    
    # Zhipu GLM (Pay-as-you-go sub-cent)
    "openai/glm-4-flash": {"input": 0.00, "cached_input": 0.00, "output": 0.00}, # Free Tier
    "openai/glm-4-air": {"input": 0.14, "cached_input": 0.07, "output": 0.14},
    "openai/glm-4": {"input": 1.40, "cached_input": 0.70, "output": 1.40},
}

# ------------------------------------------------------------------------------
# 3. Dynamic Model Cascades & Intelligent Fallbacks
# ------------------------------------------------------------------------------
def get_model_for_role(role: str) -> str:
    """
    Resolves the optimal model for an agent role based on available keys.
    Falls back gracefully to alternative models when specific keys are absent.
    """
    has_anthropic = bool(ANTHROPIC_API_KEY)
    has_openai = bool(OPENAI_API_KEY)
    has_gemini = bool(GEMINI_API_KEY)
    has_deepseek = bool(DEEPSEEK_API_KEY)
    has_zhipu = bool(ZHIPU_API_KEY)

    if role == "architect":
        # Preferred: Claude 3.5/3.7 Sonnet -> OpenAI GPT-4o -> Gemini 2.0 Flash
        if has_anthropic:
            return "anthropic/claude-3-5-sonnet-20241022"
        if has_openai:
            return "openai/gpt-4o"
        if has_gemini:
            return "gemini/gemini-3.8-flash"
        return "deepseek/deepseek-chat"

    elif role == "librarian":
        # Preferred: Gemini 2.0 Flash (2M token window) -> Claude -> OpenAI
        if has_gemini:
            return "gemini/gemini-3.8-flash"
        if has_anthropic:
            return "anthropic/claude-3-5-sonnet-20241022"
        if has_openai:
            return "openai/gpt-4o"
        return "deepseek/deepseek-chat"

    elif role == "reasoner":
        # Preferred: DeepSeek-R1 (cheap elite reasoning) -> OpenAI o3-mini/o1 -> Claude
        if has_deepseek:
            return "deepseek/deepseek-reasoner"
        if has_openai:
            return "openai/o3-mini"
        if has_anthropic:
            return "anthropic/claude-3-5-sonnet-20241022"
        return "gemini/gemini-2.0-flash"

    elif role == "coder":
        # Preferred: DeepSeek-V3 (cheap & fast) or Claude 3.5 Sonnet -> GPT-4o
        if has_deepseek:
            return "deepseek/deepseek-chat"
        if has_anthropic:
            return "anthropic/claude-3-5-sonnet-20241022"
        if has_openai:
            return "openai/gpt-4o"
        return "gemini/gemini-2.0-flash"

    elif role == "swarm":
        # Preferred: GLM-4-Flash (Free/sub-cent) -> DeepSeek-V3 -> Gemini Flash
        if has_zhipu:
            return "openai/glm-4-flash"
        if has_deepseek:
            return "deepseek/deepseek-chat"
        if has_gemini:
            return "gemini/gemini-3.8-flash"
        if has_openai:
            return "openai/gpt-4o-mini"
        return "anthropic/claude-3-5-haiku-20241022"

    elif role == "reviewer":
        # Preferred: Claude 3.5 Sonnet -> Gemini 2.0 Flash -> GPT-4o
        if has_anthropic:
            return "anthropic/claude-3-5-sonnet-20241022"
        if has_gemini:
            return "gemini/gemini-3.8-flash"
        if has_openai:
            return "openai/gpt-4o"
        return "deepseek/deepseek-chat"

    return "gemini/gemini-2.0-flash"


def get_active_providers_summary() -> Dict[str, Any]:
    """Returns a clean diagnostic report of active and missing providers."""
    return {
        "Gemini (Antigravity)": {"status": bool(GEMINI_API_KEY), "role": "Librarian (Large Context Ingestion)"},
        "Anthropic (Claude)": {"status": bool(ANTHROPIC_API_KEY), "role": "Architect & Reviewer (Lead Planner)"},
        "OpenAI (Codex)": {"status": bool(OPENAI_API_KEY), "role": "Algorithmic Reasoning Specialist"},
        "DeepSeek": {"status": bool(DEEPSEEK_API_KEY), "role": "R1 Deep Reasoning & V3 Coding Swarm"},
        "Zhipu GLM": {"status": bool(ZHIPU_API_KEY), "role": "Utility, Scraping & Fast Unit Test Gen"},
        "OpenRouter": {"status": bool(OPENROUTER_API_KEY), "role": "Universal Fallback Router"},
    }
