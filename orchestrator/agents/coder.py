"""
Production Code Implementation Agent (Claude Sonnet / DeepSeek-V3).

Translates architectural plans and algorithmic specifications into clean,
tested, production-ready code with complete implementations (no placeholders).
"""

from typing import Optional
from orchestrator.agents.base import BaseAgent
from orchestrator.router import ModelRouter, AgentResponse

CODER_SYSTEM_PROMPT = """You are the **Lead Implementation Engineer**, powered by state-of-the-art coding models (Claude 3.5 Sonnet / DeepSeek-V3).

Your directives:
1. **Flawless Implementation**: Implement the architectural specification and algorithmic pseudocode with 100% precision.
2. **No Placeholders or Ellipses**: Never output `// TODO: implement later` or `... rest of code`. Every function, class, and method must be completely written and functional.
3. **Clean Code & Idiomatic Patterns**: Follow modern software engineering best practices, type annotations, and robust error handling.
4. **File & Diff Organization**: Clearly label target file paths, complete file contents, or exact unified diffs.
"""


class CoderAgent(BaseAgent):
    """Implementation Engineer Specialist."""

    def __init__(self, router: ModelRouter):
        super().__init__(
            name="Lead-Coder",
            role="coder",
            router=router,
            system_prompt=CODER_SYSTEM_PROMPT,
        )

    def implement(
        self,
        task: str,
        specs: str,
        reasoning_solution: Optional[str] = None,
    ) -> AgentResponse:
        context_parts = [f"### Architectural Specifications:\n{specs}"]
        if reasoning_solution:
            context_parts.append(f"### Algorithmic Solution & Pseudocode:\n{reasoning_solution}")
        combined_context = "\n\n".join(context_parts)

        prompt = (
            f"Produce the complete, fully realized code implementation for:\n"
            f"Objective: {task}\n\n"
            f"Provide all code blocks with filenames and complete implementation details."
        )
        return self.run(task=prompt, context=combined_context, temperature=0.1)
