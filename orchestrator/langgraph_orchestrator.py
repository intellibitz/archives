"""
LangGraph Multi-Model Agent Orchestration Workflow.

Implements a stateful, graph-based agent pipeline using LangGraph:
- Librarian (Gemini 2M Context): Ingests repository files and logs.
- Architect (Claude 3.5/3.7 Sonnet): Strategic contract & module planning.
- Reasoner (DeepSeek-R1 / o1): Deep algorithmic proofs & concurrency verification.
- Coder (DeepSeek-V3 / Claude): Production implementation.
- Swarm Worker (GLM-4 / DeepSeek-V3): High-throughput unit test generation.
- Reviewer (Senior QA): Audit & iterative feedback loop.
"""

import time
from typing import TypedDict, List, Dict, Any, Optional, Literal
from langchain_core.messages import SystemMessage, HumanMessage
from langchain_openai import ChatOpenAI
from langgraph.graph import StateGraph, START, END

from orchestrator.config import (
    LITELLM_API_BASE,
    LITELLM_MASTER_KEY,
    PRICING_TABLE,
)


class OrchestratorState(TypedDict, total=False):
    task: str
    workspace_context: Optional[str]
    context_dossier: str
    architect_plan: str
    needs_deep_reasoning: bool
    reasoner_proof: Optional[str]
    code_implementation: str
    test_suite: str
    review_feedback: Optional[str]
    is_approved: bool
    iteration_count: int
    telemetry: List[Dict[str, Any]]
    total_cost_usd: float
    total_tokens: int
    total_cached_tokens: int


