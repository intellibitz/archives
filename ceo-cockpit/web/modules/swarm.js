// Round 4 — Multi-Agent Swarm Mission Control (Frontend View)
(function() {
  'use strict';

  function initSwarmView(container) {
    container.innerHTML = `
      <div class="deck-toolbar">
        <div class="deck-toolbar-left">
          <span class="deck-panel-title">🐝 Multi-Agent Swarm Mission Control</span>
          <span class="badge" id="swarmAgentCount">5 Agents Active</span>
          <span class="badge badge-accent" id="swarmStrategyBadge">Parallel Council</span>
        </div>
        <div class="deck-toolbar-right">
          <button class="deck-btn" id="btnRefreshTopology">🔄 Refresh Topology</button>
          <button class="deck-btn" id="btnViewSwarmHistory">📜 Swarm History</button>
        </div>
      </div>

      <div class="deck-content-split">
        <!-- Left: Swarm Topology & Dispatcher -->
        <div class="deck-pane deck-pane-left">
          <div class="deck-section-label">Active Agent Topology</div>
          <div class="swarm-topology-grid" id="swarmTopologyGrid">
            <div class="deck-empty">Loading agent council...</div>
          </div>

          <div class="deck-section-label" style="margin-top: 16px;">Executive Directive Dispatcher</div>
          <div class="swarm-dispatch-box">
            <textarea id="swarmPromptInput" class="deck-textarea" rows="3"
              placeholder="Issue strategic directive to the autonomous swarm (e.g. 'Audit authentication architecture and outline zero-trust migration plan')..."></textarea>
            <div class="swarm-actions-row">
              <select id="swarmStrategySelect" class="deck-select">
                <option value="parallel-council">Parallel Council (Full Swarm)</option>
                <option value="adversarial-review">Adversarial Defense Review</option>
                <option value="pipeline-staged">Pipeline Staged Execution</option>
              </select>
              <button class="btn btn-primary" id="btnDispatchSwarm">🚀 Launch Swarm</button>
            </div>
          </div>
        </div>

        <!-- Right: Real-time Live Consensus & Agent Stream -->
        <div class="deck-pane deck-pane-right" id="swarmResultsPane">
          <div class="deck-section-label">Swarm Consensus & Perspectives</div>
          <div id="swarmConsensusBox" class="swarm-consensus-box" style="display: none;">
            <div class="swarm-consensus-header">
              <span class="swarm-consensus-badge">👑 Executive Consensus</span>
              <span id="swarmRunMeta" class="swarm-meta"></span>
            </div>
            <div id="swarmConsensusBody" class="swarm-consensus-body"></div>
          </div>

          <div id="swarmAgentsStream" class="swarm-agents-stream">
            <div class="deck-empty">Select or issue a directive to mobilize the agent swarm.</div>
          </div>
        </div>
      </div>
    `;

    // Elements
    const grid = container.querySelector('#swarmTopologyGrid');
    const promptInput = container.querySelector('#swarmPromptInput');
    const btnDispatch = container.querySelector('#btnDispatchSwarm');
    const btnRefresh = container.querySelector('#btnRefreshTopology');
    const btnHistory = container.querySelector('#btnViewSwarmHistory');
    const consensusBox = container.querySelector('#swarmConsensusBox');
    const consensusBody = container.querySelector('#swarmConsensusBody');
    const runMeta = container.querySelector('#swarmRunMeta');
    const agentsStream = container.querySelector('#swarmAgentsStream');
    const agentCountBadge = container.querySelector('#swarmAgentCount');
    const strategySelect = container.querySelector('#swarmStrategySelect');
    const strategyBadge = container.querySelector('#swarmStrategyBadge');

    let currentAgents = [];

    // Strategy Change
    strategySelect.addEventListener('change', () => {
      strategyBadge.textContent = strategySelect.options[strategySelect.selectedIndex].text;
    });

    // Fetch Topology
    async function loadTopology() {
      try {
        const res = await fetch('/api/swarm/topology');
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const data = await res.json();
        currentAgents = data.agents || [];
        agentCountBadge.textContent = `${currentAgents.length} Agents Active`;
        renderTopology(currentAgents);
      } catch (err) {
        grid.innerHTML = `<div class="deck-error">Failed to load topology: ${err.message}</div>`;
      }
    }

    function renderTopology(agents) {
      grid.innerHTML = agents.map(a => `
        <div class="swarm-agent-card" data-id="${a.id}">
          <div class="swarm-agent-header">
            <span class="swarm-agent-icon">${a.icon}</span>
            <div class="swarm-agent-meta">
              <div class="swarm-agent-name">${a.name}</div>
              <div class="swarm-agent-role">${a.role}</div>
            </div>
            <span class="swarm-status-dot online" title="Status: Online"></span>
          </div>
          <div class="swarm-agent-footer">
            <span class="badge">${a.model}</span>
            <span class="badge badge-subtle">${a.effort} effort</span>
          </div>
        </div>
      `).join('');
    }

    btnRefresh.addEventListener('click', loadTopology);

    // Launch Swarm Dispatch
    btnDispatch.addEventListener('click', async () => {
      const prompt = promptInput.value.trim();
      if (!prompt) {
        promptInput.focus();
        return;
      }

      btnDispatch.disabled = true;
      btnDispatch.textContent = '⚡ Swarm Mobilizing...';
      consensusBox.style.display = 'none';
      agentsStream.innerHTML = `
        <div class="swarm-progress-state">
          <div class="spinner"></div>
          <div>Broadcasting directive to ${currentAgents.length} autonomous agents...</div>
        </div>
      `;

      try {
        const res = await fetch('/api/swarm/dispatch', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ prompt, agent_ids: currentAgents.map(a => a.id) })
        });

        if (!res.ok) {
          const err = await res.json().catch(() => ({}));
          throw new Error(err.error || `HTTP ${res.status}`);
        }

        const reader = res.body.getReader();
        const decoder = new TextDecoder('utf-8');
        let buffer = '';
        agentsStream.innerHTML = '';

        while (true) {
          const { done, value } = await reader.read();
          if (done) break;
          buffer += decoder.decode(value, { stream: true });
          const frames = buffer.split('\n\n');
          buffer = frames.pop() || '';

          for (const frame of frames) {
            const line = frame.trim();
            if (!line.startsWith('data: ')) continue;
            try {
              const evt = JSON.parse(line.substring(6));
              handleSwarmEvent(evt);
            } catch (e) {
              // ignore parse errors
            }
          }
        }
      } catch (err) {
        agentsStream.innerHTML = `<div class="deck-error">Swarm dispatch failed: ${err.message}</div>`;
      } finally {
        btnDispatch.disabled = false;
        btnDispatch.textContent = '🚀 Launch Swarm';
      }
    });

    function handleSwarmEvent(evt) {
      if (evt.type === 'agent_complete') {
        renderAgentResponse(evt.result);
      } else if (evt.type === 'agent_error') {
        renderAgentError(evt.result);
      } else if (evt.type === 'synthesizing') {
        consensusBox.style.display = 'block';
        runMeta.textContent = 'Chief of Staff synthesizing consensus...';
        consensusBody.innerHTML = `<div class="swarm-synthesizing"><div class="spinner"></div> Synthesizing multi-agent perspectives into unified executive consensus...</div>`;
      } else if (evt.type === 'swarm_exit') {
        consensusBox.style.display = 'block';
        runMeta.textContent = `Completed • ${evt.record.results.length} Perspectives Consolidated`;
        consensusBody.innerHTML = `<div class="markdown-body">${formatMarkdown(evt.synthesis)}</div>`;
      }
    }

    function renderAgentResponse(res) {
      const card = document.createElement('div');
      card.className = 'swarm-result-card';
      card.innerHTML = `
        <div class="swarm-result-header">
          <div class="swarm-result-title">
            <span>${res.icon}</span>
            <strong>${res.name}</strong>
            <span class="badge">${res.role}</span>
          </div>
          <div class="swarm-result-meta">
            <span class="badge badge-subtle">${res.latency_ms}ms</span>
            ${res.cost_usd ? `<span class="badge badge-accent">$${res.cost_usd.toFixed(4)}</span>` : ''}
          </div>
        </div>
        <div class="swarm-result-body markdown-body">
          ${formatMarkdown(res.reply)}
        </div>
      `;
      agentsStream.appendChild(card);
    }

    function renderAgentError(res) {
      const card = document.createElement('div');
      card.className = 'swarm-result-card error';
      card.innerHTML = `
        <div class="swarm-result-header">
          <div class="swarm-result-title">
            <span>${res.icon}</span>
            <strong>${res.name}</strong>
            <span class="badge badge-error">Failed</span>
          </div>
        </div>
        <div class="swarm-result-body text-error">${res.reply}</div>
      `;
      agentsStream.appendChild(card);
    }

    function formatMarkdown(text) {
      if (!text) return '';
      return text
        .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
        .replace(/^### (.*$)/gim, '<h3>$1</h3>')
        .replace(/^## (.*$)/gim, '<h2>$1</h2>')
        .replace(/^# (.*$)/gim, '<h1>$1</h1>')
        .replace(/\*\*(.*?)\*\*/gim, '<strong>$1</strong>')
        .replace(/\*(.*?)\*/gim, '<em>$1</em>')
        .replace(/\n\n/gim, '<p></p>')
        .replace(/\n/gim, '<br/>');
    }

    // Swarm History View
    btnHistory.addEventListener('click', async () => {
      try {
        const res = await fetch('/api/swarm/history?limit=15');
        const data = await res.json();
        const runs = data.history || [];
        if (!runs.length) {
          agentsStream.innerHTML = '<div class="deck-empty">No previous swarm runs recorded.</div>';
          return;
        }
        agentsStream.innerHTML = runs.map(r => `
          <div class="swarm-result-card">
            <div class="swarm-result-header">
              <div class="swarm-result-title">
                <strong>Swarm Run ${r.id}</strong>
                <span class="badge">${r.agent_count} agents</span>
              </div>
              <span class="badge badge-subtle">${new Date(r.timestamp * 1000).toLocaleTimeString()}</span>
            </div>
            <div class="swarm-result-directive"><em>"${r.prompt}"</em></div>
            <div class="swarm-result-body markdown-body">${formatMarkdown(r.synthesis)}</div>
          </div>
        `).join('');
      } catch (err) {
        agentsStream.innerHTML = `<div class="deck-error">Failed to load history: ${err.message}</div>`;
      }
    });

    // Initial load
    loadTopology();
  }

  // Register with Command Deck
  if (window.CockpitDeck) {
    window.CockpitDeck.registerView('swarm', {
      title: 'Swarm Mission Control',
      icon: '🐝',
      shortcut: 'Alt+2',
      mount: initSwarmView
    });
  }
})();
