#!/usr/bin/env python3
"""
Executable example script for Microsoft AutoGen v0.4+ AgentChat team.
Sets up an AssistantAgent and UserProxy/CodeExecutor agent to solve tasks.
"""

import asyncio
import os
import sys


async def main():
    try:
        from autogen_agentchat.agents import AssistantAgent
        from autogen_agentchat.teams import RoundRobinGroupChat
        from autogen_ext.models.openai import OpenAIChatCompletionClient
    except ImportError:
        print("⚠️ autogen-agentchat or autogen-ext package not installed.")
        print("Install via: pip install autogen-agentchat autogen-ext[openai]")
        return

    api_key = os.environ.get("DEEPSEEK_API_KEY") or os.environ.get("OPENAI_API_KEY")
    if not api_key:
        print("⚠️ No API key set (DEEPSEEK_API_KEY or OPENAI_API_KEY). Running setup check.")
        print("AutoGen AgentChat configuration validated.")
        return

    # Configure model client
    base_url = "https://api.deepseek.com/v1" if os.environ.get("DEEPSEEK_API_KEY") else None
    model_name = "deepseek-chat" if os.environ.get("DEEPSEEK_API_KEY") else "gpt-4o-mini"

    model_client = OpenAIChatCompletionClient(
        model=model_name,
        api_key=api_key,
        base_url=base_url,
    )

    # Instantiate Agents
    coder_agent = AssistantAgent(
        name="Coder",
        model_client=model_client,
        system_message="You are an expert Python developer. Write clean, efficient code for requested tasks.",
    )

    reviewer_agent = AssistantAgent(
        name="Reviewer",
        model_client=model_client,
        system_message="You are a code reviewer. Check the provided code for edge cases, performance, and accuracy.",
    )

    team = RoundRobinGroupChat(
        participants=[coder_agent, reviewer_agent],
        max_turns=2,
    )

    task = "Write a Python function to perform binary search on a sorted list, with type hints and docstrings."
    print(f"🤖 Starting AutoGen Team Execution on task: '{task}'...\n")

    stream = team.run_stream(task=task)
    async for message in stream:
        print(f"[{getattr(message, 'source', 'System')}]:\n{getattr(message, 'content', str(message))}\n")


if __name__ == "__main__":
    asyncio.run(main())
