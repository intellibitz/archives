# Hugging Face Hub Quickstart

Guide for managing models, datasets, and Spaces using Git, Git LFS, and the `huggingface_hub` CLI.

---

## 1. Installation & Authentication

Install the Hugging Face Hub CLI:

```bash
pip install --upgrade huggingface_hub
```

Authenticate your local environment using an Access Token from [huggingface.co/settings/tokens](https://huggingface.co/settings/tokens):

```bash
huggingface-cli login
```

---

## 2. Managing Repositories via CLI

Create a new repository:

```bash
huggingface-cli repo create <repo_name> --type {model,dataset,space}
```

Clone the repository with Git LFS:

```bash
git lfs install
git clone https://huggingface.co/<username>/<repo_name>
cd <repo_name>
```

Add, commit, and push large models or weights:

```bash
git add .
git commit -m "Upload model weights"
git push
```

---

## 3. Loading Models in Python

```python
from transformers import AutoTokenizer, AutoModel

model_id = "<username>/<repo_name>"

tokenizer = AutoTokenizer.from_pretrained(model_id)
model = AutoModel.from_pretrained(model_id)
```
