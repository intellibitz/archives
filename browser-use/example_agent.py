#!/usr/bin/env python3
"""
Executable example script for Browser Use autonomous web agent.
Uses DeepSeek-V3 or OpenAI-compatible endpoint to autonomously navigate and perform web actions.
"""

import asyncio
import os
import sys

from browser_use import Agent
from langchain_openai import ChatOpenAI


async def main():
    # 1. Configure LLM provider (DeepSeek-V3 recommended for agentic cost efficiency)
    api_key = os.environ.get("DEEPSEEK_API_KEY") or os.environ.get("OPENROUTER_API_KEY") or os.environ.get("OPENAI_API_KEY")
    if not api_key:
        print("⚠️ Warning: No API key found. Set DEEPSEEK_API_KEY, OPENROUTER_API_KEY, or OPENAI_API_KEY.")
        print("Running in dry-run/demonstration mode.")
        return

    base_url = "https://api.deepseek.com/v1" if os.environ.get("DEEPSEEK_API_KEY") else None
    model_name = "deepseek-chat" if os.environ.get("DEEPSEEK_API_KEY") else "gpt-4o-mini"

    llm = ChatOpenAI(
        model=model_name,
        api_key=api_key,
        base_url=base_url,
    )

    # 2. Define the autonomous web task
    task_description = (
        "Go to https://github.com/trending, search for top trending repositories "
        "in Python, and extract the names and descriptions of the top 3 projects."
    )

    print(f"🤖 Starting Browser Use Agent...")
    print(f"🎯 Task: {task_description}\n")

    # 3. Instantiate and run the autonomous agent
    agent = Agent(
        task=task_description,
        llm=llm,
        headless=True,
    )

    result = await agent.run()
    print("\n✅ Task Complete!")
    print(f"📋 Result:\n{result}")


if __name__ == "__main__":
    asyncio.run(main())
