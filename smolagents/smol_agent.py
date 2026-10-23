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

    # Route through local LiteLLM Proxy Gateway
    model = LiteLLMModel(
        model_id="openai/deepseek-coder",
        api_key=os.environ.get("LITELLM_MASTER_KEY", "orchestrator-local-gateway"),
        api_base=os.environ.get("LITELLM_PROXY_URL", "http://127.0.0.1:4000/v1"),
    )

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
