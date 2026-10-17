#!/usr/bin/env python3
"""
Executable example script for Hugging Face Smolagents CodeAgent.
Demonstrates a code-first autonomous agent generating Python actions.
"""

import os
import sys


def main():
    try:
        from smolagents import CodeAgent, DuckDuckGoSearchTool, HfApiModel, LiteLLMModel
    except ImportError:
        print("⚠️ smolagents package not installed.")
        print("Install via: pip install smolagents duckduckgo-search litellm")
        return

    api_key = os.environ.get("DEEPSEEK_API_KEY") or os.environ.get("OPENROUTER_API_KEY") or os.environ.get("OPENAI_API_KEY")
    if not api_key:
        print("⚠️ No API key set. Demonstrating Smolagents structure.")
        print("CodeAgent structure initialized.")
        return

    # Instantiate model using LiteLLM wrapper (DeepSeek-V3 recommended)
    if os.environ.get("DEEPSEEK_API_KEY"):
        model = LiteLLMModel(
            model_id="deepseek/deepseek-chat",
            api_key=os.environ["DEEPSEEK_API_KEY"],
            api_base="https://api.deepseek.com/v1",
        )
    else:
        model = HfApiModel()

    agent = CodeAgent(
        tools=[DuckDuckGoSearchTool()],
        model=model,
        add_base_tools=True,
    )

    task = "How many days passed between the release of DeepSeek-V3 and DeepSeek-R1?"
    print(f"🤖 Running Smolagents CodeAgent on task: '{task}'...\n")

    result = agent.run(task)
    print(f"\n✅ Result: {result}")


if __name__ == "__main__":
    main()
