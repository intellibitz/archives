#!/usr/bin/env python3
"""
Executable example script for CrewAI multi-agent orchestration.
Demonstrates setting up a Researcher Agent and Writer Agent to build an AI analysis report.
"""

import os
import sys

from crewai import Agent, Crew, Process, Task


def main():
    # Route through local LiteLLM Proxy Gateway
    proxy_url = os.environ.get("LITELLM_PROXY_URL", "http://127.0.0.1:4000/v1")
    master_key = os.environ.get("LITELLM_MASTER_KEY", "orchestrator-local-gateway")
    os.environ["OPENAI_API_BASE"] = proxy_url
    os.environ["OPENAI_API_KEY"] = master_key
    os.environ["OPENAI_MODEL_NAME"] = "openai/deepseek-coder"
    api_key = master_key

    # 1. Define Agents
    researcher = Agent(
        role="Senior AI Researcher",
        goal="Analyze the latest trends in autonomous AI agents for 2026",
        backstory=(
            "You are an expert AI technology analyst specializing in multi-agent systems, "
            "agentic software engineering, and LLM economics."
        ),
        verbose=True,
        allow_delegation=False,
    )

    writer = Agent(
        role="Lead Technical Editor",
        goal="Synthesize research insights into a structured technical summary",
        backstory=(
            "You are a seasoned technical writer who translates complex AI research into "
            "actionable insights for developers and architects."
        ),
        verbose=True,
        allow_delegation=False,
    )

    # 2. Define Tasks
    research_task = Task(
        description="Identify top 3 autonomous agent paradigms and their key token-efficiency advantages.",
        expected_output="Bullet list of 3 agent paradigms with cost efficiency metrics.",
        agent=researcher,
    )

    writing_task = Task(
        description="Write a 3-paragraph executive summary based on the research findings.",
        expected_output="A clean markdown executive summary document.",
        agent=writer,
    )

    # 3. Form Crew & Execute
    ai_crew = Crew(
        agents=[researcher, writer],
        tasks=[research_task, writing_task],
        process=Process.sequential,
        verbose=True,
    )

    print("🚀 Initiating CrewAI Multi-Agent Execution...")
    if api_key:
        result = ai_crew.kickoff()
        print("\n✅ Crew Execution Finished!")
        print(f"📋 Output:\n{result}")
    else:
        print("Crew setup validated successfully!")


if __name__ == "__main__":
    main()
