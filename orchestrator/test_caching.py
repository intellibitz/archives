import json
import os
import time
import requests

API_BASE = os.environ.get("LITELLM_PROXY_URL", "http://127.0.0.1:4000/v1")
API_KEY = os.environ.get("LITELLM_MASTER_KEY", "orchestrator-local-gateway")
HEADERS = {
    "Authorization": f"Bearer {API_KEY}",
    "Content-Type": "application/json"
}

def test_model(model_name):
    print(f"\n=== Testing Model: {model_name} ===")
    payload = {
        "model": model_name,
        "messages": [{"role": "user", "content": "Write a single sentence explaining quantum computing."}],
        "max_tokens": 100
    }

    # Call 1: Uncached (Prime the cache)
    t0 = time.time()
    resp1 = requests.post(f"{API_BASE}/chat/completions", headers=HEADERS, json=payload)
    t1 = time.time()
    if resp1.status_code != 200:
        print(f"Call 1 Failed: {resp1.status_code} - {resp1.text}")
        return
    print(f"Call 1 (Prime): {t1 - t0:.2f} seconds")

    # Call 2: Should hit the cache
    t0 = time.time()
    resp2 = requests.post(f"{API_BASE}/chat/completions", headers=HEADERS, json=payload)
    t1 = time.time()
    print(f"Call 2 (Cache Check): {t1 - t0:.2f} seconds")

    # Dump headers starting with x-litellm
    cache_headers = {k: v for k, v in resp2.headers.items() if "litellm" in k.lower() or "cache" in k.lower()}
    print("Relevant Headers on Call 2:", cache_headers)

if __name__ == "__main__":
    test_model("gemini-payg")
    test_model("deepseek-coder")

    print("\n=== Fetching Key Info & Spend ===")
    try:
        base_endpoint = API_BASE.replace("/v1", "")
        resp = requests.get(f"{base_endpoint}/key/info", headers=HEADERS)
        if resp.status_code == 200:
            data = resp.json()
            print("Key Info:")
            print(json.dumps(data, indent=2))
        else:
            print(f"Failed to fetch key info: {resp.status_code} {resp.text}")
    except Exception as e:
        print(f"Error fetching key info: {e}")
