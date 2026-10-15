"""
The Master Orchestrator Engine.

Orchestrates the collaborative agent pipeline across Antigravity (Gemini),
Claude (Architect), Codex/DeepSeek (Reasoner), Coder, and Swarm (GLM-4).
"""

import time
from typing import Optional, Dict, Any, List, Callable
from dataclasses import dataclass, field

from orchestrator.router import ModelRouter, AgentResponse, ModelUsage
from orchestrator.agents.librarian import LibrarianAgent
from orchestrator.agents.architect import ArchitectAgent
from orchestrator.agents.reasoner import ReasonerAgent
from orchestrator.agents.coder import CoderAgent
from orchestrator.agents.swarm import SwarmWorkerAgent
from orchestrator.agents.reviewer import ReviewerAgent


@dataclass
class OrchestratorStep:
    agent_name: str
    role: str
    model: str
    duration_sec: float
    tokens: int
    cost_usd: float
    summary: str


@dataclass
class ExecutionReport:
    task: str
    context_dossier: Optional[str]
    architect_plan: str
    reasoner_solution: Optional[str]
    code_implementation: str
    test_suite: str
    final_review: str
    steps: List[OrchestratorStep] = field(default_factory=list)
    total_tokens: int = 0
    total_cost_usd: float = 0.0
    total_duration_sec: float = 0.0


class MasterOrchestrator:
    """The Supreme Orchestrator coordinating all models and agents."""

    def __init__(self, router: Optional[ModelRouter] = None, dry_run: bool = False):
        self.router = router or ModelRouter(dry_run=dry_run)
        self.librarian = LibrarianAgent(self.router)
        self.architect = ArchitectAgent(self.router)
        self.reasoner = ReasonerAgent(self.router)
        self.coder = CoderAgent(self.router)
        self.swarm = SwarmWorkerAgent(self.router)
        self.reviewer = ReviewerAgent(self.router)

    def execute_workflow(
        self,
        task: str,
        workspace_context: Optional[str] = None,
        force_reasoner: bool = False,
        step_callback: Optional[Callable[[str, str, Dict[str, Any]], None]] = None,
    ) -> ExecutionReport:
        """
        Executes the complete multi-agent workflow:
        1. Context Ingestion (Librarian)
        2. Strategic Planning (Architect)
        3. Deep Algorithmic Reasoning (Reasoner, conditional)
        4. Code Synthesis (Coder)
        5. Bulk Test Generation (Swarm)
        6. Security & Correctness Audit (Reviewer)
        """
        start_time = time.time()
        steps: List[OrchestratorStep] = []

        def log_step(agent_name: str, role: str, resp: AgentResponse, summary: str):
            step = OrchestratorStep(
                agent_name=agent_name,
                role=role,
                model=resp.model,
                duration_sec=resp.usage.latency_sec,
                tokens=resp.usage.total_tokens,
                cost_usd=resp.usage.estimated_cost_usd,
                summary=summary,
            )
            steps.append(step)
            if step_callback:
                step_callback(agent_name, summary, {
                    "model": resp.model,
                    "tokens": resp.usage.total_tokens,
                    "cost": resp.usage.estimated_cost_usd,
                    "latency": resp.usage.latency_sec,
                })

        # ----------------------------------------------------------------------
        # Phase 1: Ingestion & Briefing (Antigravity Context Librarian)
        # ----------------------------------------------------------------------
        context_dossier: Optional[str] = None
        if workspace_context:
            if step_callback:
                step_callback("Librarian", "Ingesting workspace context via massive context window...", {})
            lib_resp = self.librarian.ingest_and_summarize(workspace_context, task)
            context_dossier = lib_resp.content
            log_step("Antigravity-Librarian", "librarian", lib_resp, "Generated structured Context Dossier")

        # ----------------------------------------------------------------------
        # Phase 2: Master Architecture & Decomposition (Claude Master Architect)
        # ----------------------------------------------------------------------
        if step_callback:
            step_callback("Architect", "Synthesizing architectural strategy and decomposing milestones...", {})
        arch_resp = self.architect.plan(task, context_dossier or "No local workspace context provided.")
        architect_plan = arch_resp.content
        log_step("Claude-Architect", "architect", arch_resp, "Produced Architectural Strategy & Milestone Directives")

        # ----------------------------------------------------------------------
        # Phase 3: Deep Algorithmic Reasoning (DeepSeek-R1 / o1 Specialist)
        # ----------------------------------------------------------------------
        # Determine if deep reasoning is required based on architect output or flag
        needs_reasoning = force_reasoner or (
            "Requires Deep Reasoning: YES" in architect_plan
            or "algorithmic" in task.lower()
            or "concurrency" in task.lower()
            or "optimization" in task.lower()
        )

        reasoner_solution: Optional[str] = None
        if needs_reasoning:
            if step_callback:
                step_callback("Reasoner", "Solving complex logic/invariants via DeepSeek-R1 reasoning tokens...", {})
            reason_resp = self.reasoner.solve_algorithm(task, architect_plan)
            reasoner_solution = reason_resp.content
            log_step("DeepSeek-Reasoner", "reasoner", reason_resp, "Solved algorithmic invariants and provided formal pseudocode")

        # ----------------------------------------------------------------------
        # Phase 4: Production Code Synthesis (Lead Coder)
        # ----------------------------------------------------------------------
        if step_callback:
            step_callback("Coder", "Implementing production-ready code modules and file structures...", {})
        coder_resp = self.coder.implement(task, architect_plan, reasoner_solution)
        code_implementation = coder_resp.content
        log_step("Lead-Coder", "coder", coder_resp, "Synthesized complete production implementation")

        # ----------------------------------------------------------------------
        # Phase 5: Bulk Test & Documentation Swarm (GLM-4 / DeepSeek-V3)
        # ----------------------------------------------------------------------
        if step_callback:
            step_callback("Swarm-Worker", "Generating unit test coverage and edge fixtures via micro-tokens...", {})
        swarm_resp = self.swarm.generate_test_suite(code_implementation, architect_plan)
        test_suite = swarm_resp.content
        log_step("Swarm-Worker", "swarm", swarm_resp, "Generated comprehensive unit tests and mock fixtures")

        # ----------------------------------------------------------------------
        # Phase 6: Quality Assurance & Security Gatekeeper (Senior Reviewer)
        # ----------------------------------------------------------------------
        if step_callback:
            step_callback("Reviewer", "Auditing code against requirements, security standards, and reliability...", {})
        rev_resp = self.reviewer.review(task, code_implementation, test_suite, architect_plan)
        final_review = rev_resp.content
        log_step("Senior-Reviewer", "reviewer", rev_resp, "Completed security audit and operational sign-off")

        # ----------------------------------------------------------------------
        # Assemble Final Report
        # ----------------------------------------------------------------------
        total_tokens = sum(s.tokens for s in steps)
        total_cost = sum(s.cost_usd for s in steps)
        total_duration = time.time() - start_time

        return ExecutionReport(
            task=task,
            context_dossier=context_dossier,
            architect_plan=architect_plan,
            reasoner_solution=reasoner_solution,
            code_implementation=code_implementation,
            test_suite=test_suite,
            final_review=final_review,
            steps=steps,
            total_tokens=total_tokens,
            total_cost_usd=total_cost,
            total_duration_sec=total_duration,
        )
