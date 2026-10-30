/* =============================================================================
 * Round 15 — Cross-Harness Interop & Gateway Matrix
 * Probing LiteLLM, Redis, Qdrant, and Ollama in real-time.
 * ========================================================================== */
(function () {
  'use strict';

  const D = () => window.CockpitDeck;

  async function render(view) {
    view.innerHTML = `
      <div class="deck-split-view">
        <div class="deck-pane" style="flex: 2;">
          <div class="deck-pane-header">
            <h3>🌐 AI Infrastructure Gateway Matrix</h3>
            <button class="deck-btn-sm" id="btnProbeGateways">🔄 Probe All</button>
          </div>
          <div id="gatewayStatsGrid" class="deck-stats-grid">
            <div class="deck-stat-card"><span class="deck-stat-val text-green" id="statHealthyGateways">—</span><span class="deck-stat-lbl">Online Gateways</span></div>
            <div class="deck-stat-card"><span class="deck-stat-val text-cyan" id="statAvgLatency">—</span><span class="deck-stat-lbl">Avg Response Latency</span></div>
            <div class="deck-stat-card"><span class="deck-stat-val text-purple">Zero-Cost First</span><span class="deck-stat-lbl">Routing Policy</span></div>
          </div>

          <div style="margin-top:16px;">
            <h4>Gateway Nodes</h4>
            <div id="gatewayNodesGrid" style="display:grid;grid-template-columns:repeat(auto-fit, minmax(260px, 1fr));gap:12px;margin-top:8px;">
              <p class="deck-muted">Probing infrastructure nodes...</p>
            </div>
          </div>
        </div>

        <div class="deck-pane" style="flex: 1;">
          <div class="deck-pane-header">
            <h3>🔀 Failover & Routing Chain</h3>
          </div>
          <div id="routingChainList" style="display:flex;flex-direction:column;gap:10px;margin-top:10px;">
            <p class="deck-muted">Loading routing matrix...</p>
          </div>
        </div>
      </div>
    `;

    document.getElementById('btnProbeGateways').addEventListener('click', probeGateways);
    await probeGateways();
    await loadRoutingChain();
  }

  async function probeGateways() {
    try {
      const data = await D().api('/api/gateways/ping-all', { method: 'POST' });
      const gws = data.gateways || [];

      document.getElementById('statHealthyGateways').textContent = `${data.healthy_count ?? 0} / ${data.total_count ?? 0}`;

      const validLatencies = gws.filter(g => g.status !== 'OFFLINE').map(g => g.latency_ms);
      const avgLat = validLatencies.length ? Math.round(validLatencies.reduce((a, b) => a + b, 0) / validLatencies.length) : 0;
      document.getElementById('statAvgLatency').textContent = `${avgLat}ms`;

      const host = document.getElementById('gatewayNodesGrid');
      if (!host) return;

      host.innerHTML = gws.map(g => {
        const isOnline = g.status === 'HEALTHY' || g.status === 'DEGRADED';
        const statusClass = g.status === 'HEALTHY' ? 'green' : (g.status === 'DEGRADED' ? 'amber' : 'rose');
        return `
          <div style="background:#1E293B;border:1px solid #334155;border-radius:8px;padding:14px;display:flex;flex-direction:column;justify-content:space-between;">
            <div>
              <div style="display:flex;justify-content:space-between;align-items:center;">
                <strong style="color:#F8FAFC;font-size:14px;">${escapeHtml(g.name)}</strong>
                <span class="deck-status-pill ${statusClass}">${escapeHtml(g.status)}</span>
              </div>
              <div style="font-size:12px;color:#94A3B8;margin-top:4px;">Port: <strong>${g.port}</strong> · ${escapeHtml(g.protocol)}</div>
              <p style="font-size:11px;color:#64748B;margin:6px 0 0;">${escapeHtml(g.role)}</p>
            </div>
            <div style="display:flex;justify-content:space-between;align-items:center;margin-top:12px;padding-top:8px;border-top:1px solid #334155;font-size:12px;">
              <span style="color:#94A3B8;">Latency:</span>
              <span style="color:${isOnline ? '#38BDF8' : '#F43F5E'};font-weight:bold;">${isOnline ? `${g.latency_ms}ms` : 'Unreachable'}</span>
            </div>
          </div>
        `;
      }).join('');
    } catch (err) {
      console.error('Probe failed:', err);
    }
  }

  async function loadRoutingChain() {
    try {
      const data = await D().api('/api/gateways/routing-matrix');
      const chain = data.failover_chain || [];
      const host = document.getElementById('routingChainList');
      if (!host) return;

      host.innerHTML = chain.map((c, i) => `
        <div style="background:#0F172A;border:1px solid #1E293B;border-radius:6px;padding:10px;">
          <div style="display:flex;justify-content:space-between;align-items:center;">
            <span style="color:#38BDF8;font-weight:bold;font-size:12px;">Tier ${c.tier}: ${escapeHtml(c.name)}</span>
            <span class="deck-badge" style="background:#0284C7;color:#fff;">${escapeHtml(c.cost)}</span>
          </div>
          <div style="font-size:12px;color:#F1F5F9;margin-top:4px;">${escapeHtml(c.target)}</div>
          <div style="font-size:11px;color:#64748B;margin-top:2px;">Expected Latency: ${escapeHtml(c.latency)}</div>
        </div>
      `).join('');
    } catch (err) {
      console.error('Routing load error:', err);
    }
  }

  function escapeHtml(str) {
    return String(str || '').replace(/[&<>"']/g, c => ({
      '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
    }[c]));
  }

  CockpitDeck.registerView('gateways', {
    title: 'Cross-Harness Gateway Matrix',
    navLabel: 'Gateways',
    icon: '🌐',
    order: 85,
    render: render
  });
})();
