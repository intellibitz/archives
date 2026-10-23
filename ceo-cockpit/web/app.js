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

  // Header Actions & Session Elements
  const btnToggleSessions = document.getElementById('btnToggleSessions');
  const btnNewSession = document.getElementById('btnNewSession');
  const btnOpenCommandPalette = document.getElementById('btnOpenCommandPalette');
  const sessionCountBadge = document.getElementById('sessionCountBadge');

  // Sessions Drawer Elements
  const sessionsDrawer = document.getElementById('sessionsDrawer');
  const btnCloseSessionsDrawer = document.getElementById('btnCloseSessionsDrawer');
  const btnDrawerNewSession = document.getElementById('btnDrawerNewSession');
  const inputFilterSessions = document.getElementById('inputFilterSessions');
  const sessionsListContainer = document.getElementById('sessionsListContainer');

  // Slash Commands & Action Elements
  const slashMenu = document.getElementById('slashMenu');
  const slashOptionsList = document.getElementById('slashOptionsList');
  const btnHaltStream = document.getElementById('btnHaltStream');
  const btnExportReport = document.getElementById('btnExportReport');
  const conversationTimeline = document.getElementById('conversationTimeline');
  const outputPlaceholder = document.getElementById('outputPlaceholder');
  const outputThreadTitle = document.getElementById('outputThreadTitle');
  const outputSessionBadge = document.getElementById('outputSessionBadge');

  // Streaming Turn Card Elements
  const streamingTurnCard = document.getElementById('streamingTurnCard');
  const streamingModelPill = document.getElementById('streamingModelPill');
  const streamingVelocity = document.getElementById('streamingVelocity');
  const streamingThinkingAccordion = document.getElementById('streamingThinkingAccordion');
  const streamingThinkingHeader = document.getElementById('streamingThinkingHeader');
  const streamingThinkingBody = document.getElementById('streamingThinkingBody');
  const streamingThinkingText = document.getElementById('streamingThinkingText');
  const streamingThinkingBadge = document.getElementById('streamingThinkingBadge');
  const btnToggleStreamingThinking = document.getElementById('btnToggleStreamingThinking');
  const streamingContentText = document.getElementById('streamingContentText');
  const typewriterCursor = document.getElementById('typewriterCursor');

  // Command Palette Elements
  const commandPaletteBackdrop = document.getElementById('commandPaletteBackdrop');
  const commandPaletteModal = document.getElementById('commandPaletteModal');
  const cpSearchInput = document.getElementById('cpSearchInput');
  const cpOptionsList = document.getElementById('cpOptionsList');

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

  // Model Pricing & Penny Matrix Elements
  const btnOpenPricingModal = document.getElementById('btnOpenPricingModal');
  const btnClosePricingModal = document.getElementById('btnClosePricingModal');
  const btnRefreshPricingMatrix = document.getElementById('btnRefreshPricingMatrix');
  const modelPricingBackdrop = document.getElementById('modelPricingBackdrop');
  const modelCostBadge = document.getElementById('modelCostBadge');
  const modelCostDot = document.getElementById('modelCostDot');
  const modelCostText = document.getElementById('modelCostText');
  const promptPennyMeter = document.getElementById('promptPennyMeter');
  const pennyEstimatedTokens = document.getElementById('pennyEstimatedTokens');
  const pennyEstimatedCost = document.getElementById('pennyEstimatedCost');
  const pennyRemainingBudget = document.getElementById('pennyRemainingBudget');
  const btnOpenPricingFromMeter = document.getElementById('btnOpenPricingFromMeter');
  const pricingModalActiveCount = document.getElementById('pricingModalActiveCount');
  const pricingModalTotalCount = document.getElementById('pricingModalTotalCount');
  const pricingModalTodaySpend = document.getElementById('pricingModalTodaySpend');
  const pricingModalRemainingDaily = document.getElementById('pricingModalRemainingDaily');
  const pricingModalCacheSaved = document.getElementById('pricingModalCacheSaved');
  const pricingModalBreakerState = document.getElementById('pricingModalBreakerState');
  const simInputTokens = document.getElementById('simInputTokens');
  const simOutputTokens = document.getElementById('simOutputTokens');
  const pricingMatrixTableBody = document.getElementById('pricingMatrixTableBody');
  const calcPresetChips = document.querySelectorAll('.calc-preset-chip');
  const customModelRow = document.getElementById('customModelRow');
  const inputCustomModel = document.getElementById('inputCustomModel');
  const btnApplyCustomModel = document.getElementById('btnApplyCustomModel');
  const btnCancelCustomModel = document.getElementById('btnCancelCustomModel');

  let allModelsList = [];
  let allModelsPricingMap = {};
  let currentMode = 'auto';
  let selectedAgent = 'local-coordinator';
  let activeWorkspace = null;
  let isWsContextAttached = true;
  let isThinkingExpanded = false;
  let isStreamingThinkingExpanded = false;
  let activeSessionId = null;
  let activeSession = null;
  let allSessions = [];
  let activeStreamAbortController = null;
  let activeSlashIndex = 0;
  let activeCpIndex = 0;

  // Round 2 DOM Elements: Pinned Files, File Viewer, Auto-Decisions & Tree Explorer
  const pinnedFilesBar = document.getElementById('pinnedFilesBar');
  const pinnedChipsContainer = document.getElementById('pinnedChipsContainer');
  const pinnedTokensBadge = document.getElementById('pinnedTokensBadge');
  const btnClearPinned = document.getElementById('btnClearPinned');

  const fileViewerBackdrop = document.getElementById('fileViewerBackdrop');
  const fileViewerTitle = document.getElementById('fileViewerTitle');
  const fileViewerMeta = document.getElementById('fileViewerMeta');
  const fileViewerCodeBlock = document.getElementById('fileViewerCodeBlock');
  const btnPinViewerFile = document.getElementById('btnPinViewerFile');
  const btnCopyViewerFile = document.getElementById('btnCopyViewerFile');
  const btnCloseFileViewer = document.getElementById('btnCloseFileViewer');

  const chipAutoClick = document.getElementById('chipAutoClick');
  const autoClickerBackdrop = document.getElementById('autoClickerBackdrop');
  const btnCloseAutoClickerModal = document.getElementById('btnCloseAutoClickerModal');
  const btnRestartAutoClicker = document.getElementById('btnRestartAutoClicker');
  const acModalStatus = document.getElementById('acModalStatus');
  const acModalMode = document.getElementById('acModalMode');
  const acModalPid = document.getElementById('acModalPid');
  const acModalHeartbeat = document.getElementById('acModalHeartbeat');
  const acModalTotalClicks = document.getElementById('acModalTotalClicks');
  const acActionsTableBody = document.getElementById('acActionsTableBody');
  const acLogTailBlock = document.getElementById('acLogTailBlock');

  const inputFilterWsFiles = document.getElementById('inputFilterWsFiles');
  const btnRefreshWsTree = document.getElementById('btnRefreshWsTree');

  // Round 2 State
  let pinnedFiles = [];
  let currentViewingFile = null;
  let workspaceTreeData = null;

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

    fetchWorkspaceTree();
  }

  function formatBytes(bytes) {
    if (!bytes || bytes <= 0) return '0 B';
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1048576) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / 1048576).toFixed(1)} MB`;
  }

  // ==============================================================================
  // Pinned Context Files Engine
  // ==============================================================================
  function renderPinnedFilesBar() {
    if (!pinnedFilesBar || !pinnedChipsContainer) return;
    if (pinnedFiles.length === 0) {
      pinnedFilesBar.style.display = 'none';
      return;
    }

    pinnedFilesBar.style.display = 'flex';
    pinnedChipsContainer.innerHTML = '';

    let totalTokens = 0;
    pinnedFiles.forEach(file => {
      totalTokens += file.token_estimate || 0;
      const chip = document.createElement('div');
      chip.className = 'pinned-chip';
      chip.innerHTML = `
        <span>📄</span>
        <span>${escapeHtml(file.name)}</span>
        <span class="pinned-chip-tokens">~${file.token_estimate} tok</span>
        <button class="pinned-chip-remove" title="Unpin ${escapeHtml(file.name)}">✕</button>
      `;
      chip.querySelector('.pinned-chip-remove').addEventListener('click', (e) => {
        e.stopPropagation();
        unpinFile(file.path);
      });
      chip.addEventListener('click', () => {
        openFileViewer(file.path);
      });
      pinnedChipsContainer.appendChild(chip);
    });

    if (pinnedTokensBadge) {
      pinnedTokensBadge.textContent = `${totalTokens.toLocaleString()} tokens attached`;
    }
  }

  function pinFile(fileData) {
    if (!fileData || !fileData.path) return;
    const exists = pinnedFiles.some(f => f.path === fileData.path);
    if (!exists) {
      pinnedFiles.push(fileData);
      renderPinnedFilesBar();
      renderFileTree();
    }
  }

  function unpinFile(filePath) {
    pinnedFiles = pinnedFiles.filter(f => f.path !== filePath);
    renderPinnedFilesBar();
    renderFileTree();
  }

  function clearPinnedFiles() {
    pinnedFiles = [];
    renderPinnedFilesBar();
    renderFileTree();
  }

  if (btnClearPinned) {
    btnClearPinned.addEventListener('click', clearPinnedFiles);
  }

  // ==============================================================================
  // Hierarchical File Explorer & File Viewer
  // ==============================================================================
  async function fetchWorkspaceTree() {
    try {
      const res = await fetch('/api/workspace/tree');
      if (!res.ok) return;
      const data = await res.json();
      workspaceTreeData = data.tree;
      renderFileTree();
    } catch (err) {
      console.warn('Workspace tree fetch failed', err);
    }
  }

  function renderFileTree() {
    if (!wsFilesList || !workspaceTreeData) return;
    wsFilesList.innerHTML = '';
    const filterText = (inputFilterWsFiles?.value || '').trim().toLowerCase();

    const container = document.createElement('div');
    container.className = 'ws-tree-container';

    function traverse(node, depth = 0) {
      if (!node) return;
      const matches = !filterText || node.name.toLowerCase().includes(filterText) || node.path.toLowerCase().includes(filterText);

      let hasMatchingChild = false;
      if (node.is_dir && node.children) {
        hasMatchingChild = node.children.some(c => matchesFilter(c, filterText));
      }

      if (matches || hasMatchingChild) {
        if (depth > 0) {
          const item = document.createElement('div');
          item.className = 'tree-node-item';

          const left = document.createElement('div');
          left.className = 'tree-node-left';
          
          let indents = '';
          for (let i = 1; i < depth; i++) {
            indents += '<span class="tree-indent"></span>';
          }

          const icon = node.is_dir ? '📁' : '📄';
          left.innerHTML = `${indents}<span class="tree-icon">${icon}</span><span class="tree-name">${escapeHtml(node.name)}</span>`;

          const right = document.createElement('div');
          right.className = 'tree-node-right';

          if (!node.is_dir) {
            const sizeStr = formatBytes(node.size_bytes);
            const isPinned = pinnedFiles.some(f => f.path === node.path || f.path === node.full_path);
            right.innerHTML = `
              <span class="tree-size">${sizeStr}</span>
              <button class="tree-action-btn tree-btn-view" title="Preview file">👁️ View</button>
              <button class="tree-action-btn tree-btn-pin ${isPinned ? 'pinned' : ''}" title="${isPinned ? 'Unpin' : 'Pin to Context'}">${isPinned ? '📌 Pinned' : '+ Pin'}</button>
            `;

            right.querySelector('.tree-btn-view').addEventListener('click', (e) => {
              e.stopPropagation();
              openFileViewer(node.path);
            });

            right.querySelector('.tree-btn-pin').addEventListener('click', async (e) => {
              e.stopPropagation();
              if (isPinned) {
                unpinFile(node.path);
              } else {
                const res = await fetch(`/api/workspace/file?path=${encodeURIComponent(node.path)}`);
                if (res.ok) {
                  const fData = await res.json();
                  if (fData.file) pinFile(fData.file);
                }
              }
            });
          } else {
            const count = node.children ? node.children.length : 0;
            right.innerHTML = `<span class="tree-size">${count} items</span>`;
          }

          item.appendChild(left);
          item.appendChild(right);
          container.appendChild(item);
        }

        if (node.is_dir && node.children) {
          node.children.forEach(child => traverse(child, depth + 1));
        }
      }
    }

    function matchesFilter(n, q) {
      if (!q) return true;
      if (n.name.toLowerCase().includes(q) || n.path.toLowerCase().includes(q)) return true;
      if (n.is_dir && n.children) {
        return n.children.some(c => matchesFilter(c, q));
      }
      return false;
    }

    traverse(workspaceTreeData, 0);
    wsFilesList.appendChild(container);
  }

  if (inputFilterWsFiles) {
    inputFilterWsFiles.addEventListener('input', () => {
      renderFileTree();
    });
  }

  if (btnRefreshWsTree) {
    btnRefreshWsTree.addEventListener('click', () => {
      fetchWorkspaceTree();
    });
  }

  // ==============================================================================
  // Executive File Viewer Modal
  // ==============================================================================
  async function openFileViewer(filePath) {
    if (!filePath) return;
    try {
      const res = await fetch(`/api/workspace/file?path=${encodeURIComponent(filePath)}`);
      if (!res.ok) {
        alert('Could not read file from workspace.');
        return;
      }
      const data = await res.json();
      if (!data.file) return;

      currentViewingFile = data.file;
      if (fileViewerTitle) fileViewerTitle.textContent = currentViewingFile.name;
      if (fileViewerMeta) {
        const sizeStr = formatBytes(currentViewingFile.size_bytes);
        fileViewerMeta.textContent = `${currentViewingFile.lines || 0} lines • ${sizeStr} • ~${currentViewingFile.token_estimate || 0} tokens`;
      }
      if (fileViewerCodeBlock) {
        fileViewerCodeBlock.textContent = currentViewingFile.content || '';
      }

      if (btnPinViewerFile) {
        const isPinned = pinnedFiles.some(f => f.path === currentViewingFile.path);
        btnPinViewerFile.textContent = isPinned ? '📌 Already Pinned' : '📌 Pin to Context';
      }

      if (fileViewerBackdrop) fileViewerBackdrop.style.display = 'flex';
    } catch (err) {
      alert(`Error viewing file: ${err.message}`);
    }
  }

  function closeFileViewer() {
    if (fileViewerBackdrop) fileViewerBackdrop.style.display = 'none';
    currentViewingFile = null;
  }

  if (btnCloseFileViewer) btnCloseFileViewer.addEventListener('click', closeFileViewer);
  if (fileViewerBackdrop) {
    fileViewerBackdrop.addEventListener('click', (e) => {
      if (e.target === fileViewerBackdrop) closeFileViewer();
    });
  }

  if (btnPinViewerFile) {
    btnPinViewerFile.addEventListener('click', () => {
      if (currentViewingFile) {
        pinFile(currentViewingFile);
        closeFileViewer();
      }
    });
  }

  if (btnCopyViewerFile) {
    btnCopyViewerFile.addEventListener('click', () => {
      if (currentViewingFile && currentViewingFile.content) {
        navigator.clipboard.writeText(currentViewingFile.content);
        btnCopyViewerFile.textContent = '✓ Copied';
        setTimeout(() => { btnCopyViewerFile.textContent = '📋 Copy'; }, 2000);
      }
    });
  }

  // ==============================================================================
  // Decision Auto-Clicker Mission Control Modal
  // ==============================================================================
  async function openAutoClickerModal() {
    try {
      const res = await fetch('/api/autoclicker/telemetry');
      if (!res.ok) return;
      const data = await res.json();

      if (acModalStatus) {
        acModalStatus.textContent = data.online ? 'ACTIVE' : 'IDLE / RESTARTING';
        acModalStatus.className = `stat-value ${data.online ? 'text-green' : 'text-red'}`;
      }
      if (acModalMode) acModalMode.textContent = data.mode || 'Low-Latency';
      if (acModalPid) acModalPid.textContent = data.pid ? `PID ${data.pid}` : 'PID N/A';
      if (acModalHeartbeat) acModalHeartbeat.textContent = `Heartbeat: ${data.heartbeat_age_sec}s ago`;
      if (acModalTotalClicks) acModalTotalClicks.textContent = (data.total_clicks || 0).toLocaleString();

      if (acActionsTableBody) {
        acActionsTableBody.innerHTML = '';
        if (data.recent_actions && data.recent_actions.length > 0) {
          data.recent_actions.forEach(act => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
              <td style="font-family:var(--font-mono);font-size:11px;color:#94A3B8;">${escapeHtml(act.timestamp)}</td>
              <td><span class="ac-btn-tag">${escapeHtml(act.button)}</span></td>
              <td style="font-family:var(--font-mono);font-size:10.5px;color:#64748B;">${escapeHtml(act.raw)}</td>
            `;
            acActionsTableBody.appendChild(tr);
          });
        } else {
          acActionsTableBody.innerHTML = '<tr><td colspan="3" style="text-align:center;color:#64748B;padding:12px;">No dialog approvals recorded in current window.</td></tr>';
        }
      }

      if (acLogTailBlock) {
        acLogTailBlock.textContent = (data.recent_log_lines || []).join('\n');
      }

      if (autoClickerBackdrop) autoClickerBackdrop.style.display = 'flex';
    } catch (err) {
      console.warn('Auto-clicker telemetry error', err);
    }
  }

  function closeAutoClickerModal() {
    if (autoClickerBackdrop) autoClickerBackdrop.style.display = 'none';
  }

  if (chipAutoClick) chipAutoClick.addEventListener('click', openAutoClickerModal);
  if (btnCloseAutoClickerModal) btnCloseAutoClickerModal.addEventListener('click', closeAutoClickerModal);
  if (autoClickerBackdrop) {
    autoClickerBackdrop.addEventListener('click', (e) => {
      if (e.target === autoClickerBackdrop) closeAutoClickerModal();
    });
  }

  if (btnRestartAutoClicker) {
    btnRestartAutoClicker.addEventListener('click', async () => {
      btnRestartAutoClicker.textContent = '🔄 Restarting...';
      try {
        await fetch('/api/autoclicker/action', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ action: 'restart' })
        });
        setTimeout(async () => {
          await openAutoClickerModal();
          btnRestartAutoClicker.textContent = '🔄 Restart Process';
        }, 1500);
      } catch (err) {
        btnRestartAutoClicker.textContent = '🔄 Restart Process';
      }
    });
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
      if (!isOpen) {
        fetchWorkspaceTree();
      }
    });
  }

  if (btnCloseWsFilesDrawer && workspaceFilesDrawer) {
    btnCloseWsFilesDrawer.addEventListener('click', () => {
      workspaceFilesDrawer.style.display = 'none';
    });
  }

  // ==============================================================================
  // Models & Harness Deliberation Controls (Budget-Conscious & Pricing-Aware)
  // ==============================================================================
  const KNOWN_MODEL_REGISTRY = {
    'deepseek-reasoner': { name: 'DeepSeek Reasoner (R1)', provider: 'DeepSeek', prompt: 0.55, completion: 2.19, icon: '🧠', tier: 'Low ($$)', context: '128k', is_reasoning: true, group: 'reasoning' },
    'deepseek-chat': { name: 'DeepSeek Chat (V3)', provider: 'DeepSeek', prompt: 0.14, completion: 0.28, icon: '⚡', tier: 'Ultra-Low ($)', context: '128k', group: 'speed' },
    'gemini-flash': { name: 'Gemini 2.5/3.8 Flash (1M+)', provider: 'Google AI Studio', prompt: 0.075, completion: 0.30, icon: '⚡', tier: 'Ultra-Low ($)', context: '1M+', is_safeguard: true, group: 'speed' },
    'gemini-pro': { name: 'Gemini Pro (Frontier)', provider: 'Google AI Studio', prompt: 1.25, completion: 5.00, icon: '💎', tier: 'Moderate ($$$)', context: '1M+', group: 'multimodal' },
    'claude-3-5-sonnet': { name: 'Claude 3.5 Sonnet', provider: 'Anthropic', prompt: 3.00, completion: 15.00, icon: '🎭', tier: 'Premium ($$$$)', context: '200k', group: 'multimodal' },
    'claude-3-5-haiku': { name: 'Claude 3.5 Haiku', provider: 'Anthropic', prompt: 0.80, completion: 4.00, icon: '🍃', tier: 'Moderate ($$$)', context: '200k', group: 'multimodal' },
    'siliconflow-qwen-coder': { name: 'Qwen 2.5 Coder 32B (SiliconFlow)', provider: 'SiliconFlow', prompt: 0.20, completion: 0.40, icon: '💻', tier: 'Ultra-Low ($)', context: '128k', group: 'code' },
    'siliconflow-qwen': { name: 'Qwen 2.5 72B Instruct (SiliconFlow)', provider: 'SiliconFlow', prompt: 0.20, completion: 0.40, icon: '🌐', tier: 'Ultra-Low ($)', context: '128k', group: 'generalist' },
    'siliconflow-deepseek': { name: 'DeepSeek V3 (SiliconFlow)', provider: 'SiliconFlow', prompt: 0.14, completion: 0.28, icon: '⚡', tier: 'Ultra-Low ($)', context: '64k', group: 'speed' },
    'dashscope-qwen-max': { name: 'DashScope Qwen Max', provider: 'Alibaba DashScope', prompt: 1.60, completion: 6.40, icon: '👑', tier: 'Moderate ($$$)', context: '32k', is_reasoning: true, group: 'reasoning' },
    'dashscope-qwen-plus': { name: 'DashScope Qwen Plus', provider: 'Alibaba DashScope', prompt: 0.12, completion: 0.24, icon: '🌐', tier: 'Ultra-Low ($)', context: '128k', group: 'speed' },
    'dashscope-qwen-coder': { name: 'DashScope Qwen Coder', provider: 'Alibaba DashScope', prompt: 0.12, completion: 0.24, icon: '💻', tier: 'Ultra-Low ($)', context: '128k', group: 'code' },
    'local-safeguard': { name: 'Zero-Cost Local Safeguard', provider: 'Local / OpenClaw', prompt: 0.0, completion: 0.0, icon: '🛡️', tier: 'Free ($0.00)', context: '32k', is_safeguard: true, group: 'safeguard' }
  };

  async function fetchModelsList() {
    let serverModels = [];
    try {
      const res = await fetch('/api/models');
      if (res.ok) {
        const data = await res.json();
        if (data.models && Array.isArray(data.models)) {
          serverModels = data.models;
        }
      }
    } catch (err) {
      console.warn('Models fetch fallback to known registry', err);
    }

    // Populate modelMap seeded with complete registry
    const modelMap = new Map();
    Object.keys(KNOWN_MODEL_REGISTRY).forEach(id => {
      const k = KNOWN_MODEL_REGISTRY[id];
      modelMap.set(id, {
        id,
        name: k.name,
        provider: k.provider,
        cost_tier: k.tier,
        context_window: k.context,
        prompt_price_per_m: k.prompt,
        completion_price_per_m: k.completion,
        cached_price_per_m: k.prompt * 0.2,
        is_reasoning: Boolean(k.is_reasoning),
        is_safeguard: Boolean(k.is_safeguard),
        icon: k.icon,
        group: k.group,
        status: 'active',
        price_display: k.prompt === 0 ? 'Free ($0.000)' : `$${k.prompt.toFixed(3)} / $${k.completion.toFixed(3)} per 1M`,
        typical_turn_cents: ((2000 * k.prompt + 1000 * k.completion) / 10000.0)
      });
    });

    // Layer in any server-provided or dynamically discovered models
    serverModels.forEach(sm => {
      const id = sm.id;
      if (!id || id.toLowerCase().includes('embedding')) return;
      const k = KNOWN_MODEL_REGISTRY[id] || {};
      const promptP = typeof sm.prompt_price_per_m === 'number' ? sm.prompt_price_per_m : (k.prompt ?? 0.50);
      const compP = typeof sm.completion_price_per_m === 'number' ? sm.completion_price_per_m : (k.completion ?? 2.00);
      const isReasoning = sm.is_reasoning !== undefined ? sm.is_reasoning : Boolean(k.is_reasoning || id.includes('reasoner') || id.includes('r1'));
      const isSafeguard = sm.is_safeguard !== undefined ? sm.is_safeguard : Boolean(k.is_safeguard || promptP === 0);
      const icon = k.icon || (isReasoning ? '🧠' : (id.includes('coder') ? '💻' : (id.includes('claude') ? '🎭' : '🤖')));

      let group = k.group || 'generalist';
      if (!k.group) {
        if (isReasoning) group = 'reasoning';
        else if (id.includes('coder')) group = 'code';
        else if (promptP < 0.40) group = 'speed';
        else if (promptP >= 3.00) group = 'multimodal';
      }

      modelMap.set(id, {
        id,
        name: sm.name || k.name || formatModelName(id),
        provider: sm.provider || k.provider || 'LiteLLM Gateway',
        cost_tier: sm.cost_tier || k.tier || (promptP >= 3.0 ? 'Premium ($$$$)' : (promptP < 0.4 ? 'Ultra-Low ($)' : 'Moderate ($$$)')),
        context_window: sm.context_window || k.context || '128k',
        prompt_price_per_m: promptP,
        completion_price_per_m: compP,
        cached_price_per_m: sm.cached_price_per_m ?? (promptP * 0.2),
        is_reasoning: isReasoning,
        is_safeguard: isSafeguard,
        icon,
        group,
        status: sm.status || 'active',
        price_display: promptP === 0 ? 'Free ($0.000)' : `$${promptP.toFixed(3)} / $${compP.toFixed(3)} per 1M`,
        typical_turn_cents: sm.typical_turn_cents || ((2000 * promptP + 1000 * compP) / 10000.0)
      });
    });

    allModelsList = Array.from(modelMap.values());
    allModelsPricingMap = {};
    allModelsList.forEach(m => { allModelsPricingMap[m.id] = m; });

    if (!selectModel) return;
    const currentSelected = selectModel.value || 'auto';
    selectModel.innerHTML = '';

    // Helper to append options into optgroups
    const appendGroup = (label, models) => {
      if (!models || models.length === 0) return;
      const grp = document.createElement('optgroup');
      grp.label = label;
      models.forEach(m => {
        const opt = document.createElement('option');
        opt.value = m.id;
        opt.textContent = `${m.icon} ${m.name} — $${m.prompt_price_per_m.toFixed(2)} / $${m.completion_price_per_m.toFixed(2)} [Active]`;
        grp.appendChild(opt);
      });
      selectModel.appendChild(grp);
    };

    // 1. Auto Router & Safeguards
    const grpAuto = document.createElement('optgroup');
    grpAuto.label = '⚡ Auto Router & Safeguards (Budget Default)';
    const optAuto = document.createElement('option');
    optAuto.value = 'auto';
    optAuto.textContent = '✨ Auto (Frontier Router - DeepSeek / Gemini)';
    grpAuto.appendChild(optAuto);
    const safeguards = allModelsList.filter(m => m.is_safeguard);
    safeguards.forEach(m => {
      const opt = document.createElement('option');
      opt.value = m.id;
      opt.textContent = `${m.icon} ${m.name} — Free ($0.000) [Active]`;
      grpAuto.appendChild(opt);
    });
    selectModel.appendChild(grpAuto);

    // 2. Frontier Reasoning Tier
    appendGroup('🧠 Frontier Reasoning Tier (Deep Logic & Math)', allModelsList.filter(m => !m.is_safeguard && (m.group === 'reasoning' || m.is_reasoning)));

    // 3. Ultra-Speed Tier (< $0.40/1M)
    appendGroup('⚡ Ultra-Speed & Massive Context Tier (< $0.40/1M)', allModelsList.filter(m => !m.is_safeguard && !m.is_reasoning && m.group === 'speed'));

    // 4. Code Engineering Tier
    appendGroup('💻 Code Engineering & Architecture Tier', allModelsList.filter(m => !m.is_safeguard && m.group === 'code'));

    // 5. Frontier Multimodal & Claude Tier
    appendGroup('💎 Frontier Multimodal & Claude Tier', allModelsList.filter(m => !m.is_safeguard && m.group === 'multimodal'));

    // 6. Open Frontier Generalist
    appendGroup('🌐 Open Frontier Generalist Tier', allModelsList.filter(m => !m.is_safeguard && m.group === 'generalist'));

    // 7. Custom Model ID Entry
    const grpCustom = document.createElement('optgroup');
    grpCustom.label = '✏️ Custom / Direct Model ID';
    const optCustom = document.createElement('option');
    optCustom.value = '__custom__';
    optCustom.textContent = '✏️ Enter Custom LiteLLM Model ID...';
    grpCustom.appendChild(optCustom);
    selectModel.appendChild(grpCustom);

    // Restore selected value
    const values = Array.from(selectModel.options).map(o => o.value);
    if (values.includes(currentSelected)) {
      selectModel.value = currentSelected;
    } else if (currentSelected && currentSelected !== 'auto' && currentSelected !== '__custom__') {
      const opt = document.createElement('option');
      opt.value = currentSelected;
      opt.textContent = `🤖 ${currentSelected} (Custom Model)`;
      grpCustom.insertBefore(opt, optCustom);
      selectModel.value = currentSelected;
    } else {
      selectModel.value = 'auto';
    }

    updateModelCostBadge(selectModel.value);
    updatePromptPennyMeter();
  }

  function applyCustomModel() {
    if (!inputCustomModel || !selectModel) return;
    const modelId = inputCustomModel.value.trim();
    if (!modelId) {
      if (customModelRow) customModelRow.style.display = 'none';
      selectModel.value = 'auto';
      selectModel.dispatchEvent(new Event('change'));
      return;
    }

    if (!allModelsPricingMap[modelId]) {
      const customMeta = {
        id: modelId,
        name: formatModelName(modelId),
        provider: 'LiteLLM Gateway',
        cost_tier: 'Custom ($$)',
        context_window: '128k',
        prompt_price_per_m: 0.50,
        completion_price_per_m: 2.00,
        cached_price_per_m: 0.10,
        is_reasoning: (modelId.includes('reasoner') || modelId.includes('r1') || modelId.includes('o1')),
        is_safeguard: false,
        icon: '🤖',
        status: 'active',
        price_display: '$0.500 / $2.000 per 1M',
        typical_turn_cents: 0.30
      };
      allModelsList.push(customMeta);
      allModelsPricingMap[modelId] = customMeta;
    }

    let opt = Array.from(selectModel.options).find(o => o.value === modelId);
    if (!opt) {
      opt = document.createElement('option');
      opt.value = modelId;
      opt.textContent = `🤖 ${formatModelName(modelId)} [Active]`;
      const optCustom = selectModel.querySelector('option[value="__custom__"]');
      if (optCustom && optCustom.parentNode) {
        optCustom.parentNode.insertBefore(opt, optCustom);
      } else {
        selectModel.appendChild(opt);
      }
    }

    selectModel.value = modelId;
    if (customModelRow) customModelRow.style.display = 'none';
    selectModel.dispatchEvent(new Event('change'));
    if (window.Cockpit) window.Cockpit.notify(`Selected model: ${modelId}`, 'success');
  }

  if (btnApplyCustomModel && inputCustomModel) {
    btnApplyCustomModel.addEventListener('click', applyCustomModel);
    inputCustomModel.addEventListener('keydown', (e) => {
      if (e.key === 'Enter') {
        e.preventDefault();
        applyCustomModel();
      }
    });
  }

  if (btnCancelCustomModel) {
    btnCancelCustomModel.addEventListener('click', () => {
      if (customModelRow) customModelRow.style.display = 'none';
      if (selectModel.value === '__custom__') {
        selectModel.value = 'auto';
        selectModel.dispatchEvent(new Event('change'));
      }
    });
  }

  function updateModelCostBadge(modelId) {
    if (!modelCostBadge || !modelCostText) return;
    if (modelId === '__custom__') {
      modelCostBadge.className = 'model-cost-pill tier-moderate';
      if (modelCostDot) modelCostDot.className = 'cost-dot dot-green';
      modelCostText.innerHTML = '✏️ Custom Model &bull; Enter any LiteLLM Model ID';
      return;
    }
    if (modelId === 'auto') {
      modelCostBadge.className = 'model-cost-pill tier-ultralow';
      if (modelCostDot) modelCostDot.className = 'cost-dot dot-green';
      modelCostText.innerHTML = '⚡ Auto Router &bull; Budget Conscious (DeepSeek / Gemini)';
      return;
    }

    const m = allModelsPricingMap[modelId];
    if (!m) {
      modelCostBadge.className = 'model-cost-pill tier-moderate';
      if (modelCostDot) modelCostDot.className = 'cost-dot dot-green';
      modelCostText.textContent = `🤖 ${modelId} &bull; Standard Pricing (~$0.50/M in)`;
      return;
    }

    const isFree = m.prompt_price_per_m === 0;
    const isUltraLow = m.prompt_price_per_m < 0.40;
    const isPremium = m.prompt_price_per_m >= 3.00;

    if (isFree) {
      modelCostBadge.className = 'model-cost-pill tier-free';
      if (modelCostDot) modelCostDot.className = 'cost-dot dot-green';
      modelCostText.textContent = `🛡️ Free &bull; $0.000 / 1M &bull; 100% Zero-Cost Local`;
    } else if (isUltraLow) {
      modelCostBadge.className = 'model-cost-pill tier-ultralow';
      if (modelCostDot) modelCostDot.className = 'cost-dot dot-green';
      modelCostText.textContent = `🟢 Active &bull; In: $${m.prompt_price_per_m.toFixed(3)}/M &bull; Out: $${m.completion_price_per_m.toFixed(3)}/M &bull; ~${m.typical_turn_cents.toFixed(2)}¢/turn`;
    } else if (isPremium) {
      modelCostBadge.className = 'model-cost-pill tier-premium';
      if (modelCostDot) modelCostDot.className = 'cost-dot dot-rose';
      modelCostText.textContent = `⚠️ Premium Model &bull; In: $${m.prompt_price_per_m.toFixed(2)}/M &bull; Out: $${m.completion_price_per_m.toFixed(2)}/M &bull; ~${m.typical_turn_cents.toFixed(2)}¢/turn`;
    } else {
      modelCostBadge.className = 'model-cost-pill tier-moderate';
      if (modelCostDot) modelCostDot.className = 'cost-dot dot-green';
      modelCostText.textContent = `🟡 Active &bull; In: $${m.prompt_price_per_m.toFixed(2)}/M &bull; Out: $${m.completion_price_per_m.toFixed(2)}/M &bull; ~${m.typical_turn_cents.toFixed(2)}¢/turn`;
    }
  }

  function updatePromptPennyMeter() {
    if (!promptPennyMeter) return;
    const text = executivePrompt ? executivePrompt.value : '';
    const charLen = text.length;
    let inputTokens = Math.max(1, Math.ceil(charLen / 4));

    pinnedFiles.forEach(pf => { inputTokens += (pf.token_estimate || 0); });

    const effort = selectEffort ? selectEffort.value : 'auto';
    let outputTokens = 2000;
    if (effort === 'low') outputTokens = 1500;
    else if (effort === 'medium') outputTokens = 3000;
    else if (effort === 'high') outputTokens = 6000;
    else if (effort === 'max') outputTokens = 12000;

    const totalTokens = inputTokens + outputTokens;
    if (pennyEstimatedTokens) pennyEstimatedTokens.textContent = `~${inputTokens.toLocaleString()} in + ~${outputTokens.toLocaleString()} out (${totalTokens.toLocaleString()} tk)`;

    const selModel = selectModel ? selectModel.value : 'auto';
    let promptPrice = 0.075;
    let completionPrice = 0.30;

    if (selModel !== 'auto' && selModel !== '__custom__' && allModelsPricingMap[selModel]) {
      promptPrice = allModelsPricingMap[selModel].prompt_price_per_m;
      completionPrice = allModelsPricingMap[selModel].completion_price_per_m;
    }

    const estCostUsd = (inputTokens * promptPrice + outputTokens * completionPrice) / 1_000_000.0;
    const estCostCents = estCostUsd * 100.0;

    if (pennyEstimatedCost) {
      if (promptPrice === 0 && completionPrice === 0) {
        pennyEstimatedCost.textContent = `~$0.0000 (0.00¢) [Free]`;
        pennyEstimatedCost.className = 'penny-cost text-cyan';
      } else {
        pennyEstimatedCost.textContent = `~$${estCostUsd.toFixed(5)} (${estCostCents.toFixed(3)}¢)`;
        if (estCostUsd < 0.001) {
          pennyEstimatedCost.className = 'penny-cost text-green';
        } else if (estCostUsd < 0.01) {
          pennyEstimatedCost.className = 'penny-cost text-amber';
        } else {
          pennyEstimatedCost.className = 'penny-cost text-rose';
        }
      }
    }
  }

  // Sync Model Dropdown with Mode Tabs
  if (selectModel) {
    selectModel.addEventListener('change', () => {
      const val = selectModel.value;
      if (val === '__custom__') {
        if (customModelRow) {
          customModelRow.style.display = 'flex';
          if (inputCustomModel) {
            inputCustomModel.focus();
            inputCustomModel.select();
          }
        }
        updateModelCostBadge('__custom__');
        return;
      } else {
        if (customModelRow) customModelRow.style.display = 'none';
      }

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
      updateModelCostBadge(val);
      updatePromptPennyMeter();
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
        updateModelCostBadge(selectModel.value);
        updatePromptPennyMeter();
      }
      updateDecisionBannerForMode(currentMode);
    });
  });

  if (executivePrompt) {
    executivePrompt.addEventListener('input', updatePromptPennyMeter);
  }
  if (selectEffort) {
    selectEffort.addEventListener('change', updatePromptPennyMeter);
  }

  // ==============================================================================
  // Executive Model Pricing & Penny Matrix Modal Logic
  // ==============================================================================
  async function openPricingModal() {
    if (!modelPricingBackdrop) return;
    modelPricingBackdrop.style.display = 'flex';
    await refreshPricingMatrix();
  }

  function closePricingModal() {
    if (modelPricingBackdrop) modelPricingBackdrop.style.display = 'none';
  }

  async function refreshPricingMatrix() {
    try {
      const res = await fetch('/api/models/pricing');
      if (!res.ok) return;
      const data = await res.json();
      renderPricingModalData(data);
    } catch (err) {
      console.warn('Failed to load pricing matrix', err);
    }
  }

  function renderPricingModalData(data) {
    if (!data) return;
    const b = data.budget || {};
    if (pricingModalActiveCount) pricingModalActiveCount.textContent = data.active_count || '--';
    if (pricingModalTotalCount) pricingModalTotalCount.textContent = `of ${data.count || '--'} cataloged`;
    if (pricingModalTodaySpend) pricingModalTodaySpend.textContent = `$${(b.today_spend_usd || 0).toFixed(4)}`;
    if (pricingModalRemainingDaily) pricingModalRemainingDaily.textContent = `Remaining: $${(b.remaining_daily_usd || 0).toFixed(4)}`;
    if (pricingModalCacheSaved) pricingModalCacheSaved.textContent = `$${(b.cached_savings_usd || 0).toFixed(4)}`;
    if (pricingModalBreakerState) {
      pricingModalBreakerState.textContent = b.circuit_breaker_state || 'NORMAL';
      pricingModalBreakerState.className = b.circuit_breaker_state === 'TRIPPED' ? 'stat-value text-rose' : (b.circuit_breaker_state === 'WARNING' ? 'stat-value text-amber' : 'stat-value text-green');
    }

    renderPricingTableRows(data.models || allModelsList);
  }

  function renderPricingTableRows(models) {
    if (!pricingMatrixTableBody) return;
    pricingMatrixTableBody.innerHTML = '';

    const inTokens = parseInt(simInputTokens ? simInputTokens.value : 2000, 10) || 2000;
    const outTokens = parseInt(simOutputTokens ? simOutputTokens.value : 1000, 10) || 1000;

    models.forEach(m => {
      const tr = document.createElement('tr');
      const isActive = m.status === 'active';
      const promptP = m.prompt_price_per_m || 0;
      const compP = m.completion_price_per_m || 0;
      const simCostUsd = (inTokens * promptP + outTokens * compP) / 1_000_000.0;
      const simCostCents = simCostUsd * 100.0;

      let tierBadgeClass = 'ultralow';
      if (promptP === 0) tierBadgeClass = 'free';
      else if (promptP >= 3.0) tierBadgeClass = 'premium';
      else if (promptP >= 0.5) tierBadgeClass = 'moderate';

      let costTagClass = 'cheap';
      if (promptP === 0) costTagClass = 'free';
      else if (simCostUsd > 0.05) costTagClass = 'expensive';
      else if (simCostUsd > 0.005) costTagClass = 'moderate';

      const promptPriceStr = promptP === 0 ? '$0.000 (Free)' : `$${promptP.toFixed(3)}`;
      const compPriceStr = compP === 0 ? '$0.000 (Free)' : `$${compP.toFixed(3)}`;
      const simCostStr = promptP === 0 ? '$0.0000 (0.00¢)' : `$${simCostUsd.toFixed(5)} (${simCostCents.toFixed(3)}¢)`;

      tr.innerHTML = `
        <td>
          <div class="table-model-title">
            ${m.is_reasoning ? '🧠 ' : (m.id.includes('coder') ? '💻 ' : (promptP === 0 ? '🛡️ ' : '⚡ '))}${escapeHtml(m.name)}
            <span class="badge-tier ${tierBadgeClass}">${escapeHtml(m.cost_tier || '')}</span>
          </div>
          <span class="table-model-sub">${escapeHtml(m.id)} &bull; ${escapeHtml(m.category || '')}</span>
        </td>
        <td><span style="color:#CBD5E1;">${escapeHtml(m.provider || 'Gateway')}</span></td>
        <td>
          <span class="badge-status ${isActive ? 'active' : 'inactive'}">
            ${isActive ? '● ACTIVE' : '○ INACTIVE'}
          </span>
        </td>
        <td><span class="table-price-val text-cyan">${promptPriceStr}</span></td>
        <td><span class="table-price-val text-green">${compPriceStr}</span></td>
        <td><span class="sim-cost-tag ${costTagClass}">${simCostStr}</span></td>
        <td><span style="font-family:var(--font-mono);font-size:11px;color:#94A3B8;">${escapeHtml(m.context_window || '128k')}</span></td>
        <td>
          <button class="btn-select-model-row" data-model="${escapeHtml(m.id)}">
            Select Model
          </button>
        </td>
      `;

      const selectBtn = tr.querySelector('.btn-select-model-row');
      if (selectBtn) {
        selectBtn.addEventListener('click', () => {
          if (selectModel) {
            let opt = Array.from(selectModel.options).find(o => o.value === m.id);
            if (!opt) {
              opt = document.createElement('option');
              opt.value = m.id;
              opt.textContent = `${m.icon || '🤖'} ${m.name} — ${m.price_display || '$0.50 / $2.00'} [Active]`;
              const optCustom = selectModel.querySelector('option[value="__custom__"]');
              if (optCustom && optCustom.parentNode) {
                optCustom.parentNode.insertBefore(opt, optCustom);
              } else {
                selectModel.appendChild(opt);
              }
            }
            selectModel.value = m.id;
            selectModel.dispatchEvent(new Event('change'));
          }
          closePricingModal();
          if (window.Cockpit) window.Cockpit.notify(`Selected ${m.name} (${m.price_display || ''})`, 'success');
        });
      }

      pricingMatrixTableBody.appendChild(tr);
    });
  }

  if (btnOpenPricingModal) btnOpenPricingModal.addEventListener('click', openPricingModal);
  if (btnClosePricingModal) btnClosePricingModal.addEventListener('click', closePricingModal);
  if (btnOpenPricingFromMeter) btnOpenPricingFromMeter.addEventListener('click', openPricingModal);
  if (btnRefreshPricingMatrix) btnRefreshPricingMatrix.addEventListener('click', refreshPricingMatrix);

  if (modelPricingBackdrop) {
    modelPricingBackdrop.addEventListener('click', (e) => {
      if (e.target === modelPricingBackdrop) closePricingModal();
    });
  }

  if (simInputTokens) {
    simInputTokens.addEventListener('input', () => {
      renderPricingTableRows(allModelsList);
    });
  }
  if (simOutputTokens) {
    simOutputTokens.addEventListener('input', () => {
      renderPricingTableRows(allModelsList);
    });
  }

  calcPresetChips.forEach(chip => {
    chip.addEventListener('click', () => {
      calcPresetChips.forEach(c => c.classList.remove('active'));
      chip.classList.add('active');
      if (simInputTokens) simInputTokens.value = chip.dataset.in;
      if (simOutputTokens) simOutputTokens.value = chip.dataset.out;
      renderPricingTableRows(allModelsList);
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
    } else if (mode === 'arena') {
      decisionIndicator.textContent = '🏛️';
      decisionText.textContent = 'Council Arena Active: Parallel adversarial deliberation across DeepSeek Reasoner & Gemini Flash with executive consensus synthesis.';
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

  // ==============================================================================
  // Executive Sessions Archive Engine
  // ==============================================================================
  async function fetchSessions() {
    try {
      const res = await fetch('/api/sessions');
      if (!res.ok) return;
      const data = await res.json();
      allSessions = data.sessions || [];
      if (sessionCountBadge) sessionCountBadge.textContent = allSessions.length;
      renderSessionsList(allSessions);

      if (!activeSessionId && allSessions.length > 0) {
        selectSession(allSessions[0].id);
      }
    } catch (err) {
      console.warn('Failed to load sessions', err);
    }
  }

  function renderSessionsList(sessions) {
    if (!sessionsListContainer) return;
    sessionsListContainer.innerHTML = '';

    if (!sessions.length) {
      sessionsListContainer.innerHTML = '<div style="padding:16px;text-align:center;color:#64748B;font-size:12px;">No archived sessions. Click "+ New Thread" to begin.</div>';
      return;
    }

    sessions.forEach(s => {
      const item = document.createElement('div');
      item.className = `session-item ${s.id === activeSessionId ? 'active' : ''}`;
      
      const timeStr = formatRelativeTime(s.updated_at);
      const modelTag = s.model ? s.model.replace('-reasoner', ' R1').replace('-flash', ' Flash') : 'Auto';

      item.innerHTML = `
        <div class="session-item-left">
          <span class="session-item-title">${escapeHtml(s.title || 'Untitled Directive')}</span>
          <div class="session-item-meta">
            <span class="meta-tag">🤖 ${escapeHtml(modelTag)}</span>
            <span class="meta-tag">💬 ${s.message_count || 0} turns</span>
            <span>•</span>
            <span>${timeStr}</span>
          </div>
        </div>
        <div class="session-item-right">
          <button class="session-del-btn" title="Delete Session" data-id="${s.id}">✕</button>
        </div>
      `;

      item.addEventListener('click', (e) => {
        if (e.target.closest('.session-del-btn')) return;
        selectSession(s.id);
        if (sessionsDrawer) sessionsDrawer.style.display = 'none';
      });

      const delBtn = item.querySelector('.session-del-btn');
      if (delBtn) {
        delBtn.addEventListener('click', async (e) => {
          e.stopPropagation();
          await deleteSession(s.id);
        });
      }

      sessionsListContainer.appendChild(item);
    });
  }

  async function selectSession(sessionId) {
    if (!sessionId) return;
    try {
      const res = await fetch(`/api/sessions/${sessionId}`);
      if (!res.ok) return;
      const data = await res.json();
      activeSessionId = sessionId;
      activeSession = data.session;

      if (outputThreadTitle) outputThreadTitle.textContent = activeSession.title || 'Executive Briefing';
      if (outputSessionBadge) {
        outputSessionBadge.style.display = 'inline-block';
        outputSessionBadge.textContent = `${(activeSession.messages || []).length} turns`;
      }

      renderTimeline(activeSession.messages || []);
      renderSessionsList(allSessions);
    } catch (err) {
      console.warn('Failed to load session details', err);
    }
  }

  async function createNewSession(customTitle) {
    try {
      const res = await fetch('/api/sessions', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          title: customTitle || 'New Executive Directive',
          workspace_path: activeWorkspace ? activeWorkspace.path : null,
          model: selectModel ? selectModel.value : 'auto'
        })
      });
      const data = await res.json();
      if (data.success && data.session) {
        activeSessionId = data.session.id;
        activeSession = data.session;
        if (outputThreadTitle) outputThreadTitle.textContent = activeSession.title;
        if (outputSessionBadge) outputSessionBadge.style.display = 'none';
        renderTimeline([]);
        await fetchSessions();
        if (sessionsDrawer) sessionsDrawer.style.display = 'none';
        executivePrompt.value = '';
        executivePrompt.focus();
      }
    } catch (err) {
      console.error('Failed to create new session', err);
    }
  }

  async function deleteSession(sessionId) {
    try {
      await fetch(`/api/sessions/${sessionId}`, { method: 'DELETE' });
      if (activeSessionId === sessionId) {
        activeSessionId = null;
        activeSession = null;
        renderTimeline([]);
        if (outputThreadTitle) outputThreadTitle.textContent = 'Executive Briefing & Results';
        if (outputSessionBadge) outputSessionBadge.style.display = 'none';
      }
      await fetchSessions();
    } catch (err) {
      console.error('Failed to delete session', err);
    }
  }

  // Sessions Drawer toggles
  if (btnToggleSessions) {
    btnToggleSessions.addEventListener('click', () => {
      if (!sessionsDrawer) return;
      const isOpen = sessionsDrawer.style.display !== 'none';
      sessionsDrawer.style.display = isOpen ? 'none' : 'block';
      if (workspaceDrawer) workspaceDrawer.style.display = 'none';
      if (workspaceFilesDrawer) workspaceFilesDrawer.style.display = 'none';
      if (!isOpen && inputFilterSessions) {
        inputFilterSessions.value = '';
        inputFilterSessions.focus();
        renderSessionsList(allSessions);
      }
    });
  }

  if (btnCloseSessionsDrawer && sessionsDrawer) {
    btnCloseSessionsDrawer.addEventListener('click', () => {
      sessionsDrawer.style.display = 'none';
    });
  }

  if (btnNewSession) btnNewSession.addEventListener('click', () => createNewSession());
  if (btnDrawerNewSession) btnDrawerNewSession.addEventListener('click', () => createNewSession());

  if (inputFilterSessions) {
    inputFilterSessions.addEventListener('input', () => {
      const q = inputFilterSessions.value.toLowerCase().trim();
      const filtered = allSessions.filter(s => (s.title || '').toLowerCase().includes(q));
      renderSessionsList(filtered);
    });
  }

  // ==============================================================================
  // Conversation Timeline & Turn Card Engine
  // ==============================================================================
  function renderTimeline(messages) {
    if (!conversationTimeline) return;
    conversationTimeline.innerHTML = '';

    if (!messages || messages.length === 0) {
      if (outputPlaceholder) {
        outputPlaceholder.style.display = 'block';
        conversationTimeline.appendChild(outputPlaceholder);
      }
      outputStatus.textContent = 'Awaiting Directive';
      return;
    }

    if (outputPlaceholder) outputPlaceholder.style.display = 'none';

    messages.forEach((msg, idx) => {
      const card = createTurnCard(msg, idx);
      conversationTimeline.appendChild(card);
    });

    scrollToTimelineBottom();
  }

  function createTurnCard(msg, idx) {
    const card = document.createElement('div');
    const isUser = msg.role === 'user';
    card.className = `turn-card ${isUser ? 'turn-user' : 'turn-assistant'}`;

    const timeStr = msg.timestamp ? formatTime(msg.timestamp) : '';

    if (isUser) {
      card.innerHTML = `
        <div class="turn-header">
          <div class="turn-header-left">
            <span class="turn-avatar">👔</span>
            <div class="turn-meta">
              <span class="turn-author">CEO Directive</span>
              <span class="turn-time">${timeStr}</span>
            </div>
          </div>
          ${activeWorkspace ? `<span class="ws-badge" style="font-size:10px;">📁 ${escapeHtml(activeWorkspace.name)}</span>` : ''}
        </div>
        <div class="turn-content" style="font-size:14.5px;font-weight:500;color:#F1F5F9;white-space:pre-wrap;">${escapeHtml(msg.content)}</div>
      `;
    } else {
      const modelDisplayName = msg.model ? formatModelName(msg.model) : 'Frontier AI OS';
      const costStr = msg.cost_usd ? `$${msg.cost_usd.toFixed(4)}` : '';
      const latencyStr = msg.latency_ms ? `${msg.latency_ms}ms` : '';

      let thinkingHtml = '';
      if (msg.reasoning_content) {
        const thinkingId = `t_card_${idx}_${Date.now()}`;
        thinkingHtml = `
          <div class="thinking-accordion" style="display:block;margin-bottom:12px;">
            <div class="thinking-header" id="${thinkingId}_hdr" style="cursor:pointer;">
              <div class="thinking-title">
                <span class="thinking-sparkle">🧠</span>
                <span>Frontier Chain of Thought</span>
                <span class="thinking-badge">Verified Trace</span>
              </div>
              <button class="thinking-toggle-btn" id="${thinkingId}_btn">Show Thoughts ▾</button>
            </div>
            <div class="thinking-body" id="${thinkingId}_body" style="display:none;">
              <div class="thinking-content">${escapeHtml(msg.reasoning_content)}</div>
            </div>
          </div>
        `;
      }

      card.innerHTML = `
        <div class="turn-header">
          <div class="turn-header-left">
            <span class="turn-avatar">🤖</span>
            <div class="turn-meta">
              <span class="turn-author">${escapeHtml(modelDisplayName)}</span>
              <span class="turn-time">${timeStr} ${latencyStr ? `• ${latencyStr}` : ''}</span>
            </div>
          </div>
          <div class="turn-actions">
            ${costStr ? `<span class="meta-tag" style="color:#6EE7B7;background:rgba(16,185,129,0.1);font-size:11px;padding:2px 8px;border-radius:4px;">${costStr}</span>` : ''}
            <button class="turn-btn copy-turn-btn" title="Copy response">📋 Copy</button>
          </div>
        </div>
        ${thinkingHtml}
        <div class="turn-content markdown-body">${formatExecutiveMarkdown(msg.content)}</div>
      `;

      const copyBtn = card.querySelector('.copy-turn-btn');
      if (copyBtn) {
        copyBtn.addEventListener('click', async () => {
          try {
            await navigator.clipboard.writeText(msg.content);
            copyBtn.textContent = '✓ Copied!';
            setTimeout(() => { copyBtn.textContent = '📋 Copy'; }, 2000);
          } catch (err) {}
        });
      }

      if (msg.reasoning_content) {
        const hdr = card.querySelector('.thinking-header');
        const body = card.querySelector('.thinking-body');
        const btn = card.querySelector('.thinking-toggle-btn');
        let expanded = false;
        if (hdr && body && btn) {
          hdr.addEventListener('click', () => {
            expanded = !expanded;
            body.style.display = expanded ? 'block' : 'none';
            btn.textContent = expanded ? 'Hide Thoughts ▴' : 'Show Thoughts ▾';
          });
        }
      }
    }

    return card;
  }

  function scrollToTimelineBottom() {
    if (outputPanel) {
      outputPanel.scrollTop = outputPanel.scrollHeight;
    }
  }

  // Copy Latest Brief Button
  btnCopy.addEventListener('click', async () => {
    if (!activeSession || !activeSession.messages || !activeSession.messages.length) return;
    const lastAssistant = [...activeSession.messages].reverse().find(m => m.role === 'assistant');
    if (!lastAssistant) return;
    try {
      await navigator.clipboard.writeText(lastAssistant.content);
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

    try {
      if (currentMode === 'openclaw') {
        await dispatchToOpenClaw(prompt);
      } else if (currentMode === 'arena' || prompt.startsWith('/arena')) {
        const cleanPrompt = prompt.startsWith('/arena') ? prompt.replace(/^\/arena\s*/i, '') : prompt;
        await dispatchArenaDirective(cleanPrompt);
      } else {
        await dispatchStreamingDirective(prompt);
      }
    } catch (err) {
      displayError(err.message || 'Operational communication error occurred');
    } finally {
      document.dispatchEvent(new CustomEvent('cockpit:turn-complete', { detail: { prompt } }));
      refreshHealth();
    }
  }

  // ==============================================================================
  // Council Arena (Parallel Multi-Model) Dispatcher
  // ==============================================================================
  async function dispatchArenaDirective(prompt) {
    if (!prompt) return;
    setLoading(true);
    if (outputStatus) outputStatus.textContent = 'Council Deliberating in Parallel...';

    if (!activeSessionId) {
      await createNewSession(`🏛️ Council: ${prompt.slice(0, 30)}...`);
    }

    const userMsg = {
      role: 'user',
      content: prompt,
      timestamp: Date.now() / 1000
    };
    if (activeSession) {
      activeSession.messages = activeSession.messages || [];
      activeSession.messages.push(userMsg);
    }
    const userCard = createTurnCard(userMsg, (activeSession?.messages?.length || 1) - 1);
    if (conversationTimeline) {
      if (outputPlaceholder) outputPlaceholder.style.display = 'none';
      conversationTimeline.appendChild(userCard);
      scrollToTimelineBottom();
    }

    executivePrompt.value = '';

    try {
      const arenaModels = ['deepseek-reasoner', 'gemini-flash'];
      const payload = {
        prompt: prompt,
        models: arenaModels,
        effort: selectEffort ? selectEffort.value : 'auto',
        autonomy_mode: selectAutonomy ? selectAutonomy.value : 'full',
        temperature: selectTemperature ? parseFloat(selectTemperature.value) : 0.3,
        workspace_path: activeWorkspace ? activeWorkspace.path : null,
        pinned_files: pinnedFiles,
        session_id: activeSessionId
      };

      const res = await fetch('/api/command/arena', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });

      const data = await res.json();
      if (!res.ok || !data.success) {
        throw new Error(data.error || 'Arena deliberation failed');
      }

      if (data.session) {
        activeSession = data.session;
        activeSessionId = data.session.id;
        fetchSessions();
      }

      renderArenaTurnInTimeline(data.results, data.consensus);
      if (data.budget) renderBudget(data.budget);
      if (outputStatus) outputStatus.textContent = 'Council Verdict Complete';

    } catch (err) {
      const errMsg = {
        role: 'assistant',
        content: `⚠️ Council Deliberation Error: ${err.message}`,
        timestamp: Date.now() / 1000
      };
      if (conversationTimeline) {
        conversationTimeline.appendChild(createTurnCard(errMsg, 999));
        scrollToTimelineBottom();
      }
      if (outputStatus) outputStatus.textContent = 'Deliberation Error';
    } finally {
      setLoading(false);
      executivePrompt.value = '';
    }
  }

  function renderArenaTurnInTimeline(results, consensus) {
    if (!conversationTimeline) return;
    if (outputPlaceholder) outputPlaceholder.style.display = 'none';

    const card = document.createElement('div');
    card.className = 'turn-card turn-arena';

    let colsHtml = '';
    (results || []).forEach(r => {
      const reasoningHtml = r.reasoning_content ? `
        <div class="thinking-accordion" style="margin-bottom:8px;">
          <div class="thinking-header" onclick="const b = this.nextElementSibling; b.style.display = b.style.display === 'none' ? 'block' : 'none';">
            <div class="thinking-title">
              <span class="thinking-sparkle">🧠</span>
              <span>Reasoning Trace</span>
            </div>
            <button class="thinking-toggle-btn">Show Thoughts ▾</button>
          </div>
          <div class="thinking-body" style="display:none;">
            <div class="thinking-content">${escapeHtml(r.reasoning_content)}</div>
          </div>
        </div>
      ` : '';

      colsHtml += `
        <div class="arena-col-card">
          <div class="arena-col-header">
            <div class="arena-model-tag">
              <span>🤖</span>
              <span>${escapeHtml(r.model_name)}</span>
            </div>
            <div class="arena-meta-strip">
              <span class="arena-badge-latency">⚡ ${r.latency_ms}ms</span>
              <span class="arena-badge-cost">💵 $${(r.cost_actual || 0).toFixed(4)}</span>
            </div>
          </div>
          ${reasoningHtml}
          <div class="turn-content markdown-body">${formatExecutiveMarkdown(r.content || '')}</div>
        </div>
      `;
    });

    const consensusHtml = `
      <div class="arena-consensus-card">
        <div class="consensus-header">
          <span>🏛️</span>
          <span class="consensus-title">Executive Consensus & Trade-off Analysis</span>
        </div>
        <div class="consensus-body">
          <p><strong>Fastest Perspective:</strong> ${escapeHtml(consensus?.fastest?.model_name || 'N/A')} (${consensus?.fastest?.latency_ms || 0}ms)</p>
          <p><strong>Cost-Optimal:</strong> ${escapeHtml(consensus?.most_cost_efficient?.model_name || 'N/A')} ($${(consensus?.most_cost_efficient?.cost_usd || 0).toFixed(5)})</p>
          <p style="margin-top:6px;color:#E2E8F0;">${escapeHtml(consensus?.consensus_verdict || '')}</p>
        </div>
      </div>
    `;

    card.innerHTML = `
      <div class="turn-header">
        <div class="turn-header-left">
          <span class="turn-avatar">🏛️</span>
          <div class="turn-meta">
            <span class="turn-author">Executive AI Council Arena</span>
            <span class="turn-time">${(results || []).length} Frontier Perspectives Synthesized</span>
          </div>
        </div>
        <span class="tag-model" style="background:rgba(245,158,11,0.15);color:#FBBF24;border:1px solid rgba(245,158,11,0.3);padding:2px 8px;border-radius:999px;font-size:11px;font-weight:600;">Multi-Model Arena</span>
      </div>
      <div class="arena-grid">${colsHtml}</div>
      ${consensusHtml}
    `;

    conversationTimeline.appendChild(card);
    scrollToTimelineBottom();
  }



  // ==============================================================================
  // Real-Time Streaming Dispatcher Engine
  // ==============================================================================
  async function dispatchStreamingDirective(rawPrompt) {
    let prompt = rawPrompt.trim();

    // Check Slash Commands
    if (prompt.startsWith('/plan')) {
      prompt = prompt.replace(/^\/plan\s*/i, '');
      if (selectAutonomy) selectAutonomy.value = 'plan_first';
    } else if (prompt.startsWith('/reason')) {
      prompt = prompt.replace(/^\/reason\s*/i, '');
      if (selectModel) selectModel.value = 'deepseek-reasoner';
    } else if (prompt.startsWith('/fast')) {
      prompt = prompt.replace(/^\/fast\s*/i, '');
      if (selectModel) selectModel.value = 'gemini-flash';
    } else if (prompt.startsWith('/audit')) {
      prompt = 'Perform a complete architectural and infrastructure health check on our AI gateways, Redis cache latency, and budget governor limits.';
    } else if (prompt.startsWith('/memo')) {
      prompt = `Draft a high-density, 1-page executive memorandum for the leadership team regarding: ${prompt.replace(/^\/memo\s*/i, '') || 'our core technical and business priorities'}.`;
    } else if (prompt.startsWith('/export')) {
      exportReport();
      executivePrompt.value = '';
      return;
    } else if (prompt.startsWith('/clear')) {
      await createNewSession();
      executivePrompt.value = '';
      return;
    } else if (prompt.startsWith('/workspace')) {
      const targetPath = prompt.replace(/^\/workspace\s*/i, '').trim();
      if (targetPath) await switchWorkspace(targetPath);
      executivePrompt.value = '';
      return;
    }

    if (!prompt) return;

    if (!activeSessionId) {
      await createNewSession(prompt.slice(0, 42));
    }

    const userMsg = {
      role: 'user',
      content: prompt,
      timestamp: Date.now() / 1000
    };
    if (activeSession) {
      activeSession.messages = activeSession.messages || [];
      activeSession.messages.push(userMsg);
    }
    const userCard = createTurnCard(userMsg, (activeSession?.messages?.length || 1) - 1);
    if (conversationTimeline) {
      if (outputPlaceholder) outputPlaceholder.style.display = 'none';
      conversationTimeline.appendChild(userCard);
      scrollToTimelineBottom();
    }

    executivePrompt.value = '';
    setLoading(true);
    if (btnHaltStream) btnHaltStream.style.display = 'inline-flex';

    if (streamingTurnCard) {
      streamingTurnCard.style.display = 'block';
      if (streamingContentText) streamingContentText.innerHTML = '';
      if (streamingThinkingText) streamingThinkingText.textContent = '';
      if (streamingThinkingAccordion) streamingThinkingAccordion.style.display = 'none';
      if (streamingVelocity) streamingVelocity.textContent = '⚡ Initializing stream...';
      if (typewriterCursor) typewriterCursor.style.display = 'inline-block';
    }

    const payload = {
      prompt,
      session_id: activeSessionId,
      model: selectModel ? selectModel.value : 'auto',
      effort: selectEffort ? selectEffort.value : 'auto',
      autonomy_mode: selectAutonomy ? selectAutonomy.value : 'full',
      temperature: selectTemperature ? parseFloat(selectTemperature.value) : 0.3,
      include_workspace_context: isWsContextAttached,
      workspace_path: activeWorkspace ? activeWorkspace.path : null,
      pinned_files: pinnedFiles
    };

    activeStreamAbortController = new AbortController();
    const startTime = performance.now();
    let tokenCount = 0;
    let accumulatedContent = '';
    let accumulatedReasoning = '';

    try {
      const response = await fetch('/api/command/stream', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
        signal: activeStreamAbortController.signal
      });

      if (!response.ok) {
        throw new Error(`Server returned HTTP ${response.status}`);
      }

      const reader = response.body.getReader();
      const decoder = new TextDecoder();
      let buffer = '';

      while (true) {
        const { value, done } = await reader.read();
        if (done) break;

        buffer += decoder.decode(value, { stream: true });
        const events = buffer.split('\n\n');
        buffer = events.pop();

        for (const eventBlock of events) {
          const lines = eventBlock.split('\n');
          for (const line of lines) {
            if (line.startsWith('data: ')) {
              const jsonStr = line.slice(6).trim();
              if (!jsonStr) continue;
              try {
                const eventData = JSON.parse(jsonStr);

                if (eventData.type === 'start') {
                  if (streamingModelPill) streamingModelPill.textContent = eventData.model_name || eventData.model;
                  decisionIndicator.textContent = eventData.model.includes('reasoner') ? '🧠' : '⚡';
                  decisionText.textContent = eventData.routing_reason;
                  decisionMetrics.style.display = 'flex';
                  metricModel.textContent = eventData.model;
                  if (metricEffort) metricEffort.textContent = `${(eventData.effort || 'AUTO').toUpperCase()} EFFORT`;
                  if (metricWorkspace && eventData.workspace) metricWorkspace.textContent = `📁 ${eventData.workspace.name}`;
                } else if (eventData.type === 'thinking') {
                  accumulatedReasoning += eventData.delta;
                  tokenCount += 1;
                  if (streamingThinkingAccordion) streamingThinkingAccordion.style.display = 'block';
                  if (streamingThinkingText) streamingThinkingText.textContent = accumulatedReasoning;
                  if (streamingThinkingBadge) streamingThinkingBadge.textContent = `${tokenCount} Tokens`;
                } else if (eventData.type === 'content') {
                  accumulatedContent += eventData.delta;
                  tokenCount += 1;
                  if (streamingContentText) {
                    streamingContentText.innerHTML = formatExecutiveMarkdown(accumulatedContent);
                  }
                  const elapsedSec = (performance.now() - startTime) / 1000;
                  if (elapsedSec > 0.3 && streamingVelocity) {
                    const speed = (tokenCount / elapsedSec).toFixed(1);
                    streamingVelocity.textContent = `⚡ ${speed} tokens/s`;
                  }
                  scrollToTimelineBottom();
                } else if (eventData.type === 'done') {
                  if (streamingTurnCard) streamingTurnCard.style.display = 'none';
                  if (btnHaltStream) btnHaltStream.style.display = 'none';

                  const assistantMsg = {
                    role: 'assistant',
                    content: accumulatedContent,
                    reasoning_content: accumulatedReasoning,
                    model: eventData.session?.model || payload.model,
                    latency_ms: eventData.latency_ms,
                    cost_usd: eventData.cost_actual,
                    timestamp: Date.now() / 1000
                  };

                  if (activeSession) {
                    activeSession.messages.push(assistantMsg);
                  }

                  const assistantCard = createTurnCard(assistantMsg, (activeSession?.messages?.length || 1) - 1);
                  if (conversationTimeline) {
                    conversationTimeline.appendChild(assistantCard);
                    scrollToTimelineBottom();
                  }

                  if (eventData.budget) renderBudget(eventData.budget);
                  refreshCacheStats();
                  fetchSessions();
                  outputStatus.textContent = `Completed (${eventData.latency_ms}ms)`;
                } else if (eventData.type === 'error') {
                  displayError(eventData.error);
                }
              } catch (parseErr) {
                console.warn('SSE JSON parse error', parseErr);
              }
            }
          }
        }
      }
    } catch (err) {
      if (err.name === 'AbortError') {
        outputStatus.textContent = 'Stream Halted by CEO';
        if (streamingTurnCard) streamingTurnCard.style.display = 'none';
        if (btnHaltStream) btnHaltStream.style.display = 'none';
      } else {
        displayError(err.message || 'Stream communication failed');
      }
    } finally {
      setLoading(false);
      activeStreamAbortController = null;
      if (btnHaltStream) btnHaltStream.style.display = 'none';
      if (streamingTurnCard) streamingTurnCard.style.display = 'none';
    }
  }

  // Stop button handler
  if (btnHaltStream) {
    btnHaltStream.addEventListener('click', () => {
      if (activeStreamAbortController) {
        activeStreamAbortController.abort();
      }
    });
  }

  if (btnToggleStreamingThinking) {
    btnToggleStreamingThinking.addEventListener('click', () => {
      isStreamingThinkingExpanded = !isStreamingThinkingExpanded;
      if (streamingThinkingBody) streamingThinkingBody.style.display = isStreamingThinkingExpanded ? 'block' : 'none';
      btnToggleStreamingThinking.textContent = isStreamingThinkingExpanded ? 'Hide Thoughts ▴' : 'Show Thoughts ▾';
    });
  }

  async function dispatchToOpenClaw(prompt) {
    outputStatus.textContent = 'Dispatching to OpenClaw Swarm...';

    const userMsg = {
      role: 'user',
      content: prompt,
      timestamp: Date.now() / 1000
    };
    if (activeSession) {
      activeSession.messages = activeSession.messages || [];
      activeSession.messages.push(userMsg);
    }
    const userCard = createTurnCard(userMsg, (activeSession?.messages?.length || 1) - 1);
    if (conversationTimeline) {
      if (outputPlaceholder) outputPlaceholder.style.display = 'none';
      conversationTimeline.appendChild(userCard);
      scrollToTimelineBottom();
    }
    executivePrompt.value = '';
    setLoading(true);

    try {
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

      const assistantMsg = {
        role: 'assistant',
        content: data.reply,
        model: `OpenClaw (${data.agent_id})`,
        latency_ms: data.latency_ms,
        cost_usd: 0.0,
        timestamp: Date.now() / 1000
      };

      if (activeSession) {
        activeSession.messages.push(assistantMsg);
      }

      const assistantCard = createTurnCard(assistantMsg, (activeSession?.messages?.length || 1) - 1);
      if (conversationTimeline) {
        conversationTimeline.appendChild(assistantCard);
        scrollToTimelineBottom();
      }

      outputStatus.textContent = `Agent Completed (${data.latency_ms}ms)`;
    } finally {
      setLoading(false);
    }
  }

  // ==============================================================================
  // Slash Commands Popover Engine
  // ==============================================================================
  const slashItems = [
    { cmd: '/plan', title: '/plan [directive]', desc: 'Execute in Plan-First mode with architectural roadmap & milestones' },
    { cmd: '/reason', title: '/reason [directive]', desc: 'DeepSeek R1 frontier strategic reasoning with full deliberation trace' },
    { cmd: '/fast', title: '/fast [directive]', desc: 'Fast synthesis tier via Gemini Flash with massive context ingestion' },
    { cmd: '/audit', title: '/audit', desc: 'Run complete health verification on gateways, redis, and budget governor' },
    { cmd: '/memo', title: '/memo [topic]', desc: 'Draft structured 1-page executive memorandum for leadership team' },
    { cmd: '/export', title: '/export', desc: 'Export current executive conversation thread as Markdown document' },
    { cmd: '/clear', title: '/clear', desc: 'Reset conversation thread and start a fresh directive' }
  ];

  executivePrompt.addEventListener('input', () => {
    const val = executivePrompt.value;
    if (val.startsWith('/')) {
      const q = val.toLowerCase();
      renderSlashMenu(q);
    } else {
      if (slashMenu) slashMenu.style.display = 'none';
    }
  });

  function renderSlashMenu(query) {
    if (!slashMenu || !slashOptionsList) return;
    const filtered = slashItems.filter(item => item.cmd.startsWith(query) || item.title.toLowerCase().includes(query));

    if (!filtered.length) {
      slashMenu.style.display = 'none';
      return;
    }

    slashOptionsList.innerHTML = '';
    filtered.forEach((item, idx) => {
      const opt = document.createElement('div');
      opt.className = `slash-option ${idx === 0 ? 'active' : ''}`;
      opt.dataset.cmd = item.cmd;
      opt.innerHTML = `
        <span class="slash-icon">${item.cmd === '/plan' ? '📋' : (item.cmd === '/reason' ? '🧠' : (item.cmd === '/fast' ? '⚡' : (item.cmd === '/audit' ? '🛡️' : (item.cmd === '/memo' ? '📝' : (item.cmd === '/export' ? '📄' : '🗑️')))))}</span>
        <div class="slash-info">
          <span class="slash-title">${escapeHtml(item.title)}</span>
          <span class="slash-desc">${escapeHtml(item.desc)}</span>
        </div>
      `;
      opt.addEventListener('click', () => applySlashCommand(item.cmd));
      slashOptionsList.appendChild(opt);
    });

    slashMenu.style.display = 'block';
    activeSlashIndex = 0;
  }

  function applySlashCommand(cmd) {
    executivePrompt.value = `${cmd} `;
    if (slashMenu) slashMenu.style.display = 'none';
    executivePrompt.focus();
  }

  executivePrompt.addEventListener('keydown', (e) => {
    if (slashMenu && slashMenu.style.display !== 'none') {
      const options = slashOptionsList.querySelectorAll('.slash-option');
      if (e.key === 'ArrowDown') {
        e.preventDefault();
        activeSlashIndex = (activeSlashIndex + 1) % options.length;
        options.forEach((o, i) => o.classList.toggle('active', i === activeSlashIndex));
        return;
      }
      if (e.key === 'ArrowUp') {
        e.preventDefault();
        activeSlashIndex = (activeSlashIndex - 1 + options.length) % options.length;
        options.forEach((o, i) => o.classList.toggle('active', i === activeSlashIndex));
        return;
      }
      if (e.key === 'Enter' || e.key === 'Tab') {
        e.preventDefault();
        const activeOpt = options[activeSlashIndex];
        if (activeOpt) applySlashCommand(activeOpt.dataset.cmd);
        return;
      }
      if (e.key === 'Escape') {
        slashMenu.style.display = 'none';
        return;
      }
    }

    if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') {
      e.preventDefault();
      dispatchCommand();
    }
  });

  // ==============================================================================
  // Global Executive Command Palette (Ctrl+K)
  // ==============================================================================
  const cpActions = [
    { title: 'New Executive Thread', desc: 'Create a clean, isolated conversation session', icon: '＋', action: () => createNewSession() },
    { title: 'Council Arena Mode (Parallel Multi-Model)', desc: 'Run DeepSeek + Gemini concurrently with consensus synthesis', icon: '🏛️', action: () => { const tab = document.getElementById('tabArena'); if (tab) tab.click(); } },
    { title: 'Browse Workspace File Explorer', desc: 'Browse repository hierarchy, inspect files, and pin context', icon: '📁', action: () => { if (btnExploreWs) btnExploreWs.click(); } },
    { title: 'Auto-Decisions Mission Control', desc: 'View live desktop approval stream, click counts, and restart service', icon: '⚡', action: () => openAutoClickerModal() },
    { title: 'Clear Pinned Context Files', desc: 'Remove all pinned files from active prompt context', icon: '🧹', action: () => clearPinnedFiles() },
    { title: 'Export Executive Briefing', desc: 'Download entire thread transcript as formatted Markdown report', icon: '📄', action: () => exportReport() },
    { title: 'Model: DeepSeek Reasoner (R1)', desc: 'Switch model to frontier reasoning tier ($0.55 / $2.19)', icon: '🧠', action: () => selectHarnessModel('deepseek-reasoner') },
    { title: 'Model: DeepSeek Chat (V3)', desc: 'Switch model to ultra-low cost high-speed synthesis ($0.14 / $0.28)', icon: '⚡', action: () => selectHarnessModel('deepseek-chat') },
    { title: 'Model: Gemini 2.5/3.8 Flash', desc: 'Switch model to high-speed 1M+ context tier ($0.075 / $0.300)', icon: '⚡', action: () => selectHarnessModel('gemini-flash') },
    { title: 'Model: Gemini Pro', desc: 'Switch model to frontier multimodal tier ($1.25 / $5.00)', icon: '💎', action: () => selectHarnessModel('gemini-pro') },
    { title: 'Model: Claude 3.5 Sonnet', desc: 'Switch model to frontier Anthropic tier ($3.00 / $15.00)', icon: '🎭', action: () => selectHarnessModel('claude-3-5-sonnet') },
    { title: 'Model: Claude 3.5 Haiku', desc: 'Switch model to lightweight high-velocity Claude tier ($0.80 / $4.00)', icon: '🍃', action: () => selectHarnessModel('claude-3-5-haiku') },
    { title: 'Model: Qwen 2.5 Coder 32B', desc: 'Switch model to SiliconFlow deep coding tier ($0.20 / $0.40)', icon: '💻', action: () => selectHarnessModel('siliconflow-qwen-coder') },
    { title: 'Model: Qwen 2.5 72B Instruct', desc: 'Switch model to open frontier generalist tier ($0.20 / $0.40)', icon: '🌐', action: () => selectHarnessModel('siliconflow-qwen') },
    { title: 'Model: DashScope Qwen Max', desc: 'Switch model to Alibaba frontier reasoning tier ($1.60 / $6.40)', icon: '👑', action: () => selectHarnessModel('dashscope-qwen-max') },
    { title: 'Model: DashScope Qwen Plus', desc: 'Switch model to Alibaba economical tier ($0.12 / $0.24)', icon: '🌐', action: () => selectHarnessModel('dashscope-qwen-plus') },
    { title: 'Model: Custom Model Entry', desc: 'Type any custom LiteLLM model ID directly', icon: '✏️', action: () => selectHarnessModel('__custom__') },
    { title: 'Effort: Low (2k Tokens)', desc: 'Set reasoning effort to rapid synthesis tier', icon: '⚡', action: () => { if (selectEffort) selectEffort.value = 'low'; } },
    { title: 'Effort: Max (16k+ Tokens)', desc: 'Set reasoning effort to maximum frontier proof tier', icon: '🌌', action: () => { if (selectEffort) selectEffort.value = 'max'; } },
    { title: 'Autonomy: Full Autonomy', desc: 'Enable end-to-end autonomous execution', icon: '🚀', action: () => { if (selectAutonomy) selectAutonomy.value = 'full'; } },
    { title: 'Autonomy: Plan-First', desc: 'Require structured roadmap and architecture first', icon: '📋', action: () => { if (selectAutonomy) selectAutonomy.value = 'plan_first'; } },
    { title: 'Model Pricing & Penny Ledger', desc: 'Inspect token costs and active/inactive status across all models', icon: '💰', action: () => openPricingModal() },
    { title: 'Switch Active Workspace', desc: 'Open workspace switcher folder drawer', icon: '📁', action: () => { if (workspaceDrawer) workspaceDrawer.style.display = 'block'; } },
    { title: 'Emergency Stop: Trip Circuit Breaker', desc: 'Immediately halt cloud API spend and fallback to local', icon: '🛑', action: () => { if (btnTripBreaker) btnTripBreaker.click(); } }
  ];

  function selectHarnessModel(modelId) {
    if (!selectModel) return;
    if (modelId === '__custom__') {
      selectModel.value = '__custom__';
      selectModel.dispatchEvent(new Event('change'));
      return;
    }
    let opt = Array.from(selectModel.options).find(o => o.value === modelId);
    if (!opt) {
      opt = document.createElement('option');
      opt.value = modelId;
      opt.textContent = `🤖 ${formatModelName(modelId)} [Active]`;
      const optCustom = selectModel.querySelector('option[value="__custom__"]');
      if (optCustom && optCustom.parentNode) {
        optCustom.parentNode.insertBefore(opt, optCustom);
      } else {
        selectModel.appendChild(opt);
      }
    }
    selectModel.value = modelId;
    selectModel.dispatchEvent(new Event('change'));
    if (window.Cockpit) window.Cockpit.notify(`Model switched to ${formatModelName(modelId)}`, 'info');
  }

  function openCommandPalette() {
    if (!commandPaletteBackdrop) return;
    commandPaletteBackdrop.style.display = 'flex';
    if (cpSearchInput) {
      cpSearchInput.value = '';
      cpSearchInput.focus();
    }
    renderCommandPalette('');
  }

  function closeCommandPalette() {
    if (commandPaletteBackdrop) commandPaletteBackdrop.style.display = 'none';
  }

  function renderCommandPalette(query) {
    if (!cpOptionsList) return;
    const q = (query || '').toLowerCase().trim();
    const filtered = cpActions.filter(a => a.title.toLowerCase().includes(q) || a.desc.toLowerCase().includes(q));

    cpOptionsList.innerHTML = '';
    if (!filtered.length) {
      cpOptionsList.innerHTML = '<div style="padding:16px;text-align:center;color:#64748B;font-size:12px;">No matching actions found.</div>';
      return;
    }

    filtered.forEach((item, idx) => {
      const opt = document.createElement('div');
      opt.className = `cp-option ${idx === 0 ? 'active' : ''}`;
      opt.innerHTML = `
        <div class="cp-option-left">
          <span class="cp-option-icon">${item.icon}</span>
          <div class="cp-option-info">
            <span class="cp-option-title">${escapeHtml(item.title)}</span>
            <span class="cp-option-desc">${escapeHtml(item.desc)}</span>
          </div>
        </div>
      `;
      opt.addEventListener('click', () => {
        closeCommandPalette();
        item.action();
      });
      cpOptionsList.appendChild(opt);
    });
    activeCpIndex = 0;
  }

  if (btnOpenCommandPalette) btnOpenCommandPalette.addEventListener('click', openCommandPalette);

  if (commandPaletteBackdrop) {
    commandPaletteBackdrop.addEventListener('click', (e) => {
      if (e.target === commandPaletteBackdrop) closeCommandPalette();
    });
  }

  document.addEventListener('keydown', (e) => {
    if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'k') {
      e.preventDefault();
      openCommandPalette();
    }
    if (e.key === 'Escape') {
      if (commandPaletteBackdrop && commandPaletteBackdrop.style.display !== 'none') {
        closeCommandPalette();
      }
      if (slashMenu && slashMenu.style.display !== 'none') {
        slashMenu.style.display = 'none';
      }
    }
  });

  if (cpSearchInput) {
    cpSearchInput.addEventListener('input', () => {
      renderCommandPalette(cpSearchInput.value);
    });

    cpSearchInput.addEventListener('keydown', (e) => {
      const options = cpOptionsList.querySelectorAll('.cp-option');
      if (e.key === 'ArrowDown') {
        e.preventDefault();
        activeCpIndex = (activeCpIndex + 1) % options.length;
        options.forEach((o, i) => o.classList.toggle('active', i === activeCpIndex));
      } else if (e.key === 'ArrowUp') {
        e.preventDefault();
        activeCpIndex = (activeCpIndex - 1 + options.length) % options.length;
        options.forEach((o, i) => o.classList.toggle('active', i === activeCpIndex));
      } else if (e.key === 'Enter') {
        e.preventDefault();
        const activeOpt = options[activeCpIndex];
        if (activeOpt) activeOpt.click();
      }
    });
  }

  // ==============================================================================
  // One-Click Executive Report Exporter
  // ==============================================================================
  function exportReport() {
    if (!activeSession || !activeSession.messages || activeSession.messages.length === 0) {
      alert('No conversation history to export.');
      return;
    }

    const title = activeSession.title || 'Executive_Briefing';
    const timestamp = new Date().toISOString();
    let md = `# Executive Briefing Report: ${title}\n\n`;
    md += `**Date**: ${timestamp}  \n`;
    md += `**Target Workspace**: \`${activeSession.workspace_path || 'N/A'}\`  \n`;
    md += `**Orchestration Engine**: Antigravity CEO Cockpit (Round 1 Frontier Harness)  \n\n`;
    md += `---\n\n`;

    activeSession.messages.forEach((msg, idx) => {
      if (msg.role === 'user') {
        md += `### Directive ${idx + 1} (CEO):\n> ${msg.content.replace(/\n/g, '\n> ')}\n\n`;
      } else {
        md += `### AI OS Resolution (${msg.model || 'Frontier Model'}):\n`;
        if (msg.latency_ms) md += `*Latency: ${msg.latency_ms}ms | Cost: $${(msg.cost_usd || 0).toFixed(4)}*\n\n`;
        if (msg.reasoning_content) {
          md += `#### Frontier Deliberation Trace (Chain of Thought):\n\`\`\`\n${msg.reasoning_content}\n\`\`\`\n\n`;
        }
        md += `${msg.content}\n\n---\n\n`;
      }
    });

    const blob = new Blob([md], { type: 'text/markdown;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `${title.replace(/[^a-zA-Z0-9_-]/g, '_')}_Report.md`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  }

  if (btnExportReport) btnExportReport.addEventListener('click', exportReport);

  // ==============================================================================
  // Markdown & Utility Helpers
  // ==============================================================================
  function formatExecutiveMarkdown(text) {
    if (!text) return '<p>No content generated.</p>';
    
    let html = text
      .replace(/^### (.*$)/gim, '<h4 style="color:#67E8F9;margin:16px 0 8px;font-size:15px;font-weight:700;">$1</h4>')
      .replace(/^## (.*$)/gim, '<h3 style="color:#FFFFFF;margin:20px 0 10px;font-size:17px;font-weight:700;border-bottom:1px solid rgba(255,255,255,0.08);padding-bottom:6px;">$1</h3>')
      .replace(/^# (.*$)/gim, '<h2 style="color:#FFFFFF;margin:24px 0 12px;font-size:19px;font-weight:700;">$1</h2>')
      .replace(/\*\*(.*?)\*\*/g, '<strong style="color:#F1F5F9;font-weight:700;">$1</strong>')
      .replace(/^\s*[-*]\s(.*$)/gim, '<li style="margin-left:20px;margin-bottom:6px;color:#CBD5E1;">$1</li>')
      .replace(/^\s*\d+\.\s(.*$)/gim, '<li style="margin-left:20px;margin-bottom:6px;color:#CBD5E1;">$1</li>')
      .replace(/`([^`]+)`/g, '<code style="font-family:var(--font-mono);background:rgba(0,0,0,0.35);color:#38BDF8;padding:2px 6px;border-radius:4px;font-size:12.5px;">$1</code>')
      .replace(/\n\n/g, '<br><br>');

    return `<div class="formatted-brief">${html}</div>`;
  }

  function formatRelativeTime(ts) {
    if (!ts) return 'just now';
    const diff = Math.floor(Date.now() / 1000 - ts);
    if (diff < 60) return 'just now';
    if (diff < 3600) return `${Math.floor(diff / 60)}m ago`;
    if (diff < 86400) return `${Math.floor(diff / 3600)}h ago`;
    return `${Math.floor(diff / 86400)}d ago`;
  }

  function formatTime(ts) {
    if (!ts) return '';
    const d = new Date(ts * 1000);
    return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  }

  function formatModelName(modelId) {
    if (!modelId) return 'Frontier AI OS';
    if (KNOWN_MODEL_REGISTRY[modelId]) return KNOWN_MODEL_REGISTRY[modelId].name;
    if (modelId.includes('deepseek-reasoner')) return 'DeepSeek Reasoner (R1)';
    if (modelId.includes('deepseek-chat')) return 'DeepSeek Chat (V3)';
    if (modelId.includes('gemini-flash')) return 'Gemini 2.5/3.8 Flash';
    if (modelId.includes('gemini-pro')) return 'Gemini Pro';
    if (modelId.includes('claude-3-5-sonnet')) return 'Claude 3.5 Sonnet';
    if (modelId.includes('claude-3-5-haiku')) return 'Claude 3.5 Haiku';
    if (modelId.includes('qwen-coder')) return 'Qwen 2.5 Coder 32B';
    if (modelId.includes('qwen-max')) return 'DashScope Qwen Max';
    if (modelId.includes('qwen-plus')) return 'DashScope Qwen Plus';
    if (modelId.includes('siliconflow-qwen')) return 'Qwen 2.5 72B Instruct';
    if (modelId.includes('siliconflow-deepseek')) return 'DeepSeek V3 (SiliconFlow)';
    if (modelId.includes('safeguard')) return 'Zero-Cost Local Safeguard';
    return modelId;
  }

  function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
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

    // 4. Savings Telemetry & Penny Remaining Meter
    if (hudCostSaved) {
      hudCostSaved.textContent = `$${b.cached_savings_usd.toFixed(4)}`;
    }
    if (hudHitRate) {
      hudHitRate.textContent = `${b.cache_hit_rate_pct}% Cache Rate`;
    }
    if (pennyRemainingBudget) {
      const rem = Math.max(0, b.daily_budget_usd - b.today_spend_usd);
      pennyRemainingBudget.textContent = `Daily Cap: $${rem.toFixed(2)} rem / $${b.daily_budget_usd.toFixed(2)}`;
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

  // ==============================================================================
  // Extension Bridge (consumed by web/modules/*.js Command Deck panels)
  // ==============================================================================
  window.Cockpit = {
    registerAction: (action) => { cpActions.push(action); },
    escapeHtml,
    formatMarkdown: formatExecutiveMarkdown,
    getWorkspace: () => activeWorkspace,
    getPinnedFiles: () => pinnedFiles.slice(),
    pinFile: (fileData) => pinFile(fileData),
    getPrompt: () => executivePrompt.value,
    setPrompt: (text) => { executivePrompt.value = text; executivePrompt.focus(); executivePrompt.dispatchEvent(new Event('input')); },
    submitPrompt: async (text) => { executivePrompt.value = text; await dispatchCommand(); },
    getSelections: () => ({
      model: selectModel ? selectModel.value : 'auto',
      effort: selectEffort ? selectEffort.value : 'auto',
      autonomy: selectAutonomy ? selectAutonomy.value : 'full'
    }),
    renderBudget: (b) => renderBudget(b),
    refreshBudget: () => fetchBudget(),
    newSession: () => createNewSession(),
    closePalette: () => closeCommandPalette(),
    notify: (msg, level = 'info') => {
      if (window.CockpitDeck && typeof window.CockpitDeck.toast === 'function') {
        window.CockpitDeck.toast(msg, level);
      } else {
        console.log(`[${level.toUpperCase()}] ${msg}`);
      }
    }
  };
  document.dispatchEvent(new CustomEvent('cockpit:ready'));

  // Initial runs
  fetchSessions();
  fetchWorkspace();
  fetchModelsList();
  refreshHealth();
  refreshCacheStats();
  fetchBudget();

  setInterval(refreshHealth, 10000);
  setInterval(fetchBudget, 5000);
});
