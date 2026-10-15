"""
Quality Assurance, Security & Reviewer Agent (Claude Sonnet / Gemini).

Audits the completed code, tests, and documentation against original user requirements,
identifies security vulnerabilities, verifies logic correctness, and produces the final sign-off.
"""

from typing import Optional
from orchestrator.agents.base import BaseAgent
from orchestrator.router import ModelRouter, AgentResponse

REVIEWER_SYSTEM_PROMPT = """You are the **Senior Security & Quality Reviewer**, serving as the final gatekeeper in the multi-model agent pipeline.

Your directives:
1. **Requirement Verification**: Confirm that all user requirements and architectural directives were fulfilled without omissions.
2. **Security & Vulnerability Audit**: Inspect the code for injection flaws, memory leaks, unhandled exceptions, and concurrency deadlocks.
3. **Correctness & Reliability**: Check that unit test assertions are realistic and tests will pass under all conditions.
4. **Final Sign-Off**: Provide a summary of the solution, key design decisions, and instructions on how to run or deploy the code.
"""


class ReviewerAgent(BaseAgent):
    """Quality Assurance and Security Gatekeeper."""

    def __init__(self, router: ModelRouter):
        super().__init__(
            name="Senior-Reviewer",
            role="reviewer",
            router=router,
            system_prompt=REVIEWER_SYSTEM_PROMPT,
        )

    def review(
        self,
        user_request: str,
        code_implementation: str,
        test_suite: str,
        plan: str,
    ) -> AgentResponse:
        context = (
            f"### Original Request:\n{user_request}\n\n"
            f"### Architectural Plan:\n{plan}\n\n"
            f"### Implemented Code:\n{code_implementation}\n\n"
            f"### Test Suite:\n{test_suite}"
        )
        prompt = (
            "Conduct a comprehensive quality, correctness, and security review.\n"
            "Deliver:\n"
            "1. Verification Matrix against original goals\n"
            "2. Security & Performance assessment\n"
            "3. Final Operational Sign-Off and Execution Instructions"
        )
        return self.run(task=prompt, context=context, temperature=0.1)
