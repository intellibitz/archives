"""
DeepSeek-R1 / OpenAI o1 Algorithmic Reasoner Agent.

Specializes in solving hard algorithmic proofs, mathematical formulations,
concurrency state machines, and edge cases prior to code synthesis.
"""

from typing import Optional
from orchestrator.agents.base import BaseAgent
from orchestrator.router import ModelRouter, AgentResponse

REASONER_SYSTEM_PROMPT = """You are the **Deep Algorithmic Reasoner**, an elite specialist powered by frontier reasoning models (DeepSeek-R1 / OpenAI o1/o3-mini).

Your primary directives:
1. **Unpack Complex Logic**: Work through mathematical proofs, edge-case analysis, state transitions, and concurrency guarantees step by step.
2. **Eliminate Subtle Bugs**: Detect race conditions, off-by-one errors, memory leaks, and performance bottlenecks before any line of code is committed.
3. **Formal Specification**: Output clear pseudocode, invariant properties, and algorithmic proofs that the Coder agent can implement deterministically.

Maintain strict mathematical and computational rigor. Think through every branch and edge condition.
"""


class ReasonerAgent(BaseAgent):
    """DeepSeek-R1 / o1 Algorithmic & Formal Logic Specialist."""

    def __init__(self, router: ModelRouter):
        super().__init__(
            name="DeepSeek-Reasoner",
            role="reasoner",
            router=router,
            system_prompt=REASONER_SYSTEM_PROMPT,
        )

    def solve_algorithm(self, problem_statement: str, architectural_specs: str) -> AgentResponse:
        prompt = (
            f"Analyze and solve the core algorithmic and logical challenge described below:\n"
            f"Challenge: {problem_statement}\n\n"
            f"Provide:\n"
            f"1. Detailed Chain-of-Thought Logical Proof & Invariant Analysis\n"
            f"2. Time & Space Complexity Evaluation\n"
            f"3. Concrete Pseudocode & Edge Case Handling Matrix"
        )
        return self.run(task=prompt, context=architectural_specs)
