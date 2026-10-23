/* =============================================================================
 * Round 17 — Autonomous Repository Benchmark & Test Intelligence
 * Test suite velocity, model throughput, and latency leaderboard.
 * ========================================================================== */
(function () {
  'use strict';

  const D = () => window.CockpitDeck;

  async function render(view) {
    view.innerHTML = `
      <div class="deck-split-view">
        <div class="deck-pane" style="flex: 2;">
          <div class="deck-pane-header">
            <h3>📊 Autonomous Test & Benchmark Intelligence</h3>
            <div style="display:flex;gap:8px;">
              <button class="deck-btn-sm" id="btnRefreshBenchmarks">🔄 Refresh</button>
              <button class="deck-btn-sm primary" id="btnTriggerTestRun">🚀 Run Test Suite</button>
            </div>
          </div>
          <div id="benchmarkStatsGrid" class="deck-stats-grid">
            <div class="deck-stat-card"><span class="deck-stat-val text-green" id="statPassRate">100%</span><span class="deck-stat-lbl">Latest Pass Rate</span></div>
            <div class="deck-stat-card"><span class="deck-stat-val text-cyan" id="statTestDuration">1.2s</span><span class="deck-stat-lbl">Suite Latency</span></div>
            <div class="deck-stat-card"><span class="deck-stat-val text-purple" id="statTotalChecks">52</span><span class="deck-stat-lbl">Passing Checks</span></div>
          </div>

          <div style="margin-top:16px;">
            <h4>Model Inference Speed Leaderboard</h4>
            <div class="deck-table-container">
              <table class="deck-table" id="modelLeaderboardTable">
                <thead>
                  <tr>
                    <th>Model Tier</th>
                    <th>Provider</th>
                    <th>Throughput</th>
                    <th>First Token</th>
                    <th>Cost / 1M</th>
                    <th>Classification</th>
                  </tr>
                </thead>
                <tbody id="modelLeaderboardBody">
                  <tr><td colspan="6" class="deck-muted">Loading leaderboard...</td></tr>
                </tbody>
              </table>
            </div>
          </div>

          <div style="margin-top:16px;">
            <h4>Recent Benchmark & Verification History</h4>
            <div id="benchmarkHistoryList" style="display:flex;flex-direction:column;gap:8px;max-height:220px;overflow-y:auto;padding-right:4px;">
              <p class="deck-muted">Loading history...</p>
            </div>
          </div>
        </div>

        <div class="deck-pane" style="flex: 1;">
          <div class="deck-pane-header">
            <h3>⚡ Live Model Speed Probe</h3>
          </div>
          <p style="font-size:12px;color:#94A3B8;margin-top:0;">Send an instant benchmark probe to measure tokens/sec on active gateway.</p>
          <div class="deck-form-group">
            <label style="font-size:12px;color:#94A3B8;">Target Model:</label>
            <select id="probeModelSelect" class="harness-select" style="width:100%;margin-bottom:12px;">
              <option value="gemini-flash">Gemini 2.5/3.8 Flash (Fast Tier)</option>
              <option value="deepseek-reasoner">DeepSeek Reasoner (Logic Tier)</option>
              <option value="claude-3-5-sonnet">Claude 3.5 Sonnet (Architecture)</option>
            </select>
            <button id="btnProbeModelSpeed" class="ws-apply-btn" style="width:100%;">Probe Inference Speed</button>
          </div>
          <div id="probeSpeedResultCard" style="margin-top:16px;display:none;background:#0F172A;border:1px solid #1E293B;border-radius:8px;padding:12px;">
          </div>
        </div>
      </div>
    `;

    document.getElementById('btnRefreshBenchmarks').addEventListener('click', loadData);
    document.getElementById('btnTriggerTestRun').addEventListener('click', triggerTestRun);
    document.getElementById('btnProbeModelSpeed').addEventListener('click', probeModelSpeed);

    await loadData();
  }

  async function loadData() {
    try {
      const [histData, leadData] = await Promise.all([
        D().api('/api/benchmark/history'),
        D().api('/api/benchmark/leaderboard')
      ]);

      // Populate leaderboard
      const leadBody = document.getElementById('modelLeaderboardBody');
      if (leadBody && leadData.leaderboard) {
        leadBody.innerHTML = leadData.leaderboard.map(m => `
          <tr>
            <td><strong>${escapeHtml(m.model)}</strong></td>
            <td><span class="deck-badge">${escapeHtml(m.provider)}</span></td>
            <td><span class="text-cyan" style="font-weight:bold;">${m.speed_tps} tok/s</span></td>
            <td>${m.latency_ms}ms</td>
            <td>${escapeHtml(m.cost_1m)}</td>
            <td><span class="deck-status-pill green">${escapeHtml(m.tier)}</span></td>
          </tr>
        `).join('');
      }

      // Populate history
      const histHost = document.getElementById('benchmarkHistoryList');
      if (histHost && histData.runs) {
        histHost.innerHTML = histData.runs.map(r => `
          <div style="background:#1E293B;border:1px solid #334155;border-radius:6px;padding:10px;display:flex;justify-content:space-between;align-items:center;">
            <div>
              <strong style="color:#F8FAFC;font-size:13px;">${escapeHtml(r.name || r.model || r.type)}</strong>
              <div style="font-size:11px;color:#94A3B8;margin-top:2px;">
                ${r.type === 'TEST_SUITE' ? `${r.passed} / ${r.total} passed in ${r.duration_sec}s` : `${r.tokens_per_second} tok/s · ${r.first_token_latency_ms}ms latency`}
              </div>
            </div>
            <span class="deck-status-pill green">${r.type === 'TEST_SUITE' ? `${r.pass_rate_pct}% PASS` : 'BENCHMARKED'}</span>
          </div>
        `).join('');
      }
    } catch (err) {
      console.error('Benchmark load error:', err);
    }
  }

  async function triggerTestRun() {
    const btn = document.getElementById('btnTriggerTestRun');
    btn.disabled = true;
    btn.textContent = '⏳ Testing...';

    try {
      const res = await D().api('/api/benchmark/run-tests', { method: 'POST' });
      alert(`Test Run Complete: ${res.message}`);
      await loadData();
    } catch (err) {
      alert(`Test run error: ${err.message}`);
    } finally {
      btn.disabled = false;
      btn.textContent = '🚀 Run Test Suite';
    }
  }

  async function probeModelSpeed() {
    const model = document.getElementById('probeModelSelect').value;
    const card = document.getElementById('probeSpeedResultCard');
    card.style.display = 'block';
    card.innerHTML = '<span class="deck-muted">Measuring inference throughput...</span>';

    try {
      const res = await D().api('/api/benchmark/model-speed', { method: 'POST', body: { model } });
      const b = res.benchmark || {};
      card.innerHTML = `
        <div style="color:#A7F3D0;font-weight:bold;margin-bottom:6px;">Probe Result for ${escapeHtml(b.model)}:</div>
        <div style="font-size:13px;color:#F1F5F9;">⚡ Throughput: <strong class="text-cyan">${b.tokens_per_second} tokens/sec</strong></div>
        <div style="font-size:13px;color:#F1F5F9;margin-top:4px;">⏱️ Latency: <strong>${b.first_token_latency_ms}ms</strong></div>
      `;
      await loadData();
    } catch (err) {
      card.innerHTML = `<span class="text-rose">Probe failed: ${err.message}</span>`;
    }
  }

  function escapeHtml(str) {
    return String(str || '').replace(/[&<>"']/g, c => ({
      '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
    }[c]));
  }

  CockpitDeck.registerView('benchmark', {
    title: 'Autonomous Test & Model Benchmarks',
    navLabel: 'Benchmarks',
    icon: '📊',
    order: 95,
    render: render
  });
})();
