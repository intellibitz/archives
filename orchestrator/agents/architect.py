"""
Claude Master Architect & Strategic Planner Agent.

Serves as the central command node. Evaluates requirements, formulates architecture,
decomposes workflows into milestones, and determines whether deep reasoning (R1/o1)
or bulk swarming (DeepSeek/GLM) is needed.
"""

from typing import Optional
from orchestrator.agents.base import BaseAgent
from orchestrator.router import ModelRouter, AgentResponse

ARCHITECT_SYSTEM_PROMPT = """You are the **Master Architect**, the lead strategist and orchestrator of a multi-model autonomous agent collective.

Your responsibilities:
1. **Analyze Requirements**: Deeply evaluate the user's request against the Context Dossier provided by the Librarian.
2. **System Design**: Formulate a clean, modular, and robust architecture.
3. **Task Decomposition**: Break the project into concrete, sequential execution milestones.
4. **Identify Algorithmic Bottlenecks**: Explicitly flag if the task involves complex math, concurrency, graph algorithms, or hard logic bugs that require deep reasoning (e.g. DeepSeek-R1 / OpenAI o1).
5. **Issue Exact Specs**: Define precise API boundaries, inputs, and outputs for the Coder and Swarm agents.

Output Format:
### 1. Architectural Strategy
### 2. Algorithmic Complexity Check (Requires Deep Reasoning: YES / NO + rationale)
### 3. Execution Plan (Milestones & Contracts)
### 4. Implementation Directives for Coder
### 5. Test Matrix Directives for Swarm Worker
"""


class ArchitectAgent(BaseAgent):
    """Claude / Master Strategy and Architecture Planner."""

    def __init__(self, router: ModelRouter):
        super().__init__(
            name="Claude-Architect",
            role="architect",
            router=router,
            system_prompt=ARCHITECT_SYSTEM_PROMPT,
        )

    def plan(self, user_request: str, context_dossier: str) -> AgentResponse:
        prompt = (
            f"Formulate the definitive architecture and execution roadmap for this request:\n"
            f"User Request: {user_request}"
        )
        return self.run(task=prompt, context=context_dossier, temperature=0.2)
