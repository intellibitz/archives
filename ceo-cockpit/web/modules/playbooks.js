// Round 6 — Strategic Executive Playbooks & Prompt Blueprints (Frontend View)
(function() {
  'use strict';

  function initPlaybooksView(container) {
    container.innerHTML = `
      <div class="deck-toolbar">
        <div class="deck-toolbar-left">
          <span class="deck-panel-title">📚 Strategic Executive Playbooks</span>
          <span class="badge badge-accent" id="playbookCountBadge">5 Blueprints</span>
        </div>
        <div class="deck-toolbar-right">
          <button class="deck-btn" id="btnRefreshPlaybooks">🔄 Refresh</button>
          <button class="deck-btn" id="btnPlaybooksHistory">📜 Run History</button>
        </div>
      </div>

      <div class="deck-content-split">
        <!-- Left: Playbook Catalog -->
        <div class="deck-pane deck-pane-left">
          <div class="deck-section-label">Curated Executive Blueprints</div>
          <div class="playbooks-list" id="playbooksList">
            <div class="deck-empty">Loading curated playbooks...</div>
          </div>
        </div>

        <!-- Right: Executive Playbook Report Display -->
        <div class="deck-pane deck-pane-right">
          <div class="deck-section-label">Executive Briefing & Strategic Report</div>
          <div class="playbook-report-card" id="playbookReportCard">
            <div class="playbook-report-header">
              <div class="playbook-report-meta" id="reportHeaderMeta">
                <span class="badge badge-subtle">No Active Run</span>
              </div>
              <div class="playbook-report-actions" id="reportActions" style="display: none;">
                <button class="deck-btn" id="btnCopyReport">📋 Copy Report</button>
                <button class="deck-btn" id="btnSpeakReport">🔊 Read Aloud</button>
              </div>
            </div>
            <div class="playbook-report-content markdown-body" id="reportContent">
              <div class="deck-empty">Select an executive playbook on the left to launch an autonomous strategic assessment.</div>
            </div>
          </div>
        </div>
      </div>
    `;

    // Elements
    const list = container.querySelector('#playbooksList');
    const reportCard = container.querySelector('#playbookReportCard');
    const reportMeta = container.querySelector('#reportHeaderMeta');
    const reportActions = container.querySelector('#reportActions');
    const reportContent = container.querySelector('#reportContent');
    const btnRefresh = container.querySelector('#btnRefreshPlaybooks');
    const btnHistory = container.querySelector('#btnPlaybooksHistory');
    const btnCopy = container.querySelector('#btnCopyReport');
    const btnSpeak = container.querySelector('#btnSpeakReport');

    let currentPlaybooks = [];
    let activeReportMarkdown = '';

    async function loadPlaybooks() {
      try {
        const res = await fetch('/api/playbooks');
        const data = await res.json();
        currentPlaybooks = data.playbooks || [];
        renderPlaybooks(currentPlaybooks);
      } catch (err) {
        list.innerHTML = `<div class="deck-error">Failed to load playbooks: ${err.message}</div>`;
      }
    }

    function renderPlaybooks(playbooks) {
      list.innerHTML = playbooks.map(p => `
        <div class="playbook-card" data-id="${p.id}">
          <div class="playbook-card-header">
            <span class="playbook-card-icon">${p.icon}</span>
            <div class="playbook-card-info">
              <div class="playbook-card-title">${p.title}</div>
              <div class="playbook-card-category">${p.category}</div>
            </div>
          </div>
          <div class="playbook-card-desc">${p.desc}</div>
          <div class="playbook-card-footer">
            <span class="badge">${p.model}</span>
            <button class="btn btn-primary btn-sm btn-run-pb" data-id="${p.id}">⚡ Run Blueprint</button>
          </div>
        </div>
      `).join('');

      // Wire run buttons
      list.querySelectorAll('.btn-run-pb').forEach(btn => {
        btn.addEventListener('click', (e) => {
          e.stopPropagation();
          runPlaybook(btn.dataset.id);
        });
      });
    }

    async function runPlaybook(pbId) {
      const pb = currentPlaybooks.find(p => p.id === pbId);
      if (!pb) return;

      reportMeta.innerHTML = `
        <span class="badge badge-accent">Running: ${pb.title}</span>
        <span class="text-muted">Mobilizing frontier model...</span>
      `;
      reportActions.style.display = 'none';
      reportContent.innerHTML = `
        <div class="swarm-progress-state">
          <div class="spinner"></div>
          <div>Synthesizing strategic intelligence for <strong>${pb.title}</strong>...</div>
        </div>
      `;

      try {
        const res = await fetch('/api/playbooks/run', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ playbook_id: pbId })
        });
        const data = await res.json();
        if (!data.success) throw new Error(data.error || 'Execution failed');

        const run = data.run;
        activeReportMarkdown = run.report;
        reportMeta.innerHTML = `
          <strong>${run.icon} ${run.title}</strong>
          <span class="badge badge-subtle">${run.duration_ms}ms</span>
          <span class="badge">${run.model}</span>
          ${run.cost_usd ? `<span class="badge badge-accent">$${run.cost_usd.toFixed(4)}</span>` : ''}
        `;
        reportActions.style.display = 'flex';
        reportContent.innerHTML = formatMarkdown(run.report);

        if (window.Cockpit) {
          window.Cockpit.notify(`Playbook '${run.title}' completed in ${run.duration_ms}ms`, 'success');
        }
      } catch (err) {
        reportContent.innerHTML = `<div class="deck-error">Playbook execution failed: ${err.message}</div>`;
      }
    }

    // Copy Report
    btnCopy.addEventListener('click', () => {
      if (activeReportMarkdown) {
        navigator.clipboard.writeText(activeReportMarkdown);
        if (window.Cockpit) window.Cockpit.notify('Playbook report copied to clipboard.', 'info');
      }
    });

    // Voice Engine Read Aloud
    btnSpeak.addEventListener('click', () => {
      if (window.CockpitVoice && activeReportMarkdown) {
        window.CockpitVoice.speak(activeReportMarkdown);
      }
    });

    // Playbook History
    btnHistory.addEventListener('click', async () => {
      try {
        const res = await fetch('/api/playbooks/history?limit=10');
        const data = await res.json();
        const runs = data.history || [];
        if (!runs.length) {
          reportContent.innerHTML = '<div class="deck-empty">No previous playbook runs recorded.</div>';
          return;
        }
        reportMeta.innerHTML = `<span class="badge badge-accent">Playbook Run History (${runs.length})</span>`;
        reportActions.style.display = 'none';
        reportContent.innerHTML = runs.map(r => `
          <div class="swarm-result-card" style="margin-bottom: 12px;">
            <div class="swarm-result-header">
              <div class="swarm-result-title">
                <span>${r.icon}</span>
                <strong>${r.title}</strong>
                <span class="badge badge-subtle">${new Date(r.timestamp * 1000).toLocaleTimeString()}</span>
              </div>
              <span class="badge">${r.duration_ms}ms</span>
            </div>
            <div class="swarm-result-body markdown-body">${formatMarkdown(r.report)}</div>
          </div>
        `).join('');
      } catch (err) {
        reportContent.innerHTML = `<div class="deck-error">Failed to load history: ${err.message}</div>`;
      }
    });

    btnRefresh.addEventListener('click', loadPlaybooks);

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

    loadPlaybooks();
  }

  // Register in Command Deck
  if (window.CockpitDeck) {
    window.CockpitDeck.registerView('playbooks', {
      title: 'Executive Playbooks',
      icon: '📚',
      shortcut: 'Alt+4',
      mount: initPlaybooksView
    });
  }
})();
