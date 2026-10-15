"""
Ultra-Low Cost Swarm Worker Agent (Zhipu GLM-4 / DeepSeek-V3).

Executes high-volume, repetitive, or burst workloads (unit testing, mock data,
docstrings, type hints, edge-case validation) at fractions of a cent per million tokens.
"""

from typing import Optional
from orchestrator.agents.base import BaseAgent
from orchestrator.router import ModelRouter, AgentResponse

SWARM_SYSTEM_PROMPT = """You are the **High-Throughput Swarm Worker**, powered by ultra-low-cost, high-speed micro-token inference (GLM-4-Flash / DeepSeek-V3).

Your directives:
1. **Massive Test Coverage**: Generate comprehensive unit tests, integration tests, mock fixtures, and boundary tests covering every edge case.
2. **Exhaustive Documentation**: Generate complete docstrings, parameter explanations, and type annotations for all implemented code.
3. **High Efficiency**: Deliver structured, ready-to-run test suites adhering strictly to modern testing frameworks (pytest, jest, unittest, etc.).
4. **Zero Fluff**: Focus on code completeness and runnable test suites.
"""


class SwarmWorkerAgent(BaseAgent):
    """High-Volume Test & Utility Swarm Specialist."""

    def __init__(self, router: ModelRouter):
        super().__init__(
            name="Swarm-Worker",
            role="swarm",
            router=router,
            system_prompt=SWARM_SYSTEM_PROMPT,
        )

    def generate_test_suite(self, code_implementation: str, specs: str) -> AgentResponse:
        context = (
            f"### Specifications:\n{specs}\n\n"
            f"### Implemented Code:\n{code_implementation}"
        )
        prompt = (
            "Generate a complete, production-grade test suite covering:\n"
            "- Happy paths & common usage\n"
            "- Edge cases (null/empty inputs, boundary limits, exceptions)\n"
            "- Mock fixtures and harness setup\n"
            "- 100% assertions and runnable test methods"
        )
        return self.run(task=prompt, context=context, temperature=0.2)
