# Meta Llama 3 Model Reference

Reference notes for Meta Llama 3 open models on Hugging Face.

---

## Model Overview

- **Developer**: Meta AI
- **Repository**: [meta-llama/Meta-Llama-3-8B-Instruct](https://huggingface.co/meta-llama/Meta-Llama-3-8B-Instruct)
- **Variations**: 8B and 70B parameter models in pre-trained and instruction-tuned variants.
- **Architecture**: Auto-regressive transformer model utilizing Supervised Fine-Tuning (SFT) and Reinforcement Learning with Human Feedback (RLHF).

---

## Quickstart with Transformers

```python
import torch
from transformers import AutoTokenizer, AutoModelForCausalLM

model_id = "meta-llama/Meta-Llama-3-8B-Instruct"

tokenizer = AutoTokenizer.from_pretrained(model_id)
model = AutoModelForCausalLM.from_pretrained(
    model_id,
    torch_dtype=torch.bfloat16,
    device_map="auto"
)

messages = [
    {"role": "system", "content": "You are a helpful assistant."},
    {"role": "user", "content": "Explain async/await in Kotlin."}
]

input_ids = tokenizer.apply_chat_template(
    messages,
    add_generation_prompt=True,
    return_tensors="pt"
).to(model.device)

outputs = model.generate(input_ids, max_new_tokens=256)
response = tokenizer.decode(outputs[0][input_ids.shape[-1]:], skip_special_tokens=True)
print(response)
```
