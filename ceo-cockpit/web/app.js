// Antigravity CEO Cockpit — Executive Interaction Engine

document.addEventListener('DOMContentLoaded', () => {
  // DOM Elements
  const tabButtons = document.querySelectorAll('.mode-tab');
  const executivePrompt = document.getElementById('executivePrompt');
  const btnDispatch = document.getElementById('btnDispatch');
  const presetChips = document.querySelectorAll('.preset-chip');
  const decisionBanner = document.getElementById('decisionBanner');
  const decisionText = document.getElementById('decisionText');
  const decisionIndicator = document.getElementById('decisionIndicator');
  const decisionMetrics = document.getElementById('decisionMetrics');
  const metricModel = document.getElementById('metricModel');
  const metricCache = document.getElementById('metricCache');
  const metricLatency = document.getElementById('metricLatency');
  const outputContent = document.getElementById('outputContent');
  const outputStatus = document.getElementById('outputStatus');
  const btnCopy = document.getElementById('btnCopy');
  const redisKeysVal = document.getElementById('redisKeysVal');
  const hudCostSaved = document.getElementById('hudCostSaved');
  const agentItems = document.querySelectorAll('.agent-item');

  let currentMode = 'auto';
  let selectedAgent = 'local-coordinator';

  // Mode Selection
  tabButtons.forEach(tab => {
    tab.addEventListener('click', () => {
      tabButtons.forEach(t => t.classList.remove('active'));
      tab.classList.add('active');
      currentMode = tab.dataset.mode;
      updateDecisionBannerForMode(currentMode);
    });
  });

  function updateDecisionBannerForMode(mode) {
    decisionMetrics.style.display = 'none';
    if (mode === 'auto') {
      decisionIndicator.textContent = '✨';
      decisionText.textContent = 'Auto-Mode Active: Dynamically routes DeepSeek for deep logic vs Gemini for high-throughput speed, caching responses in Redis.';
    } else if (mode === 'deepseek') {
      decisionIndicator.textContent = '🧠';
      decisionText.textContent = 'Manual Tier: DeepSeek Reasoner engaged for complex logic, strategic blueprints, and deep audits.';
    } else if (mode === 'gemini') {
      decisionIndicator.textContent = '⚡';
      decisionText.textContent = 'Manual Tier: Gemini 3.8 Flash engaged for rapid synthesis, high context processing, and executive memos.';
    } else if (mode === 'openclaw') {
      decisionIndicator.textContent = '🧭';
      decisionText.textContent = 'Multi-Agent Swarm: Dispatching directly to Chief of Staff & OpenClaw background agents.';
    }
  }

  // Quick Presets
  presetChips.forEach(chip => {
    chip.addEventListener('click', () => {
      executivePrompt.value = chip.dataset.prompt;
      executivePrompt.focus();
    });
  });

  // Keyboard shortcut: Ctrl + Enter / Cmd + Enter dispatches
  executivePrompt.addEventListener('keydown', (e) => {
    if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') {
      e.preventDefault();
      dispatchCommand();
    }
  });

  btnDispatch.addEventListener('click', dispatchCommand);

  // Agent Roster Click: focus agent
  agentItems.forEach(item => {
    item.addEventListener('click', () => {
      const agentId = item.dataset.agent;
      selectedAgent = agentId;
      tabButtons.forEach(t => t.classList.remove('active'));
      const openclawTab = document.getElementById('tabOpenclaw');
      if (openclawTab) openclawTab.classList.add('active');
      currentMode = 'openclaw';
      
      const agentName = item.querySelector('.agent-name').textContent;
      decisionIndicator.textContent = '🤖';
      decisionText.textContent = `Direct Agent Target: Assigned to ${agentName} (${agentId}) for autonomous execution.`;
      executivePrompt.focus();
    });
  });

  // Copy Brief Button
  btnCopy.addEventListener('click', async () => {
    const text = outputContent.innerText;
    if (!text || text.includes('Standing By')) return;
    try {
      await navigator.clipboard.writeText(text);
      const originalText = btnCopy.textContent;
      btnCopy.textContent = '✓ Copied!';
      setTimeout(() => { btnCopy.textContent = originalText; }, 2000);
    } catch (err) {
      console.error('Clipboard copy failed', err);
    }
  });

  // Main Command Dispatcher
  async function dispatchCommand() {
    const prompt = executivePrompt.value.trim();
    if (!prompt) {
      executivePrompt.focus();
      return;
    }

    setLoading(true);

    try {
      if (currentMode === 'openclaw') {
        await dispatchToOpenClaw(prompt);
      } else {
        await dispatchToLiteLLM(prompt);
      }
    } catch (err) {
      displayError(err.message || 'Operational communication error occurred');
    } finally {
      setLoading(false);
      refreshHealth();
    }
  }

  async function dispatchToLiteLLM(prompt) {
    outputStatus.textContent = 'Routing & Executing...';
    
    const response = await fetch('/api/command', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ prompt, mode: currentMode })
    });

    const data = await response.json();
    if (!response.ok || !data.success) {
      throw new Error(data.error || 'Execution failed');
    }

    // Update Decision Banner
    decisionIndicator.textContent = data.cached ? '💾' : (data.provider === 'DeepSeek' ? '🧠' : '⚡');
    decisionText.textContent = `${data.routing_reason}`;
    decisionMetrics.style.display = 'flex';
    metricModel.textContent = data.model;
    metricCache.textContent = data.cached ? 'CACHE HIT (0ms cost)' : 'DIRECT INFERENCE';
    metricCache.style.color = data.cached ? '#6EE7B7' : '#94A3B8';
    metricLatency.textContent = `${data.latency_ms} ms`;

    // Render Formatted Output
    renderOutput(data.content);
    outputStatus.textContent = `Completed (${data.latency_ms}ms)`;

    // Refresh Cache Count
    refreshCacheStats();
  }

  async function dispatchToOpenClaw(prompt) {
    outputStatus.textContent = 'Dispatching to OpenClaw Swarm...';

    const response = await fetch('/api/openclaw/dispatch', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ message: prompt, agent_id: selectedAgent })
    });

    const data = await response.json();
    if (!response.ok || !data.success) {
      throw new Error(data.error || 'OpenClaw execution failed');
    }

    decisionIndicator.textContent = '🧭';
    decisionText.textContent = `Autonomous execution completed by OpenClaw Agent [${data.agent_id}].`;
    decisionMetrics.style.display = 'flex';
    metricModel.textContent = `OpenClaw: ${data.agent_id}`;
    metricCache.textContent = 'SWARM COMPLETE';
    metricLatency.textContent = `${data.latency_ms} ms`;

    renderOutput(data.reply);
    outputStatus.textContent = `Agent Completed (${data.latency_ms}ms)`;
  }

  function renderOutput(content) {
    outputContent.innerHTML = '';
    const formatted = formatExecutiveMarkdown(content);
    outputContent.innerHTML = formatted;
  }

  function formatExecutiveMarkdown(text) {
    if (!text) return '<p>No content generated.</p>';
    
    let html = text
      // Bold headers
      .replace(/^### (.*$)/gim, '<h4 style="color:#67E8F9;margin:16px 0 8px;font-size:15px;font-weight:700;">$1</h4>')
      .replace(/^## (.*$)/gim, '<h3 style="color:#FFFFFF;margin:20px 0 10px;font-size:17px;font-weight:700;border-bottom:1px solid rgba(255,255,255,0.08);padding-bottom:6px;">$1</h3>')
      .replace(/^# (.*$)/gim, '<h2 style="color:#FFFFFF;margin:24px 0 12px;font-size:19px;font-weight:700;">$1</h2>')
      // Bold highlights
      .replace(/\*\*(.*?)\*\*/g, '<strong style="color:#F1F5F9;font-weight:600;">$1</strong>')
      // Bullet items
      .replace(/^\s*[-*]\s(.*$)/gim, '<li style="margin-left:20px;margin-bottom:6px;color:#CBD5E1;">$1</li>')
      // Numbered items
      .replace(/^\s*\d+\.\s(.*$)/gim, '<li style="margin-left:20px;margin-bottom:6px;color:#CBD5E1;">$1</li>')
      // Paragraphs
      .replace(/\n\n/g, '<br><br>');

    return `<div class="formatted-brief">${html}</div>`;
  }

  function displayError(errMsg) {
    outputContent.innerHTML = `
      <div style="padding:20px;background:rgba(244,63,94,0.1);border:1px solid rgba(244,63,94,0.3);border-radius:12px;color:#FDA4AF;">
        <strong style="display:block;margin-bottom:6px;font-size:15px;">⚠️ Operational Exception</strong>
        <span>${errMsg}</span>
      </div>
    `;
    outputStatus.textContent = 'Error Encountered';
  }

  function setLoading(isLoading) {
    btnDispatch.disabled = isLoading;
    if (isLoading) {
      btnDispatch.querySelector('.btn-text').textContent = 'Processing Directive...';
      btnDispatch.querySelector('.btn-icon').textContent = '⏳';
      outputStatus.textContent = 'Executing...';
    } else {
      btnDispatch.querySelector('.btn-text').textContent = 'Execute Directive';
      btnDispatch.querySelector('.btn-icon').textContent = '➔';
    }
  }

  // Periodic Telemetry Refresh
  async function refreshHealth() {
    try {
      const res = await fetch('/api/health');
      if (!res.ok) return;
      const data = await res.json();
      
      const s = data.services;
      updateChip('chipLitellm', s.litellm.online);
      updateChip('chipOpenclaw', s.openclaw.online);
      updateChip('chipRedis', s.redis_cache.online);
      updateChip('chipQdrant', s.qdrant_memory.online);

      if (s.redis_cache && s.redis_cache.keys !== undefined) {
        redisKeysVal.textContent = `${s.redis_cache.keys} Cached`;
      }
    } catch (err) {
      console.warn('Health ping failed', err);
    }
  }

  async function refreshCacheStats() {
    try {
      const res = await fetch('/api/cache-stats');
      if (!res.ok) return;
      const data = await res.json();
      if (data.cachedKeys) {
        redisKeysVal.textContent = `${data.cachedKeys} Cached`;
      }
      if (data.estimatedCostSaved) {
        hudCostSaved.textContent = data.estimatedCostSaved;
      }
    } catch (err) {
      console.warn('Cache stats failed', err);
    }
  }

  function updateChip(chipId, isOnline) {
    const chip = document.getElementById(chipId);
    if (!chip) return;
    const dot = chip.querySelector('.chip-dot');
    if (dot) {
      dot.className = isOnline ? 'chip-dot dot-green' : 'chip-dot dot-rose';
      if (!isOnline) {
        dot.style.backgroundColor = '#F43F5E';
        dot.style.boxShadow = '0 0 8px rgba(244,63,94,0.6)';
      }
    }
  }

  // Initial runs
  refreshHealth();
  refreshCacheStats();
  setInterval(refreshHealth, 10000);
});
