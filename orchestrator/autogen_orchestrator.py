"""
AutoGen Multi-Agent Team Orchestration Workflow.

Implements collaborative multi-agent conversation teams using modern AutoGen (v0.4+):
- Librarian: Antigravity / Gemini 2M context for repository ingestion.
- Architect: Claude 3.5/3.7 Sonnet for high-level technical contracts.
- Reasoner: DeepSeek-R1 for algorithmic proof & invariants.
- Coder: DeepSeek-V3 / Claude for production code.
- SwarmTester: GLM-4-Flash for exhaustive test suites.
- Reviewer: Senior QA Gatekeeper for security audit & sign-off.
"""

import asyncio
from typing import List, Dict, Any, Optional
from dataclasses import dataclass, field

from autogen_agentchat.agents import AssistantAgent
from autogen_agentchat.teams import RoundRobinGroupChat, SelectorGroupChat
from autogen_agentchat.conditions import TextMentionTermination, MaxMessageTermination
from autogen_ext.models.openai import OpenAIChatCompletionClient

from orchestrator.config import (
    LITELLM_API_BASE,
    LITELLM_MASTER_KEY,
)


@dataclass
class AutoGenMessage:
    source: str
    content: str


@dataclass
class AutoGenReport:
    task: str
    messages: List[AutoGenMessage] = field(default_factory=list)
    total_messages: int = 0
    final_output: str = ""


class AutoGenOrchestrator:
    """Multi-Agent Team Orchestrator using AutoGen and the LiteLLM Multi-Model Plumbing."""

    def __init__(
        self,
        base_url: str = LITELLM_API_BASE,
        api_key: str = LITELLM_MASTER_KEY,
        dry_run: bool = False,
    ):
        self.base_url = base_url
        self.api_key = api_key
        self.dry_run = dry_run

    def _create_client(self, model: str) -> OpenAIChatCompletionClient:
        """Creates an OpenAIChatCompletionClient bound to the local plumbing proxy."""
        return OpenAIChatCompletionClient(
            model=model,
            base_url=self.base_url,
            api_key=self.api_key,
            model_info={
                "vision": False,
                "function_calling": True,
                "json_output": True,
                "family": "unknown",
                "structured_output": False,
            },
        )

    def build_team(
        self,
        selector: bool = False,
        include_reasoner: bool = True,
        max_messages: int = 12,
    ):
        """Constructs the specialized multi-model agent team."""
        # 1. Antigravity Librarian
        librarian_client = self._create_client("antigravity-librarian")
        librarian = AssistantAgent(
            name="Librarian",
            model_client=librarian_client,
            system_message=(
                "You are the Antigravity Context Librarian powered by Google Gemini 2M context. "
                "Analyze repo structure, dependencies, and constraints. "
                "Output a dense Context Dossier for the Architect."
            ),
        )

        # 2. Claude Master Architect
        architect_client = self._create_client("claude-architect")
        architect = AssistantAgent(
            name="Architect",
            model_client=architect_client,
            system_message=(
                "You are the Lead Software Architect powered by Claude. "
                "Formulate architectural contracts, module boundaries, and API specifications. "
                "Delegate algorithmic proofs to Reasoner and code production to Coder."
            ),
        )

        # 3. DeepSeek-R1 Reasoner
        reasoner_client = self._create_client("deepseek-reasoner")
        reasoner = AssistantAgent(
            name="Reasoner",
            model_client=reasoner_client,
            system_message=(
                "You are the Algorithmic Logic Specialist powered by DeepSeek-R1. "
                "Provide formal mathematical proofs, verify concurrency invariants, "
                "and analyze worst-case time/space complexity."
            ),
        )

        # 4. Lead Coder
        coder_client = self._create_client("deepseek-coder")
        coder = AssistantAgent(
            name="Coder",
            model_client=coder_client,
            system_message=(
                "You are the Lead Production Coder powered by DeepSeek-V3 / Claude. "
                "Synthesize complete, production-grade code with zero placeholders or ellipses."
            ),
        )

        # 5. Micro-Token Swarm Worker
        swarm_client = self._create_client("glm-utility")
        swarm = AssistantAgent(
            name="SwarmTester",
            model_client=swarm_client,
            system_message=(
                "You are the High-Throughput Swarm Worker powered by GLM-4-Flash. "
                "Generate complete unit test suites with 100% assertions, edge cases, and mock fixtures."
            ),
        )

        # 6. Senior Reviewer Gatekeeper
        reviewer_client = self._create_client("claude-architect")
        reviewer = AssistantAgent(
            name="Reviewer",
            model_client=reviewer_client,
            system_message=(
                "You are the Senior Security and Quality Gatekeeper. "
                "Audit the code and answers against the user requirements. "
                "Always finish your review by appending 'TERMINATE' at the end of your response."
            ),
        )

        agents = [librarian, architect]
        if include_reasoner:
            agents.append(reasoner)
        agents.extend([coder, swarm, reviewer])

        termination = TextMentionTermination("TERMINATE") | MaxMessageTermination(max_messages)

        if selector:
            selector_client = self._create_client("claude-architect")
            return SelectorGroupChat(
                agents,
                model_client=selector_client,
                termination_condition=termination,
            )
        return RoundRobinGroupChat(agents, termination_condition=termination)

    async def run_async(
        self,
        task: str,
        selector: bool = False,
        include_reasoner: bool = True,
        max_messages: int = 10,
    ) -> AutoGenReport:
        """Executes the AutoGen multi-agent group conversation."""
        if self.dry_run:
            await asyncio.sleep(0.05)
            mock_msgs = [
                AutoGenMessage("User", f"Task: {task}"),
                AutoGenMessage("Librarian", "[DRY-RUN MOCK] Context Dossier generated from Gemini 2M."),
                AutoGenMessage("Architect", "[DRY-RUN MOCK] Architecture blueprint formulated by Claude."),
                AutoGenMessage("Reasoner", "[DRY-RUN MOCK] Algorithmic proofs verified by DeepSeek-R1."),
                AutoGenMessage("Coder", "[DRY-RUN MOCK] Production implementation synthesized."),
                AutoGenMessage("SwarmTester", "[DRY-RUN MOCK] Full unit test coverage generated by GLM-4."),
                AutoGenMessage("Reviewer", "[DRY-RUN MOCK] Verification passed. TERMINATE"),
            ]
            return AutoGenReport(
                task=task,
                messages=mock_msgs,
                total_messages=len(mock_msgs),
                final_output=mock_msgs[-1].content,
            )

        team = self.build_team(
            selector=selector,
            include_reasoner=include_reasoner,
            max_messages=max_messages,
        )
        result = await team.run(task=task)

        parsed_messages: List[AutoGenMessage] = []
        for msg in result.messages:
            content = msg.content if isinstance(msg.content, str) else str(msg.content)
            parsed_messages.append(AutoGenMessage(source=msg.source, content=content))

        final_text = parsed_messages[-1].content if parsed_messages else ""
        return AutoGenReport(
            task=task,
            messages=parsed_messages,
            total_messages=len(parsed_messages),
            final_output=final_text,
        )

    def run(
        self,
        task: str,
        selector: bool = False,
        include_reasoner: bool = True,
        max_messages: int = 10,
    ) -> AutoGenReport:
        """Synchronous wrapper for run_async."""
        return asyncio.run(
            self.run_async(
                task=task,
                selector=selector,
                include_reasoner=include_reasoner,
                max_messages=max_messages,
            )
        )
