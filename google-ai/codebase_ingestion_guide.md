# 📚 Massive Codebase Ingestion with Google Gemini (1M–2M Context)

This guide details how to leverage Google Gemini's **1,000,000 to 2,000,000 token context window** to ingest, analyze, and refactor entire software repositories without vector databases, embedding pipelines, or chunking errors.

---

## 🚫 1. The Vector RAG Problem in Software Engineering

Traditional Retrieval-Augmented Generation (RAG) splits code into arbitrary chunks (typically 256–1024 tokens) and indexes them in a vector database:

* **Lost Call Graphs:** Slicing a class definition across multiple chunks breaks type signatures, inheritance trees, and dependency imports.
* **Semantic Mismatch:** Searching for *"where is the database connection closed"* often fails to match implicit context, variable re-assignments, or deferred statements (`defer`, `finally`).
* **Cross-File Blindness:** Traditional RAG retrieves 5 to 10 isolated snippets, missing transitive dependencies across the project.

### The Solution: Direct Context Ingestion
With Gemini 1.5 Pro and 2.0 Flash, you can pack an entire repository of 50,000+ lines of code into a single prompt. The model reasons over the **complete Abstract Syntax Tree (AST) and project architecture simultaneously**.

---

## 🛠️ 2. Repository Bundling Script (Python)

Use this lightweight script to bundle an entire repository into a structured markdown prompt format:

```python
#!/usr/bin/env python3
"""
Packs workspace source files into a single Gemini-ready context document.
"""

import os
from pathlib import Path

IGNORE_DIRS = {".git", ".idea", ".vscode", "__pycache__", "node_modules", "target", "build", "dist", ".venv"}
ALLOWED_EXTS = {".py", ".kt", ".java", ".ts", ".js", ".go", ".rs", ".sql", ".md", ".json", ".yaml", ".yml"}

def bundle_repository(root_path: Path, output_file: Path):
    with open(output_file, "w", encoding="utf-8") as out:
        out.write(f"# Codebase Snapshot: {root_path.name}\n\n")
        
        for root, dirs, files in os.walk(root_path):
            dirs[:] = [d for d in dirs if d not in IGNORE_DIRS]
            for file in sorted(files):
                ext = Path(file).suffix.lower()
                if ext in ALLOWED_EXTS:
                    full_path = Path(root) / file
                    rel_path = full_path.relative_to(root_path)
                    try:
                        content = full_path.read_text(encoding="utf-8", errors="replace")
                        out.write(f"\n--- File: {rel_path} ---\n```{ext.lstrip('.')}\n")
                        out.write(content)
                        out.write("\n```\n")
                    except Exception as e:
                        print(f"Skipping {rel_path}: {e}")

if __name__ == "__main__":
    bundle_repository(Path("."), Path("codebase_bundle.md"))
    print("✅ Generated codebase_bundle.md for Gemini ingestion.")
```

---

## ⚡ 3. Ingesting with the Gemini Python SDK

```python
import os
from google import genai
from google.genai import types

client = genai.Client(api_key=os.environ["GEMINI_API_KEY"])

# Upload large bundle or pass as text
with open("codebase_bundle.md", "r", encoding="utf-8") as f:
    repo_text = f.read()

prompt = """
You are the Master Codebase Architect. Analyze this entire repository snapshot.
Identify:
1. Architectural bottlenecks and circular dependencies.
2. Unhandled edge cases and concurrency vulnerabilities.
3. A prioritized refactoring roadmap.
"""

response = client.models.generate_content(
    model="gemini-2.0-flash",
    contents=[repo_text, prompt],
    config=types.GenerateContentConfig(
        temperature=0.2,
        max_output_tokens=4096,
    )
)

print(response.text)
```

---

## 💰 4. Economics of 1M Token Codebase Ingestion

| Workload | Tokens Ingested | Gemini 2.0 Flash Cost | Gemini 1.5 Pro Cost | Claude 3.5 Sonnet (200K Cap) |
| :--- | :---: | :---: | :---: | :---: |
| Small Repo (15k lines) | ~100,000 | **$0.01** | $0.12 | $0.30 |
| Medium Repo (60k lines) | ~400,000 | **$0.04** | $0.50 | ❌ Exceeds 200k limit |
| Large Monorepo (150k lines) | ~1,000,000 | **$0.10** | $1.25 | ❌ Exceeds 200k limit |
| Full Enterprise Stack (300k lines) | ~2,000,000 | ❌ Exceeds 1M | **$2.50** | ❌ Exceeds 200k limit |

Gemini's prompt caching further drops repeated queries by **75%**, allowing continuous codebase re-evaluation for fractions of a penny.
