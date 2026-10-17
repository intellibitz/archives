#!/usr/bin/env python3
"""
Executable example script for Pydantic AI type-safe autonomous agent.
Demonstrates structured output validation and dependency injection.
"""

import os
import sys
from dataclasses import dataclass

from pydantic import BaseModel, Field


# 1. Define Output Schema
class AgentAnalysis(BaseModel):
    summary: str = Field(description="Summary of the analysis")
    confidence_score: float = Field(description="Confidence score between 0.0 and 1.0")
    key_takeaways: list[str] = Field(description="List of key takeaways")


# 2. Define Dependencies
@dataclass
class SystemDeps:
    environment: str
    api_endpoint: str


def main():
    try:
        from pydantic_ai import Agent, RunContext
    except ImportError:
        print("⚠️ pydantic-ai package not installed.")
        print("Install via: pip install pydantic-ai")
        return

    api_key = os.environ.get("DEEPSEEK_API_KEY") or os.environ.get("OPENAI_API_KEY")
    if not api_key:
        print("⚠️ No API key found. Demonstrating Pydantic AI agent schema initialization.")
        print(f"Output Schema: {AgentAnalysis.model_json_schema()}")
        return

    model_string = "openai:deepseek-chat" if os.environ.get("DEEPSEEK_API_KEY") else "openai:gpt-4o-mini"
    if os.environ.get("DEEPSEEK_API_KEY"):
        os.environ["OPENAI_BASE_URL"] = "https://api.deepseek.com/v1"

    # Instantiate Type-Safe Agent
    agent = Agent(
        model_string,
        result_type=AgentAnalysis,
        system_prompt="You are an autonomous technical auditor. Return structured data matching the schema.",
    )

    deps = SystemDeps(environment="production", api_endpoint="https://api.deepseek.com/v1")
    task = "Analyze the advantages of DeepSeek-V3 prompt caching for autonomous AI agents."

    print(f"🤖 Running Pydantic AI Agent on task: '{task}'...\n")
    result = agent.run_sync(task, deps=deps)

    print("✅ Successfully Validated Response:")
    print(f"Summary: {result.data.summary}")
    print(f"Confidence: {result.data.confidence_score}")
    print(f"Takeaways: {result.data.key_takeaways}")


if __name__ == "__main__":
    main()
