# BYOK (Bring Your Own Key) & API Key Management Guide

This document covers best practices for obtaining, configuring, storing, and securing API keys when integrating commercial AI models (OpenAI, Anthropic Claude, DeepSeek, Qwen, Google Gemini, Mistral) into development environments, desktop clients, IDE plugins, and custom applications.

---

## 🔑 What is BYOK?
**BYOK (Bring Your Own Key)** is a licensing and architectural pattern where users supply their own cloud API keys rather than paying for a centralized subscription. This gives you:
- **Direct Billing:** Pay exact per-token costs directly to the provider (OpenAI, Anthropic, DeepSeek, etc.) with no markup.
- **Data Privacy:** Requests go straight from your client/tool to the official API endpoint without third-party proxy logging.
- **Model Flexibility:** Instantly switch between frontier models (GPT-4o, Claude 3.5 Sonnet, DeepSeek-R1) within any BYOK-compatible desktop app or IDE plugin.

---

## 🛠️ Obtaining API Keys by Provider

| Provider | Portal for API Key Generation | Common Environment Variable |
| :--- | :--- | :--- |
| **DeepSeek** | [platform.deepseek.com](https://platform.deepseek.com) | `DEEPSEEK_API_KEY` |
| **OpenAI** | [platform.openai.com](https://platform.openai.com) | `OPENAI_API_KEY` |
| **Anthropic (Claude)** | [console.anthropic.com](https://console.anthropic.com) | `ANTHROPIC_API_KEY` |
| **Alibaba Qwen** | [bailian.console.aliyun.com](https://bailian.console.aliyun.com) | `DASHSCOPE_API_KEY` |
| **Google AI (Gemini)** | [aistudio.google.com](https://aistudio.google.com) | `GEMINI_API_KEY` |
| **Mistral AI** | [console.mistral.ai](https://console.mistral.ai) | `MISTRAL_API_KEY` |
| **Zhipu GLM** | [open.bigmodel.cn](https://open.bigmodel.cn) | `ZHIPU_API_KEY` |

---

## 🔒 Security Best Practices for API Keys

1. **Never Commit Keys to Git:** Always add `.env`, `secrets.json`, and API key files to your `.gitignore`.
2. **Use Environment Variables:** Load keys dynamically at runtime:
   ```bash
   export OPENAI_API_KEY="sk-..."
   export ANTHROPIC_API_KEY="sk-ant-..."
   export DEEPSEEK_API_KEY="sk-..."
   ```
3. **Configure IDE & Desktop Clients Securely:**
   - **Cursor / VS Code Extensions (Continue):** Store keys in extension settings or local secret stores.
   - **Claude Desktop (MCP Config):** Inject keys securely into MCP server environment blocks in `claude_desktop_config.json`:
     ```json
     {
       "mcpServers": {
         "my-database-server": {
           "command": "node",
           "args ["/path/to/server.js"],
           "env": {
             "OPENAI_API_KEY": "sk-..."
           }
         }
       }
     }
     ```
4. **Rate Limits & Budget Alerts:** Set strict hard spending limits and budget alerts in each provider's developer console to prevent runaway agent loops from incurring unexpected costs.
