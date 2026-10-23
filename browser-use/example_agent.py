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
    # Configure LLM provider via local LiteLLM Proxy Gateway
    api_key = os.environ.get("LITELLM_MASTER_KEY", "orchestrator-local-gateway")
    base_url = os.environ.get("LITELLM_PROXY_URL", "http://127.0.0.1:4000/v1")
    model_name = "deepseek-coder"

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
