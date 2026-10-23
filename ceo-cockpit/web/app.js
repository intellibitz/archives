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
  const hudHitRate = document.getElementById('hudHitRate');
  const hudSpeedup = document.getElementById('hudSpeedup');
  const agentItems = document.querySelectorAll('.agent-item');

  // Orchestration Harness Controls
  const selectModel = document.getElementById('selectModel');
  const selectEffort = document.getElementById('selectEffort');
  const selectAutonomy = document.getElementById('selectAutonomy');
  const selectTemperature = document.getElementById('selectTemperature');
  const metricEffort = document.getElementById('metricEffort');
  const metricWorkspace = document.getElementById('metricWorkspace');

  // Workspace Harness Elements
  const activeWorkspacePath = document.getElementById('activeWorkspacePath');
  const wsBranchBadge = document.getElementById('wsBranchBadge');
  const wsItemsBadge = document.getElementById('wsItemsBadge');
  const btnToggleWsContext = document.getElementById('btnToggleWsContext');
  const btnExploreWs = document.getElementById('btnExploreWs');
  const btnSwitchWs = document.getElementById('btnSwitchWs');
  const workspaceDrawer = document.getElementById('workspaceDrawer');
  const btnCloseWsDrawer = document.getElementById('btnCloseWsDrawer');
  const inputCustomWsPath = document.getElementById('inputCustomWsPath');
  const btnApplyWsPath = document.getElementById('btnApplyWsPath');
  const wsRecentsChips = document.getElementById('wsRecentsChips');
  const workspaceFilesDrawer = document.getElementById('workspaceFilesDrawer');
  const btnCloseWsFilesDrawer = document.getElementById('btnCloseWsFilesDrawer');
  const wsSnapshotName = document.getElementById('wsSnapshotName');
  const wsFilesList = document.getElementById('wsFilesList');

  // Frontier Chain of Thought Accordion Elements
  const thinkingAccordion = document.getElementById('thinkingAccordion');
  const thinkingHeader = document.getElementById('thinkingHeader');
  const btnToggleThinking = document.getElementById('btnToggleThinking');
  const thinkingBody = document.getElementById('thinkingBody');
  const thinkingText = document.getElementById('thinkingText');
  const thinkingBadge = document.getElementById('thinkingBadge');

  // Budget Governor & Circuit-Breaker Elements
  const circuitBreakerBanner = document.getElementById('circuitBreakerBanner');
  const cbIcon = document.getElementById('cbIcon');
  const cbTitle = document.getElementById('cbTitle');
  const cbSubtitle = document.getElementById('cbSubtitle');
  const btnTripBreaker = document.getElementById('btnTripBreaker');
  const btnResetBreaker = document.getElementById('btnResetBreaker');
  const hudBreakerBadge = document.getElementById('hudBreakerBadge');
  const progDailyText = document.getElementById('progDailyText');
  const progDailyBar = document.getElementById('progDailyBar');
  const progMonthlyText = document.getElementById('progMonthlyText');
  const progMonthlyBar = document.getElementById('progMonthlyBar');
  const capButtons = document.querySelectorAll('.cap-btn');

  let currentMode = 'auto';
  let selectedAgent = 'local-coordinator';
  let activeWorkspace = null;
  let isWsContextAttached = true;
  let isThinkingExpanded = false;

  // ==============================================================================
  // Workspace Management Engine
  // ==============================================================================
  async function fetchWorkspace() {
    try {
      const res = await fetch('/api/workspace');
      if (!res.ok) return;
      const data = await res.json();
      renderWorkspace(data.workspace, data.recents);
    } catch (err) {
      console.warn('Workspace fetch error', err);
    }
  }

  function renderWorkspace(ws, recents) {
    if (!ws) return;
    activeWorkspace = ws;
    if (activeWorkspacePath) {
      activeWorkspacePath.textContent = ws.path;
      activeWorkspacePath.title = ws.path;
    }
    if (wsBranchBadge) {
      wsBranchBadge.textContent = ws.branch && ws.branch !== 'N/A' ? `🌱 ${ws.branch}` : '🌱 main';
    }
    if (wsItemsBadge) {
      wsItemsBadge.textContent = `${ws.total_items || (ws.entries ? ws.entries.length : 0)} items`;
    }
    if (wsSnapshotName) {
      wsSnapshotName.textContent = ws.name;
    }

    // Render recents chips
    if (wsRecentsChips && Array.isArray(recents)) {
      wsRecentsChips.innerHTML = '';
      recents.forEach(p => {
        const chip = document.createElement('button');
        chip.className = 'ws-chip';
        const base = p.split(/[\\/]/).filter(Boolean).pop() || p;
        chip.textContent = `${base} (${p})`;
        chip.title = p;
        chip.addEventListener('click', () => switchWorkspace(p));
        wsRecentsChips.appendChild(chip);
      });
    }

    renderWorkspaceFiles(ws.entries || []);
  }

  function renderWorkspaceFiles(entries) {
    if (!wsFilesList) return;
    wsFilesList.innerHTML = '';
    if (!entries.length) {
      wsFilesList.innerHTML = '<span style="color:#64748B;font-size:12px;">No items found in directory.</span>';
      return;
    }

    entries.slice(0, 48).forEach(entry => {
      const item = document.createElement('div');
      item.className = `ws-file-item ${entry.is_dir ? 'is-dir' : ''}`;
      const icon = entry.is_dir ? '📁' : '📄';
      const sizeStr = entry.is_dir ? '' : ` (${formatBytes(entry.size_bytes)})`;
      item.innerHTML = `<span class="ws-file-icon">${icon}</span><span>${entry.name}${sizeStr}</span>`;
      wsFilesList.appendChild(item);
    });
  }

  function formatBytes(bytes) {
    if (!bytes || bytes <= 0) return '0 B';
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1048576) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / 1048576).toFixed(1)} MB`;
  }

  async function switchWorkspace(newPath) {
    if (!newPath) return;
    try {
      const res = await fetch('/api/workspace', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ path: newPath })
      });
      const data = await res.json();
      if (!res.ok || !data.success) {
        alert(data.error || 'Failed to switch workspace');
        return;
      }
      renderWorkspace(data.workspace, data.recents);
      if (workspaceDrawer) workspaceDrawer.style.display = 'none';
    } catch (err) {
      alert(`Workspace error: ${err.message}`);
    }
  }

  // Workspace Context Toggle
  if (btnToggleWsContext) {
    btnToggleWsContext.addEventListener('click', () => {
      isWsContextAttached = !isWsContextAttached;
      btnToggleWsContext.classList.toggle('active', isWsContextAttached);
      const icon = btnToggleWsContext.querySelector('.ws-btn-icon');
      const text = btnToggleWsContext.querySelector('.ws-btn-text');
      if (icon) icon.textContent = isWsContextAttached ? '📎' : '❌';
      if (text) text.textContent = isWsContextAttached ? 'Context Attached' : 'Context Detached';
    });
  }

  // Workspace Switcher Drawer Toggle
  if (btnSwitchWs) {
    btnSwitchWs.addEventListener('click', () => {
      if (!workspaceDrawer) return;
      const isOpen = workspaceDrawer.style.display !== 'none';
      workspaceDrawer.style.display = isOpen ? 'none' : 'block';
      if (workspaceFilesDrawer) workspaceFilesDrawer.style.display = 'none';
      if (!isOpen && inputCustomWsPath && activeWorkspace) {
        inputCustomWsPath.value = activeWorkspace.path;
        inputCustomWsPath.focus();
      }
    });
  }

  if (btnCloseWsDrawer && workspaceDrawer) {
    btnCloseWsDrawer.addEventListener('click', () => {
      workspaceDrawer.style.display = 'none';
    });
  }

  if (btnApplyWsPath && inputCustomWsPath) {
    btnApplyWsPath.addEventListener('click', () => {
      switchWorkspace(inputCustomWsPath.value.trim());
    });
    inputCustomWsPath.addEventListener('keydown', (e) => {
      if (e.key === 'Enter') switchWorkspace(inputCustomWsPath.value.trim());
    });
  }

  // Workspace Files Preview Drawer Toggle
  if (btnExploreWs) {
    btnExploreWs.addEventListener('click', () => {
      if (!workspaceFilesDrawer) return;
      const isOpen = workspaceFilesDrawer.style.display !== 'none';
      workspaceFilesDrawer.style.display = isOpen ? 'none' : 'block';
      if (workspaceDrawer) workspaceDrawer.style.display = 'none';
    });
  }

  if (btnCloseWsFilesDrawer && workspaceFilesDrawer) {
    btnCloseWsFilesDrawer.addEventListener('click', () => {
      workspaceFilesDrawer.style.display = 'none';
    });
  }

  // ==============================================================================
  // Models & Harness Deliberation Controls
  // ==============================================================================
  async function fetchModelsList() {
    try {
      const res = await fetch('/api/models');
      if (!res.ok) return;
      const data = await res.json();
      if (!data.models || !selectModel) return;

      const existingValues = new Set(Array.from(selectModel.options).map(o => o.value));
      const group = document.createElement('optgroup');
      group.label = 'Additional Available Models';
      let added = false;

      data.models.forEach(m => {
        if (!existingValues.has(m.id)) {
          const opt = document.createElement('option');
          opt.value = m.id;
          opt.textContent = `${m.is_reasoning ? '🧠 ' : '🤖 '}${m.name}`;
          group.appendChild(opt);
          added = true;
        }
      });
      if (added) selectModel.appendChild(group);
    } catch (err) {
      console.warn('Models fetch error', err);
    }
  }

  // Sync Model Dropdown with Mode Tabs
  if (selectModel) {
    selectModel.addEventListener('change', () => {
      const val = selectModel.value;
      tabButtons.forEach(t => t.classList.remove('active'));
      if (val === 'auto') {
        currentMode = 'auto';
        const tab = document.getElementById('tabAuto');
        if (tab) tab.classList.add('active');
      } else if (val === 'deepseek-reasoner') {
        currentMode = 'deepseek';
        const tab = document.getElementById('tabDeepseek');
        if (tab) tab.classList.add('active');
      } else if (val === 'gemini-flash') {
        currentMode = 'gemini';
        const tab = document.getElementById('tabGemini');
        if (tab) tab.classList.add('active');
      } else {
        currentMode = val;
      }
      updateDecisionBannerForModel(val);
    });
  }

  tabButtons.forEach(tab => {
    tab.addEventListener('click', () => {
      tabButtons.forEach(t => t.classList.remove('active'));
      tab.classList.add('active');
      currentMode = tab.dataset.mode;

      if (selectModel) {
        if (currentMode === 'auto') selectModel.value = 'auto';
        else if (currentMode === 'deepseek') selectModel.value = 'deepseek-reasoner';
        else if (currentMode === 'gemini') selectModel.value = 'gemini-flash';
      }
      updateDecisionBannerForMode(currentMode);
    });
  });

  function updateDecisionBannerForModel(modelVal) {
    decisionMetrics.style.display = 'none';
    if (modelVal === 'auto') {
      decisionIndicator.textContent = '✨';
      decisionText.textContent = 'Auto-Mode Active: Dynamically routes DeepSeek for deep logic vs Gemini for high-throughput speed, caching responses in Redis.';
    } else if (modelVal === 'deepseek-reasoner') {
      decisionIndicator.textContent = '🧠';
      decisionText.textContent = 'Frontier Reasoning: DeepSeek Reasoner (R1) engaged with live multi-stage chain-of-thought verification.';
    } else if (modelVal === 'gemini-flash') {
      decisionIndicator.textContent = '⚡';
      decisionText.textContent = 'Ultra-Speed Tier: Gemini 2.5/3.8 Flash engaged for rapid synthesis and 1M+ token context window.';
    } else {
      decisionIndicator.textContent = '🤖';
      decisionText.textContent = `Harness Model Override: Executing directly on ${modelVal} with executive parameters.`;
    }
  }

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

  // Thinking Accordion Toggle
  function toggleThinkingAccordion() {
    isThinkingExpanded = !isThinkingExpanded;
    if (thinkingBody) thinkingBody.style.display = isThinkingExpanded ? 'block' : 'none';
    if (btnToggleThinking) btnToggleThinking.textContent = isThinkingExpanded ? 'Hide Thoughts ▴' : 'Show Thoughts ▾';
  }

  if (btnToggleThinking) btnToggleThinking.addEventListener('click', (e) => { e.stopPropagation(); toggleThinkingAccordion(); });
  if (thinkingHeader) thinkingHeader.addEventListener('click', toggleThinkingAccordion);

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
    outputStatus.textContent = 'Deliberating & Executing...';

    const payload = {
      prompt,
      model: selectModel ? selectModel.value : 'auto',
      effort: selectEffort ? selectEffort.value : 'auto',
      autonomy_mode: selectAutonomy ? selectAutonomy.value : 'full',
      temperature: selectTemperature ? parseFloat(selectTemperature.value) : 0.3,
      include_workspace_context: isWsContextAttached,
      workspace_path: activeWorkspace ? activeWorkspace.path : null
    };
    
    const response = await fetch('/api/command', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
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
    
    if (metricEffort) {
      metricEffort.style.display = data.effort ? 'inline-block' : 'none';
      metricEffort.textContent = data.effort ? `${data.effort.toUpperCase()} EFFORT` : '';
    }
    if (metricWorkspace && data.workspace) {
      metricWorkspace.style.display = 'inline-block';
      metricWorkspace.textContent = `📁 ${data.workspace.name}`;
      metricWorkspace.title = data.workspace.path || '';
    }

    metricCache.textContent = data.cached ? 'CACHE HIT (0ms cost)' : 'DIRECT INFERENCE';
    metricCache.style.color = data.cached ? '#6EE7B7' : '#94A3B8';
    metricLatency.textContent = `${data.latency_ms} ms`;

    // Handle Frontier Chain of Thought (Live Reasoning Trace)
    if (data.reasoning_content && thinkingAccordion && thinkingText) {
      thinkingAccordion.style.display = 'block';
      thinkingText.textContent = data.reasoning_content;
      if (thinkingBadge) {
        const rTokens = data.usage?.completion_tokens_details?.reasoning_tokens;
        thinkingBadge.textContent = rTokens ? `${rTokens} Reasoning Tokens` : 'Frontier Trace';
      }
    } else if (thinkingAccordion) {
      thinkingAccordion.style.display = 'none';
    }

    // Render Formatted Output
    renderOutput(data.content);
    outputStatus.textContent = `Completed (${data.latency_ms}ms)`;

    // Update Budget Telemetry immediately if returned
    if (data.budget) {
      renderBudget(data.budget);
    }

    // Refresh Cache Count
    refreshCacheStats();
  }

  async function dispatchToOpenClaw(prompt) {
    outputStatus.textContent = 'Dispatching to OpenClaw Swarm...';

    if (thinkingAccordion) thinkingAccordion.style.display = 'none';

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
      if (s.auto_clicker) {
        updateChip('chipAutoClick', s.auto_clicker.online);
        const autoClickVal = document.getElementById('autoClickVal');
        if (autoClickVal) {
          autoClickVal.textContent = s.auto_clicker.online ? 'ACTIVE' : 'OFFLINE';
        }
      }

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

  // ==============================================================================
  // Budget Governor & Circuit-Breaker Engine
  // ==============================================================================
  async function fetchBudget() {
    try {
      const res = await fetch('/api/budget');
      if (!res.ok) return;
      const b = await res.json();
      renderBudget(b);
    } catch (err) {
      console.warn('Budget fetch failed', err);
    }
  }

  function renderBudget(b) {
    if (!b) return;

    // 1. Circuit-Breaker Ribbon State
    if (circuitBreakerBanner) {
      circuitBreakerBanner.className = `circuit-breaker-banner ${b.circuit_breaker_state.toLowerCase()}`;
    }

    if (b.circuit_breaker_state === 'TRIPPED') {
      if (cbIcon) cbIcon.textContent = '🚨';
      if (cbTitle) cbTitle.textContent = 'CIRCUIT BREAKER: TRIPPED (ZERO-COST SAFEGUARD ENFORCED)';
      if (cbSubtitle) cbSubtitle.textContent = b.circuit_breaker_trip_reason || 'Hard spend limit reached. All model routing forced to Zero-Cost Local / Free Tier.';
      if (btnTripBreaker) btnTripBreaker.style.display = 'none';
      if (btnResetBreaker) btnResetBreaker.style.display = 'inline-block';
      if (hudBreakerBadge) {
        hudBreakerBadge.className = 'status-badge tripped';
        hudBreakerBadge.textContent = 'TRIPPED';
      }
    } else if (b.circuit_breaker_state === 'WARNING') {
      if (cbIcon) cbIcon.textContent = '⚠️';
      if (cbTitle) cbTitle.textContent = 'CIRCUIT BREAKER: WARNING (SPEND ELEVATED)';
      if (cbSubtitle) cbSubtitle.textContent = `Warning: ${b.daily_used_pct}% of daily cap utilized ($${b.today_spend_usd.toFixed(4)} of $${b.daily_budget_usd.toFixed(2)}).`;
      if (btnTripBreaker) btnTripBreaker.style.display = 'inline-block';
      if (btnResetBreaker) btnResetBreaker.style.display = 'none';
      if (hudBreakerBadge) {
        hudBreakerBadge.className = 'status-badge warning';
        hudBreakerBadge.textContent = 'WARNING';
      }
    } else {
      if (cbIcon) cbIcon.textContent = '🛡️';
      if (cbTitle) cbTitle.textContent = 'CIRCUIT BREAKER: ARMED (NORMAL HYBRID)';
      if (cbSubtitle) cbSubtitle.textContent = `Hard spend cap active ($${b.daily_budget_usd.toFixed(2)}/day). Auto-fallback to zero-cost safeguard models if cap breached.`;
      if (btnTripBreaker) btnTripBreaker.style.display = 'inline-block';
      if (btnResetBreaker) btnResetBreaker.style.display = 'none';
      if (hudBreakerBadge) {
        hudBreakerBadge.className = 'status-badge green';
        hudBreakerBadge.textContent = 'ARMED';
      }
    }

    // 2. Daily Spend Progress Bar
    if (progDailyText) {
      progDailyText.textContent = `$${b.today_spend_usd.toFixed(4)} / $${b.daily_budget_usd.toFixed(2)} (${b.daily_used_pct}%)`;
    }
    if (progDailyBar) {
      progDailyBar.style.width = `${Math.min(100, b.daily_used_pct)}%`;
      if (b.daily_used_pct >= 100) {
        progDailyBar.className = 'progress-fill fill-red';
      } else if (b.daily_used_pct >= 80) {
        progDailyBar.className = 'progress-fill fill-yellow';
      } else {
        progDailyBar.className = 'progress-fill fill-green';
      }
    }

    // 3. Monthly Spend Progress Bar
    if (progMonthlyText) {
      progMonthlyText.textContent = `$${b.month_spend_usd.toFixed(4)} / $${b.monthly_budget_usd.toFixed(2)} (${b.monthly_used_pct}%)`;
    }
    if (progMonthlyBar) {
      progMonthlyBar.style.width = `${Math.min(100, b.monthly_used_pct)}%`;
    }

    // 4. Savings Telemetry
    if (hudCostSaved) {
      hudCostSaved.textContent = `$${b.cached_savings_usd.toFixed(4)}`;
    }
    if (hudHitRate) {
      hudHitRate.textContent = `${b.cache_hit_rate_pct}% Cache Rate`;
    }

    // 5. Active Cap Button Highlight
    capButtons.forEach(btn => {
      const capVal = parseFloat(btn.dataset.cap);
      btn.classList.toggle('active', Math.abs(capVal - b.daily_budget_usd) < 0.01);
    });
  }

  // Emergency Trip Action
  if (btnTripBreaker) {
    btnTripBreaker.addEventListener('click', async () => {
      try {
        btnTripBreaker.disabled = true;
        const res = await fetch('/api/budget/trip', { method: 'POST' });
        const data = await res.json();
        if (data.budget) renderBudget(data.budget);
      } catch (err) {
        console.error('Trip action failed', err);
      } finally {
        btnTripBreaker.disabled = false;
      }
    });
  }

  // Reset Action
  if (btnResetBreaker) {
    btnResetBreaker.addEventListener('click', async () => {
      try {
        btnResetBreaker.disabled = true;
        const res = await fetch('/api/budget/reset', { method: 'POST' });
        const data = await res.json();
        if (data.budget) renderBudget(data.budget);
      } catch (err) {
        console.error('Reset action failed', err);
      } finally {
        btnResetBreaker.disabled = false;
      }
    });
  }

  // Quick Cap Click Handlers
  capButtons.forEach(btn => {
    btn.addEventListener('click', async () => {
      const newDailyCap = parseFloat(btn.dataset.cap);
      try {
        capButtons.forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        const res = await fetch('/api/budget/config', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ daily_budget_usd: newDailyCap })
        });
        const data = await res.json();
        if (data.budget) renderBudget(data.budget);
      } catch (err) {
        console.error('Failed to update budget cap', err);
      }
    });
  });

  // Initial runs
  fetchWorkspace();
  fetchModelsList();
  refreshHealth();
  refreshCacheStats();
  fetchBudget();

  setInterval(refreshHealth, 10000);
  setInterval(fetchBudget, 5000);
});
