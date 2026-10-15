"""
Interactive & Command-Line Interface for the AI Agent Orchestrator.

Provides colored dashboards, live step telemetry, cost tracking,
and interactive pair-programming commands.
"""

import sys
import argparse
from typing import Optional
from rich.console import Console
from rich.table import Table
from rich.panel import Panel
from rich.markdown import Markdown
from rich.progress import Progress, SpinnerColumn, TextColumn

from orchestrator.config import (
    get_active_providers_summary,
    PRICING_TABLE,
    get_model_for_role,
    WORKSPACE_ROOT,
)
from orchestrator.router import ModelRouter
from orchestrator.master import MasterOrchestrator, ExecutionReport

console = Console()


def show_status():
    """Displays active providers, API keys, and assigned roles."""
    table = Table(title="🤖 Multi-Model Provider & Subscription Status", border_style="cyan")
    table.add_column("Provider / Subscription", style="bold white")
    table.add_column("Status", justify="center")
    table.add_column("Orchestration Role & Capabilities", style="dim")

    summary = get_active_providers_summary()
    for provider, info in summary.items():
        status_str = "[bold green]🟢 CONNECTED[/bold green]" if info["status"] else "[bold yellow]⚪ MISSING KEY[/bold yellow]"
        table.add_row(provider, status_str, info["role"])

    console.print(table)

    # Assigned Models Table
    model_table = Table(title="🎯 Dynamic Role Resolution (Active Mappings)", border_style="magenta")
    model_table.add_column("Agent Role", style="bold cyan")
    model_table.add_column("Resolved Model", style="bold white")
    model_table.add_column("Cost Tier", style="dim")

    roles = [
        ("architect", "Claude 3.5/3.7 Sonnet (Master Planner)"),
        ("librarian", "Antigravity / Gemini (Large Context Ingestion)"),
        ("reasoner", "DeepSeek-R1 / o1 (Algorithmic Logic)"),
        ("coder", "DeepSeek-V3 / Claude (Code Implementation)"),
        ("swarm", "GLM-4 / DeepSeek-V3 (Test & Utility Swarm)"),
        ("reviewer", "Senior QA & Security Reviewer"),
    ]

    for role_name, desc in roles:
        model = get_model_for_role(role_name)
        model_table.add_row(f"{role_name.upper()} ({desc})", model, "Auto-Routed")

    console.print(model_table)


def show_pricing():
    """Displays token economics and rates per 1M tokens."""
    table = Table(title="💰 Per 1M Tokens Pricing Ledger", border_style="green")
    table.add_column("Model Identifier", style="bold white")
    table.add_column("Input Rate ($/1M)", justify="right")
    table.add_column("Cached Hit Rate ($/1M)", justify="right", style="green")
    table.add_column("Output Rate ($/1M)", justify="right", style="cyan")

    for model, rates in PRICING_TABLE.items():
        table.add_row(
            model,
            f"${rates.get('input', 0.0):.2f}",
            f"${rates.get('cached_input', 0.0):.3f}",
            f"${rates.get('output', 0.0):.2f}",
        )

    console.print(table)


def run_workflow_cli(task: str, dry_run: bool = False, force_reasoner: bool = False, use_proxy: bool = False):
    """Executes a complete orchestrated workflow with live terminal visuals."""
    console.print(Panel(f"[bold cyan]Task Objective:[/bold cyan] {task}", title="🚀 Master Orchestrator Running"))

    router = ModelRouter(use_proxy=use_proxy, dry_run=dry_run)
    orchestrator = MasterOrchestrator(router=router, dry_run=dry_run)

    with Progress(
        SpinnerColumn(),
        TextColumn("[progress.description]{task.description}"),
        console=console,
    ) as progress:
        task_id = progress.add_task("[cyan]Initializing agent team...", total=None)

        def on_step(agent: str, summary: str, details: dict):
            model_info = f" ({details.get('model', '')})" if details.get("model") else ""
            progress.update(task_id, description=f"[bold yellow][{agent}][/bold yellow]{model_info} {summary}")

        report = orchestrator.execute_workflow(
            task=task,
            force_reasoner=force_reasoner,
            step_callback=on_step,
        )

    console.print("\n[bold green]✅ Workflow Completed Successfully![/bold green]\n")

    # Display Execution Telemetry Table
    step_table = Table(title="📊 Step-by-Step Execution Telemetry", border_style="blue")
    step_table.add_column("Agent Node", style="bold cyan")
    step_table.add_column("Model Used", style="white")
    step_table.add_column("Tokens", justify="right")
    step_table.add_column("Time (s)", justify="right")
    step_table.add_column("Cost (USD)", justify="right", style="green")

    for s in report.steps:
        step_table.add_row(
            s.agent_name,
            s.model,
            str(s.tokens),
            f"{s.duration_sec:.2f}s",
            f"${s.cost_usd:.5f}",
        )

    console.print(step_table)
    console.print(
        f"[bold]Total Tokens:[/bold] {report.total_tokens:,} | "
        f"[bold]Total Runtime:[/bold] {report.total_duration_sec:.2f}s | "
        f"[bold green]Total Cost: ${report.total_cost_usd:.5f} USD[/bold green]\n"
    )

    # Deliverables Tabs / Panels
    console.print(Panel(Markdown(report.architect_plan), title="📐 1. Master Architecture Plan", border_style="cyan"))

    if report.reasoner_solution:
        console.print(Panel(Markdown(report.reasoner_solution), title="🧠 2. DeepSeek-R1 Algorithmic Proof", border_style="magenta"))

    console.print(Panel(Markdown(report.code_implementation), title="💻 3. Lead Coder Implementation", border_style="green"))
    console.print(Panel(Markdown(report.test_suite), title="🧪 4. Swarm-Worker Unit Test Suite", border_style="yellow"))
    console.print(Panel(Markdown(report.final_review), title="🛡️ 5. Quality & Security Review", border_style="red"))


def main():
    parser = argparse.ArgumentParser(description="Multi-Model AI Agent Orchestrator")
    subparsers = parser.add_subparsers(dest="command")

    # status command
    subparsers.add_parser("status", help="Check active providers, keys, and model routes")

    # pricing command
    subparsers.add_parser("pricing", help="Show token rates and economics table")

    # run command
    run_parser = subparsers.add_parser("run", help="Run an orchestrated task")
    run_parser.add_argument("task", type=str, help="Description of task to execute")
    run_parser.add_argument("--dry-run", action="store_true", help="Simulate execution without calling live APIs")
    run_parser.add_argument("--reasoner", action="store_true", help="Force deep algorithmic reasoning phase")
    run_parser.add_argument("--proxy", action="store_true", help="Route requests through local LiteLLM plumbing proxy gateway")

    args = parser.parse_args()

    if args.command == "status":
        show_status()
    elif args.command == "pricing":
        show_pricing()
    elif args.command == "run":
        run_workflow_cli(args.task, dry_run=args.dry_run, force_reasoner=args.reasoner, use_proxy=args.proxy)
    else:
        # Default behavior: show status & help
        show_status()
        console.print("\n[bold]Usage:[/bold] python3 -m orchestrator.cli run \"Your Task Description\"")


if __name__ == "__main__":
    main()
