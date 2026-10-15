"""
Base Agent definition for the Multi-Model Orchestration Framework.
"""

from abc import ABC, abstractmethod
from typing import Dict, Any, List, Optional
from orchestrator.router import ModelRouter, AgentResponse


class BaseAgent(ABC):
    """Abstract base class for all domain agents."""

    def __init__(
        self,
        name: str,
        role: str,
        router: ModelRouter,
        system_prompt: str,
    ):
        self.name = name
        self.role = role
        self.router = router
        self.system_prompt = system_prompt
        self.history: List[Dict[str, str]] = []

    def reset(self):
        """Resets the agent's short-term conversation memory."""
        self.history = []

    def run(
        self,
        task: str,
        context: Optional[str] = None,
        temperature: float = 0.2,
        override_model: Optional[str] = None,
    ) -> AgentResponse:
        """Executes a single step given the task and optional context."""
        user_message_parts = []
        if context:
            user_message_parts.append(f"### Context & Reference Materials:\n{context}\n")
        user_message_parts.append(f"### Current Objective:\n{task}")
        user_message = "\n".join(user_message_parts)

        messages = [
            {"role": "system", "content": self.system_prompt},
            {"role": "user", "content": user_message},
        ]

        response = self.router.call(
            role=self.role,
            messages=messages,
            temperature=temperature,
            override_model=override_model,
        )

        # Store in internal trace
        self.history.append({"role": "user", "content": user_message})
        self.history.append({"role": "assistant", "content": response.content})

        return response
