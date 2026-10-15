#!/usr/bin/env python3
"""
Automated Test Suite for the Multi-Model Agent Orchestration Framework.
"""

import sys
import os
from pathlib import Path

# Add project root to sys.path
WORKSPACE_ROOT = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(WORKSPACE_ROOT))

from orchestrator.config import (
    PRICING_TABLE,
    get_active_providers_summary,
    get_model_for_role,
)
from orchestrator.router import ModelRouter
from orchestrator.master import MasterOrchestrator


def test_provider_status():
    print("[TEST 1/5] Checking Provider Status & Config Detection...")
    summary = get_active_providers_summary()
    assert isinstance(summary, dict), "Summary must be a dict"
    for prov, data in summary.items():
        print(f"  • {prov}: {'Active' if data['status'] else 'Missing Key'} ({data['role']})")
    print("  -> Passed ✅\n")


def test_dynamic_model_resolution():
    print("[TEST 2/5] Testing Dynamic Role & Fallback Resolution...")
    roles = ["architect", "librarian", "reasoner", "coder", "swarm", "reviewer"]
    for r in roles:
        model = get_model_for_role(r)
        assert model and isinstance(model, str), f"Role {r} must resolve to a valid model"
        print(f"  • Role '{r}' resolves to: {model}")
    print("  -> Passed ✅\n")


def test_cost_calculation():
    print("[TEST 3/5] Testing Micro-Token Cost Calculation Engine...")
    router = ModelRouter(dry_run=True)
    # Test DeepSeek-V3 pricing (1M input = $0.14, 1M cached = $0.003, 1M output = $0.60)
    cost = router.calculate_cost(
        model_name="deepseek/deepseek-chat",
        prompt_tokens=1_000_000,
        completion_tokens=1_000_000,
        cached_tokens=900_000, # 90% cache hit
    )
    # Expected: (100k non-cached = $0.014) + (900k cached = $0.0027) + (1M output = $0.60) = ~$0.6167
    print(f"  • Calculated cost for 1M input (90% cached) + 1M output on DeepSeek-V3: ${cost:.6f} USD")
    assert 0.60 < cost < 0.65, f"Cost {cost} out of expected range"
    print("  -> Passed ✅\n")


def test_agent_instantiation():
    print("[TEST 4/5] Testing Agent Team Initialization...")
    router = ModelRouter(dry_run=True)
    orchestrator = MasterOrchestrator(router=router, dry_run=True)
    assert orchestrator.librarian.name == "Antigravity-Librarian"
    assert orchestrator.architect.name == "Claude-Architect"
    assert orchestrator.reasoner.name == "DeepSeek-Reasoner"
    assert orchestrator.coder.name == "Lead-Coder"
    assert orchestrator.swarm.name == "Swarm-Worker"
    assert orchestrator.reviewer.name == "Senior-Reviewer"
    print("  • All 6 specialized agent classes initialized successfully")
    print("  -> Passed ✅\n")


def test_end_to_end_workflow():
    print("[TEST 5/5] Testing End-to-End Orchestrated Pipeline (Dry-Run)...")
    router = ModelRouter(dry_run=True)
    orchestrator = MasterOrchestrator(router=router, dry_run=True)

    task = "Design an async LRU cache with TTL eviction and concurrent thread-safe locks"
    report = orchestrator.execute_workflow(task=task, force_reasoner=True)

    assert report.architect_plan, "Architect plan must not be empty"
    assert report.reasoner_solution, "Reasoner solution must not be empty"
    assert report.code_implementation, "Code implementation must not be empty"
    assert report.test_suite, "Test suite must not be empty"
    assert report.final_review, "Final review must not be empty"
    assert len(report.steps) >= 4, "Must execute at least 4 workflow steps"

    print(f"  • Workflow steps executed: {len(report.steps)}")
    print(f"  • Total tokens tracked: {report.total_tokens}")
    print(f"  • Total cost calculated: ${report.total_cost_usd:.6f} USD")
    print("  -> Passed ✅\n")


if __name__ == "__main__":
    print("======================================================================")
    print(" Running AI Agent Orchestrator Diagnostic Test Suite")
    print("======================================================================\n")
    test_provider_status()
    test_dynamic_model_resolution()
    test_cost_calculation()
    test_agent_instantiation()
    test_end_to_end_workflow()
    print("🎉 All 5 diagnostic tests PASSED successfully!")