class LangGraphOrchestrator:
    """Builds and compiles the LangGraph Multi-Model Pipeline."""

    def __init__(
        self,
        base_url: str = LITELLM_API_BASE,
        api_key: str = LITELLM_MASTER_KEY,
        dry_run: bool = False,
    ):
        self.base_url = base_url
        self.api_key = api_key
        self.dry_run = dry_run
        self.app = self._build_graph()

    def _get_llm(self, model: str, temperature: float = 0.2, max_tokens: int = 2048) -> ChatOpenAI:
        """Returns a ChatOpenAI client connected to our LiteLLM gateway."""
        return ChatOpenAI(
            model=model,
            base_url=self.base_url,
            api_key=self.api_key,
            temperature=temperature,
            max_tokens=max_tokens,
            timeout=120,
            max_retries=2,
        )

    def _invoke_llm(self, model: str, system_prompt: str, user_prompt: str) -> tuple[str, int, int, float]:
        """Invokes model and returns (content, total_tokens, cached_tokens, cost)."""
        if self.dry_run:
            time.sleep(0.05)
            mock_text = f"[DRY-RUN LANGGRAPH MOCK] Response from {model} for prompt: {user_prompt[:80]}..."
            return mock_text, 150, 60, 0.00002

        llm = self._get_llm(model=model)
        fallback_model = "local-reasoner" if "reasoner" in model else "local-coder"
        fallback_llm = self._get_llm(model=fallback_model)
        runnable = llm.with_fallbacks([fallback_llm])

        messages = [
            SystemMessage(content=system_prompt),
            HumanMessage(content=user_prompt),
        ]
        resp = runnable.invoke(messages)
        if isinstance(resp.content, list):
            content = "".join([c if isinstance(c, str) else c.get("text", "") for c in resp.content])
        elif resp.content is None:
            content = ""
        else:
            content = str(resp.content)

        meta = getattr(resp, "response_metadata", {}) or {}
        usage = meta.get("token_usage", {})
        total_tokens = usage.get("total_tokens", 250)
        prompt_tokens = usage.get("prompt_tokens", total_tokens)
        cached_tokens = (
            usage.get("prompt_tokens_details", {}).get("cached_tokens", 0)
            or usage.get("prompt_cache_hit_tokens", 0)
        )
        uncached_tokens = max(0, prompt_tokens - cached_tokens)
        completion_tokens = usage.get("completion_tokens", 0)

        # Standard pricing: cached prompt tokens receive a 90% discount (DeepSeek: $0.014 vs $0.14)
        input_rate = PRICING_TABLE.get(model, {}).get("input", 0.50)
        cached_rate = input_rate * 0.10
        output_rate = PRICING_TABLE.get(model, {}).get("output", input_rate * 2)

        cost = (
            (uncached_tokens / 1_000_000.0) * input_rate
            + (cached_tokens / 1_000_000.0) * cached_rate
            + (completion_tokens / 1_000_000.0) * output_rate
        )
        return content, total_tokens, cached_tokens, cost

    # --------------------------------------------------------------------------
    # Graph Nodes
    # --------------------------------------------------------------------------
    def librarian_node(self, state: OrchestratorState) -> OrchestratorState:
        """Context Librarian powered by Antigravity Gemini (2M tokens context)."""
        system_prompt = (
            "You are the Antigravity Context Librarian powered by Google Gemini 2M context. "
            "Your job is to analyze repository structure, dependencies, and constraints, "
            "and output a dense, high-signal Context Dossier. Be factual, concise, and structured."
        )
        user_prompt = f"Task: {state['task']}\nContext: {state.get('workspace_context', 'Repository root')}"
        model = "antigravity-librarian"
        content, tokens, cached_tokens, cost = self._invoke_llm(model, system_prompt, user_prompt)

        telemetry = list(state.get("telemetry", []))
        telemetry.append({
            "node": "Librarian",
            "model": model,
            "tokens": tokens,
            "cached_tokens": cached_tokens,
            "cost_usd": cost,
        })
        return {
            "context_dossier": content,
            "telemetry": telemetry,
            "total_tokens": state.get("total_tokens", 0) + tokens,
            "total_cached_tokens": state.get("total_cached_tokens", 0) + cached_tokens,
            "total_cost_usd": state.get("total_cost_usd", 0.0) + cost,
        }

    def architect_node(self, state: OrchestratorState) -> OrchestratorState:
        """Lead Architect powered by Claude 3.5 / 3.7 Sonnet."""
        system_prompt = (
            "You are the Lead Software Architect powered by Claude. "
            "You design robust software architectures, modular boundaries, and clean APIs. "
            "First, determine if this task requires deep mathematical, formal, concurrency, or "
            "algorithmic proofs. Include '[NEEDS_DEEP_REASONING: YES]' or '[NEEDS_DEEP_REASONING: NO]' in your header. "
            "Then output the structural implementation blueprint."
        )
        user_prompt = f"Task: {state['task']}\nContext Dossier:\n{state.get('context_dossier', '')}"
        model = "claude-architect"
        content, tokens, cached_tokens, cost = self._invoke_llm(model, system_prompt, user_prompt)

        needs_deep_reasoning = "[NEEDS_DEEP_REASONING: YES]" in content or any(
            k in state["task"].lower()
            for k in ["concurrency", "lock-free", "algorithm", "math", "proof", "ring buffer", "tree", "distributed"]
        )

        telemetry = list(state.get("telemetry", []))
        telemetry.append({
            "node": "Architect",
            "model": model,
            "tokens": tokens,
            "cached_tokens": cached_tokens,
            "cost_usd": cost,
            "needs_deep_reasoning": needs_deep_reasoning,
        })
        return {
            "architect_plan": content,
            "needs_deep_reasoning": needs_deep_reasoning,
            "telemetry": telemetry,
            "total_tokens": state.get("total_tokens", 0) + tokens,
            "total_cached_tokens": state.get("total_cached_tokens", 0) + cached_tokens,
            "total_cost_usd": state.get("total_cost_usd", 0.0) + cost,
        }

    def reasoner_node(self, state: OrchestratorState) -> OrchestratorState:
        """Algorithmic Reasoner powered by DeepSeek-R1 / local-reasoner."""
        system_prompt = (
            "You are the Algorithmic Logic Specialist powered by DeepSeek-R1. "
            "You prove correctness, verify boundary invariants, analyze worst-case time/space "
            "complexity, and eliminate race conditions. Output formal proofs and step-by-step logic."
        )
        user_prompt = (
            f"Task: {state['task']}\n"
            f"Architecture Plan:\n{state.get('architect_plan', '')}"
        )
        model = "deepseek-reasoner"
        content, tokens, cached_tokens, cost = self._invoke_llm(model, system_prompt, user_prompt)

        telemetry = list(state.get("telemetry", []))
        telemetry.append({
            "node": "Reasoner",
            "model": model,
            "tokens": tokens,
            "cached_tokens": cached_tokens,
            "cost_usd": cost,
        })
        return {
            "reasoner_proof": content,
            "telemetry": telemetry,
            "total_tokens": state.get("total_tokens", 0) + tokens,
            "total_cached_tokens": state.get("total_cached_tokens", 0) + cached_tokens,
            "total_cost_usd": state.get("total_cost_usd", 0.0) + cost,
        }

    def coder_node(self, state: OrchestratorState) -> OrchestratorState:
        """Production Coder powered by DeepSeek-V3 / Claude."""
        system_prompt = (
            "You are the Lead Production Coder powered by DeepSeek-V3 / Claude. "
            "Implement production-grade code that satisfies the architectural plan and algorithmic proofs. "
            "Provide complete, clean, modular code with ZERO placeholders or ellipses."
        )
        user_prompt = (
            f"Task: {state['task']}\n"
            f"Architecture Plan:\n{state.get('architect_plan', '')}\n"
            f"Algorithmic Proof:\n{state.get('reasoner_proof', 'Standard implementation')}\n"
        )
        if state.get("review_feedback"):
            user_prompt += f"\nPrevious Review Rejection Feedback to Fix:\n{state['review_feedback']}"

        model = "deepseek-coder"
        content, tokens, cached_tokens, cost = self._invoke_llm(model, system_prompt, user_prompt)

        telemetry = list(state.get("telemetry", []))
        telemetry.append({
            "node": "Coder",
            "model": model,
            "tokens": tokens,
            "cached_tokens": cached_tokens,
            "cost_usd": cost,
            "iteration": state.get("iteration_count", 0),
        })
        return {
            "code_implementation": content,
            "telemetry": telemetry,
            "total_tokens": state.get("total_tokens", 0) + tokens,
            "total_cached_tokens": state.get("total_cached_tokens", 0) + cached_tokens,
            "total_cost_usd": state.get("total_cost_usd", 0.0) + cost,
        }

    def swarm_worker_node(self, state: OrchestratorState) -> OrchestratorState:
        """Micro-Token Swarm powered by GLM-4-Flash / local-coder."""
        system_prompt = (
            "You are the High-Throughput Swarm Worker powered by GLM-4-Flash. "
            "You generate exhaustive unit test suites, mock fixtures, and boundary tests at fractions "
            "of a cent per million tokens. Ensure 100% assertions and edge coverage."
        )
        user_prompt = (
            f"Code to test:\n{state.get('code_implementation', '')}\n"
            f"Requirements:\n{state.get('architect_plan', '')}"
        )
        model = "glm-utility"
        content, tokens, cached_tokens, cost = self._invoke_llm(model, system_prompt, user_prompt)

        telemetry = list(state.get("telemetry", []))
        telemetry.append({
            "node": "Swarm-Worker",
            "model": model,
            "tokens": tokens,
            "cached_tokens": cached_tokens,
            "cost_usd": cost,
        })
        return {
            "test_suite": content,
            "telemetry": telemetry,
            "total_tokens": state.get("total_tokens", 0) + tokens,
            "total_cached_tokens": state.get("total_cached_tokens", 0) + cached_tokens,
            "total_cost_usd": state.get("total_cost_usd", 0.0) + cost,
        }

    def reviewer_node(self, state: OrchestratorState) -> OrchestratorState:
        """Senior Security & QA Gatekeeper powered by Claude Sonnet / Gemini."""
        system_prompt = (
            "You are the Senior Security and Quality Gatekeeper. "
            "Review the code implementation and test suite against the task requirements. "
            "If the code meets production standards, state '[STATUS: APPROVED]'. "
            "If it fails security or completeness, state '[STATUS: REJECTED]' and list actionable fixes."
        )
        user_prompt = (
            f"Task: {state['task']}\n"
            f"Code Implementation:\n{state.get('code_implementation', '')}\n"
            f"Test Suite:\n{state.get('test_suite', '')}"
        )
        model = "claude-architect"
        content, tokens, cached_tokens, cost = self._invoke_llm(model, system_prompt, user_prompt)

        is_approved = "[STATUS: APPROVED]" in content or "APPROVED" in content.upper()
        telemetry = list(state.get("telemetry", []))
        telemetry.append({
            "node": "Reviewer",
            "model": model,
            "tokens": tokens,
            "cached_tokens": cached_tokens,
            "cost_usd": cost,
            "is_approved": is_approved,
        })
        return {
            "review_feedback": content,
            "is_approved": is_approved,
            "iteration_count": state.get("iteration_count", 0) + 1,
            "telemetry": telemetry,
            "total_tokens": state.get("total_tokens", 0) + tokens,
            "total_cached_tokens": state.get("total_cached_tokens", 0) + cached_tokens,
            "total_cost_usd": state.get("total_cost_usd", 0.0) + cost,
        }

    # --------------------------------------------------------------------------
    # Conditional Edges
    # --------------------------------------------------------------------------
    def _route_after_architect(self, state: OrchestratorState) -> Literal["reasoner_node", "coder_node"]:
        if state.get("needs_deep_reasoning", False):
            return "reasoner_node"
        return "coder_node"

    def _route_after_reviewer(self, state: OrchestratorState) -> Literal["coder_node", "__end__"]:
        # If approved or already revised once, finish
        if state.get("is_approved", True) or state.get("iteration_count", 0) >= 2:
            return "__end__"
        return "coder_node"

    # --------------------------------------------------------------------------
    # Graph Construction
    # --------------------------------------------------------------------------
    def _build_graph(self):
        workflow = StateGraph(OrchestratorState)

        # Add Nodes
        workflow.add_node("librarian_node", self.librarian_node)
        workflow.add_node("architect_node", self.architect_node)
        workflow.add_node("reasoner_node", self.reasoner_node)
        workflow.add_node("coder_node", self.coder_node)
        workflow.add_node("swarm_worker_node", self.swarm_worker_node)
        workflow.add_node("reviewer_node", self.reviewer_node)

        # Add Edges
        workflow.add_edge(START, "librarian_node")
        workflow.add_edge("librarian_node", "architect_node")

        # Conditional route: Architect -> Reasoner or Coder
        workflow.add_conditional_edges(
            "architect_node",
            self._route_after_architect,
            {
                "reasoner_node": "reasoner_node",
                "coder_node": "coder_node",
            },
        )

        workflow.add_edge("reasoner_node", "coder_node")
        workflow.add_edge("coder_node", "swarm_worker_node")
        workflow.add_edge("swarm_worker_node", "reviewer_node")

        # Conditional route: Reviewer -> Coder (retry) or END
        workflow.add_conditional_edges(
            "reviewer_node",
            self._route_after_reviewer,
            {
                "coder_node": "coder_node",
                "__end__": END,
            },
        )

        return workflow.compile()

    def run(self, task: str, workspace_context: Optional[str] = None) -> OrchestratorState:
        """Executes the full compiled LangGraph workflow."""
        initial_state: OrchestratorState = {
            "task": task,
            "workspace_context": workspace_context,
            "iteration_count": 0,
            "telemetry": [],
            "total_cost_usd": 0.0,
            "total_tokens": 0,
            "total_cached_tokens": 0,
        }
        return self.app.invoke(initial_state)
