# [DeepSeek](https://www.deepseek.com) Evaluation & Integration Harness

This guide provides a comprehensive overview of the evaluation benchmark harness, test framework, API client harness, and local model runner configurations for [DeepSeek-V3](https://github.com/deepseek-ai/DeepSeek-V3) and [DeepSeek-R1](https://github.com/deepseek-ai/DeepSeek-R1).

---

## 📊 1. Official Evaluation Harness & Benchmark Results

[DeepSeek](https://www.deepseek.com) models have been rigorously evaluated using standard LLM evaluation harnesses, including [EleutherAI lm-evaluation-harness](https://github.com/EleutherAI/lm-evaluation-harness), [SWE-bench](https://github.com/swe-bench/SWE-bench), [LiveCodeBench](https://github.com/LiveCodeBench/LiveCodeBench), and [HumanEval](https://github.com/openai/human-eval).

### Benchmark Performance Comparison Table

| Benchmark / Evaluation Harness | Task Domain | [DeepSeek-V3](https://github.com/deepseek-ai/DeepSeek-V3) | [DeepSeek-R1](https://github.com/deepseek-ai/DeepSeek-R1) | OpenAI o1-mini | OpenAI o1 (1217) | Claude 3.5 Sonnet |
| :--- | :--- | :---: | :---: | :---: | :---: | :---: |
| **AIME 2024 (Pass@1)** | Olympiad Mathematics | 39.2% | **79.8%** (90.0% Consensus) | 63.6% | 79.2% | 16.0% |
| **MATH-500 (Pass@1)** | High-School Math | 90.2% | **97.3%** | 90.0% | 96.4% | 78.3% |
| **SWE-bench Verified** | Agentic Software Engineering | 42.0% | **49.2%** | 41.6% | 48.9% | 49.0% |
| **Codeforces (Percentile)** | Competitive Programming | - | **96.3%** (Rating 2029) | 93.4% | 96.6% | - |
| **LiveCodeBench (Pass@1)** | Coding Challenges | 40.5% | **65.9%** | 53.8% | 63.4% | 38.9% |
| **Codeforces (Pass@1)** | Algorithmic Problem Solving | 58.7% | **96.3%** | - | - | - |
| **GPQA Diamond (Pass@1)** | Graduate Level Science | 59.1% | **71.5%** | 60.0% | 75.7% | 65.0% |
| **MMLU (Pass@1)** | General Multitask Knowledge | 88.5% | **90.8%** | 85.2% | 91.8% | 88.3% |
| **HumanEval (Pass@1)** | Basic Python Code Generation | 82.6% | **92.3%** | 90.0% | 92.4% | 93.7% |
| **SimpleQA (Correct Rate)** | Factual QA & Hallucination | 24.9% | **24.9%** | 7.0% | 47.0% | 28.4% |

---

## 🛠️ 2. Running Benchmark Evaluations via `lm-evaluation-harness`

You can evaluate DeepSeek API endpoints or open-weights models locally using EleutherAI's `lm-evaluation-harness`.

### Installation
```bash
pip install lm-eval
```

### Running Benchmark Tasks against DeepSeek API
```bash
export DEEPSEEK_API_KEY="sk-..."

lm_eval --model openai-completions \
    --model_args model=deepseek-chat,base_url=https://api.deepseek.com/v1,api_key=$DEEPSEEK_API_KEY \
    --tasks mmlu,gsm8k,human_eval \
    --batch_size 16 \
    --output_path ./eval_results/deepseek_v3_results.json
```

---

## ⚡ 3. API Integration Harness (Python & TypeScript)

The [DeepSeek API](https://platform.deepseek.com) is fully OpenAI-compatible. You can use the standard OpenAI client SDK or any BYOK framework.

### Python Integration Harness
```python
import os
from openai import OpenAI

# Initialize client pointing to DeepSeek API endpoint
client = OpenAI(
    api_key=os.getenv("DEEPSEEK_API_KEY"),
    base_url="https://api.deepseek.com"
)

# 1. Standard Chat / General Tasks (DeepSeek-V3)
v3_response = client.chat.completions.create(
    model="deepseek-chat",
    messages=[
        {"role": "system", "content": "You are a helpful coding assistant."},
        {"role": "user", "content": "Write a Kotlin extension function to convert JSON string to a Map."}
    ]
)
print("DeepSeek-V3 Response:\n", v3_response.choices[0].message.content)

# 2. Reasoning & Complex Logic (DeepSeek-R1)
r1_response = client.chat.completions.create(
    model="deepseek-reasoner",
    messages=[
        {"role": "user", "content": "Solve for x: 3x^2 - 12x + 9 = 0 with full reasoning steps."}
    ]
)

# Extract Chain-of-Thought (Reasoning) and Final Response
reasoning_tokens = r1_response.choices[0].message.reasoning_content
final_answer = r1_response.choices[0].message.content

print("Reasoning Process:\n", reasoning_tokens)
print("Final Output:\n", final_answer)
```

### Node.js / TypeScript Integration Harness
```typescript
import OpenAI from "openai";

const openai = new OpenAI({
  baseURL: "https://api.deepseek.com",
  apiKey: process.env.DEEPSEEK_API_KEY,
});

async function run() {
  const completion = await openai.chat.completions.create({
    messages: [{ role: "user", content: "Explain DeepSeek-V3 architecture." }],
    model: "deepseek-chat",
  });

  console.log(completion.choices[0].message.content);
}

run();
```

---

## 💡 4. DeepSeek Prompt Caching Harness

DeepSeek automatically caches prompt prefixes. When sent repetitive or multi-turn agent contexts, input token prices drop by 90%:

- **[DeepSeek-V3](https://github.com/deepseek-ai/DeepSeek-V3):** $0.14 / 1M (Cache Miss) $\rightarrow$ **$0.014 / 1M (Cache Hit)**
- **[DeepSeek-R1](https://github.com/deepseek-ai/DeepSeek-R1):** $0.55 / 1M (Cache Miss) $\rightarrow$ **$0.14 / 1M (Cache Hit)**

### Prompt Caching Verification
In response metadata, verify the usage block:
```json
"usage": {
  "prompt_tokens": 5000,
  "completion_tokens": 300,
  "total_tokens": 5300,
  "prompt_tokens_details": {
    "cached_tokens": 4800
  }
}
```

---

## 🏠 5. Open-Weights Local Execution Harness

DeepSeek models can be executed locally using open-weights harnesses like Ollama, vLLM, and LM Studio.

### Ollama CLI Harness
```bash
# DeepSeek-R1 Distilled Models
ollama run deepseek-r1:70b
ollama run deepseek-r1:32b
ollama run deepseek-r1:14b
ollama run deepseek-r1:8b
ollama run deepseek-r1:1.5b
```

### High-Throughput vLLM Harness
```bash
pip install vllm

# Serve DeepSeek-R1 Distilled Model
python -m vllm.entrypoints.openai.api_server \
    --model deepseek-ai/DeepSeek-R1-Distill-Qwen-32B \
    --tensor-parallel-size 2 \
    --port 8000
```
