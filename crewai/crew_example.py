#!/usr/bin/env python3
"""
Executable example script for CrewAI multi-agent orchestration.
Demonstrates setting up a Researcher Agent and Writer Agent to build an AI analysis report.
"""

import os
import sys

from crewai import Agent, Crew, Process, Task


def main():
    api_key = os.environ.get("DEEPSEEK_API_KEY") or os.environ.get("OPENROUTER_API_KEY") or os.environ.get("OPENAI_API_KEY")
    if not api_key:
        print("⚠️ Warning: No API key set. Please set DEEPSEEK_API_KEY, OPENROUTER_API_KEY, or OPENAI_API_KEY.")
        print("Demonstrating Crew structure definition.\n")

    # Configure DeepSeek or OpenRouter model
    if os.environ.get("DEEPSEEK_API_KEY"):
        os.environ["OPENAI_API_BASE"] = "https://api.deepseek.com/v1"
        os.environ["OPENAI_MODEL_NAME"] = "deepseek-chat"

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
