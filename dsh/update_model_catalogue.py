"""Replace the litellm model list in the desktop profile patch with every
model group the gateway currently serves, with sensible context metadata."""

import json
import pathlib
import re
import shutil
import time
import urllib.request

PATCH = pathlib.Path(r"C:\Users\user\.dsh\profiles\desktop\cordis.patch.yml")
GATEWAY = "http://127.0.0.1:4000/v1/models"
MASTER_KEY = None
for line in pathlib.Path(r"C:\Users\user\.secrets\.env").read_text(encoding="utf-8").splitlines():
    m = re.match(r"^\s*LITELLM_MASTER_KEY\s*=\s*(.+)$", line)
    if m:
        MASTER_KEY = m.group(1).strip()
        break

req = urllib.request.Request(GATEWAY, headers={"Authorization": f"Bearer {MASTER_KEY}"})
with urllib.request.urlopen(req, timeout=10) as resp:
    table = json.loads(resp.read().decode("utf-8"))
ids = sorted(str(entry["id"]) for entry in table.get("data", []) if entry.get("id"))
print(f"gateway groups: {len(ids)}")

# context window / max output per model family
FAMILY = [
    (r"^claude", 200_000, 8_192),
    (r"^gemini-pro|^gemini-3\.1-pro", 1_048_576, 65_536),
    (r"^gemini", 1_048_576, 65_536),
    (r"^kimi", 262_144, 16_384),
    (r"^deepseek", 131_072, 8_192),
    (r"^siliconflow", 131_072, 8_192),
    (r"^deepinfra", 131_072, 8_192),
    (r"^glm", 131_072, 8_192),
    (r"^local-reasoner", 65_536, 8_192),
    (r"^local-llama", 131_072, 4_096),
    (r"^local", 32_768, 4_096),
    (r"^antigravity-pro", 1_048_576, 65_536),
    (r"^antigravity", 1_048_576, 65_536),
    (r"^openai", 128_000, 16_384),
    (r"^codex", 200_000, 100_000),
    (r"^text-embedding|^gemini-embedding", 2_048, None),
]

lines = ["        models:"]
for model_id in ids:
    window, max_tokens = 131_072, 8_192
    for pattern, ctx, out in FAMILY:
        if re.search(pattern, model_id):
            window, max_tokens = ctx, out
            break
    indent = "          "
    lines.append(f"{indent}- id: {model_id}")
    lines.append(f"{indent}  name: {model_id}")
    if max_tokens is None:
        lines.append(f"{indent}  contextWindow: 2048")
    else:
        lines.append(f"{indent}  contextWindow: {window}")
        lines.append(f"{indent}  maxTokens: {max_tokens}")

text = PATCH.read_text(encoding="utf-8")
backup = PATCH.with_suffix(f".yml.bak-{time.strftime('%Y%m%d-%H%M%S')}")
shutil.copy2(PATCH, backup)

raw = text.splitlines()
try:
    litellm_at = next(i for i, l in enumerate(raw) if l.rstrip() == "      litellm:")
except StopIteration:
    raise SystemExit("litellm block not found")

models_at = next(
    (i for i in range(litellm_at, len(raw)) if raw[i].rstrip() == "        models:"),
    None,
)
if models_at is None:
    raise SystemExit("models: key not found under litellm")

end = next(
    (i for i in range(models_at + 1, len(raw)) if re.match(r"^ {0,6}\S", raw[i])),
    len(raw),
)

new_raw = raw[: models_at] + lines + raw[end:]
PATCH.write_text("\n".join(new_raw) + "\n", encoding="utf-8")
print(f"backup: {backup}")
print(f"models replaced: {end - models_at - 1} -> {len(ids)}")
