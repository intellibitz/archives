# uv - Modern Python Package & Project Management

[uv](https://github.com/astral-sh/uv) by Astral is an extremely fast Python package and project manager written in Rust. It serves as a unified, drop-in replacement for `pip`, `pip-tools`, `virtualenv`, and `poetry`.

---

## 1. Installation

Install `uv` via standalone installer:

```bash
curl -LsSf https://astral.sh/uv/install.sh | sh
```

Or via Homebrew:

```bash
brew install uv
```

---

## 2. Managing Python Versions

`uv` downloads and manages standalone Python versions without needing `pyenv`:

```bash
# Install Python 3.13 or 3.12
uv python install 3.13

# List available and installed Python versions
uv python list
```

---

## 3. Virtual Environments

```bash
# Create a virtual environment using Python 3.13
uv venv --python 3.13

# Activate the virtual environment
source .venv/bin/activate
```

---

## 4. Package Installation & Resolution

```bash
# Install packages 10-100x faster than traditional pip
uv pip install numpy pandas torch transformers

# Install from requirements.txt
uv pip install -r requirements.txt

# Compile locked requirements
uv pip compile requirements.in -o requirements.txt
```

---

## 5. Running Scripts & Ephemeral Tools

Run standalone scripts with inline dependency metadata without prior manual installation:

```bash
uv run --with "requests,rich" my_script.py
```
