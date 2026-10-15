"""
Antigravity Context Librarian Agent.

Leverages Google Gemini's 1M-2M token context window to ingest huge repositories,
logs, documentation, and external references to produce a condensed, high-signal Context Dossier.
"""

from typing import Optional
from orchestrator.agents.base import BaseAgent
from orchestrator.router import ModelRouter, AgentResponse

LIBRARIAN_SYSTEM_PROMPT = """You are the **Antigravity Context Librarian**, an expert at ingesting massive context windows (up to 2,000,000 tokens) with high recall accuracy.

Your mission:
1. Absorb large codebases, file structures, schemas, API documentation, and logs.
2. Filter out boilerplate, noise, and irrelevant syntax.
3. Extract critical entities, dependency graphs, data contracts, and architectural constraints.
4. Output a concise, high-signal "Context Dossier" formatted in Markdown that the Master Architect and implementation agents can act upon immediately without getting bogged down by large token overhead.

Never generate speculative or ungrounded code; ground everything strictly in the provided references and files.
"""


class LibrarianAgent(BaseAgent):
    """Antigravity / Gemini Context Ingestion Specialist."""

    def __init__(self, router: ModelRouter):
        super().__init__(
            name="Antigravity-Librarian",
            role="librarian",
            router=router,
            system_prompt=LIBRARIAN_SYSTEM_PROMPT,
        )

    def ingest_and_summarize(self, workspace_files: str, task: str) -> AgentResponse:
        prompt = (
            f"Analyze the following workspace resources in relation to this objective:\n"
            f"Objective: {task}\n\n"
            f"Generate a structured Context Dossier containing:\n"
            f"- Relevant Core Components & Files\n"
            f"- Data Models & Interfaces Involved\n"
            f"- Key Constraints & Gotchas\n"
            f"- Essential snippets or references"
        )
        return self.run(task=prompt, context=workspace_files, temperature=0.1)
