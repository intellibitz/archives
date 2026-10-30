/* =============================================================================
 * Round 11 — Spend Forecaster & Runway Predictor
 * Real-time financial forecasting, token velocity, and runway telemetry.
 * ========================================================================== */
(function () {
  'use strict';

  const D = () => window.CockpitDeck;

  async function render(view) {
    view.innerHTML = `
      <div class="deck-split-view">
        <div class="deck-pane">
          <div class="deck-pane-header">
            <h3>📈 Live Runway & Spend Velocity</h3>
            <button class="deck-btn-sm" id="btnRefreshForecaster">🔄 Refresh</button>
          </div>
          <div id="forecasterRunwayStats" class="deck-stats-grid">
            <div class="deck-stat-card"><span class="deck-stat-val text-cyan">Loading...</span><span class="deck-stat-lbl">Runway</span></div>
          </div>
          <div style="margin-top:16px;">
            <h4>Model Burn Distribution (Past 24h)</h4>
            <div id="forecasterModelList" class="deck-table-container"></div>
          </div>
        </div>
        <div class="deck-pane">
          <div class="deck-pane-header">
            <h3>🔮 What-If Model Spend Simulator</h3>
          </div>
          <div class="deck-form-group">
            <label>Select Target Model:</label>
            <select id="simModelSelect" class="harness-select" style="width:100%;margin-bottom:12px;">
              <option value="deepseek-reasoner">DeepSeek Reasoner (R1) — $0.55 / $2.19</option>
              <option value="gemini-flash" selected>Gemini 2.5/3.8 Flash — $0.075 / $0.300</option>
              <option value="claude-3-5-sonnet">Claude 3.5 Sonnet — $3.00 / $15.00</option>
              <option value="siliconflow-qwen-coder">Qwen 2.5 Coder 32B — $0.20 / $0.40</option>
            </select>
            <label>Projected Queries / Day:</label>
            <input type="number" id="simDailyQueries" class="ws-path-input" value="150" style="width:100%;margin-bottom:12px;" />
            <label>Avg Tokens per Turn (Prompt + Output):</label>
            <input type="number" id="simAvgTokens" class="ws-path-input" value="3500" style="width:100%;margin-bottom:16px;" />
            <button id="btnRunSimulation" class="ws-apply-btn" style="width:100%;">Calculate Projected Impact</button>
          </div>
          <div id="simulationResultCard" style="margin-top:16px;display:none;"></div>
        </div>
      </div>
    `;

    document.getElementById('btnRefreshForecaster').addEventListener('click', loadForecastData);
    document.getElementById('btnRunSimulation').addEventListener('click', runSimulation);
    await loadForecastData();
  }

  async function loadForecastData() {
    try {
      const data = await D().api('/api/forecast/runway');
      const r = data.runway || {};
      const statsHost = document.getElementById('forecasterRunwayStats');
      if (statsHost) {
        const riskClass = r.risk_level === 'CRITICAL' ? 'text-rose' : (r.risk_level === 'WARNING' ? 'text-amber' : 'text-green');
        statsHost.innerHTML = `
          <div class="deck-stat-card"><span class="deck-stat-val ${riskClass}">${r.hours_to_daily_cap}h</span><span class="deck-stat-lbl">Hours to Daily Cap</span></div>
          <div class="deck-stat-card"><span class="deck-stat-val">$${(r.burn_per_hour_usd || 0).toFixed(4)}/h</span><span class="deck-stat-lbl">Burn Velocity</span></div>
          <div class="deck-stat-card"><span class="deck-stat-val text-cyan">$${(r.projected_24h_burn_usd || 0).toFixed(2)}</span><span class="deck-stat-lbl">Projected 24h Spend</span></div>
          <div class="deck-stat-card"><span class="deck-stat-val text-green">$${(r.remaining_daily_usd || 0).toFixed(2)}</span><span class="deck-stat-lbl">Daily Cap Remaining</span></div>
        `;
      }

      const listHost = document.getElementById('forecasterModelList');
      if (listHost && r.model_distribution) {
        const entries = Object.entries(r.model_distribution);
        if (entries.length === 0) {
          listHost.innerHTML = '<p class="deck-muted" style="padding:10px;">No billable calls in past 24h.</p>';
        } else {
          listHost.innerHTML = `
            <table class="deck-table">
              <thead><tr><th>Model</th><th>Calls</th><th>Spend (USD)</th></tr></thead>
              <tbody>
                ${entries.map(([m, val]) => `<tr><td><strong>${D().esc(m)}</strong></td><td>${val.calls}</td><td>$${val.spend_usd.toFixed(4)}</td></tr>`).join('')}
              </tbody>
            </table>
          `;
        }
      }
    } catch (err) {
      console.warn('Forecast fetch failed', err);
    }
  }

  async function runSimulation() {
    const model = document.getElementById('simModelSelect').value;
    const queries = parseInt(document.getElementById('simDailyQueries').value, 10) || 100;
    const tokens = parseInt(document.getElementById('simAvgTokens').value, 10) || 3000;
    const inTokens = Math.round(tokens * 0.7);
    const outTokens = Math.round(tokens * 0.3);

    try {
      const res = await D().api('/api/forecast/simulate', {
        body: { model, queries_per_day: queries, avg_input_tokens: inTokens, avg_output_tokens: outTokens }
      });
      const s = res.simulation;
      const resCard = document.getElementById('simulationResultCard');
      resCard.style.display = 'block';
      const fits = s.fits_daily_budget ? 'text-green' : 'text-rose';
      resCard.innerHTML = `
        <div style="padding:14px;background:rgba(15,23,42,0.8);border:1px solid rgba(255,255,255,0.1);border-radius:8px;">
          <h4 style="margin:0 0 8px 0;color:#67E8F9;">Simulation Results: ${D().esc(model)}</h4>
          <p>Cost per turn: <strong>$${s.cost_per_query_usd.toFixed(5)} (${s.cost_per_query_cents.toFixed(2)}¢)</strong></p>
          <p>Projected Daily Spend: <strong class="${fits}">$${s.projected_daily_spend_usd.toFixed(2)} / day (${s.budget_utilization_pct}% of cap)</strong></p>
          <p>Projected Monthly Run-Rate: <strong>$${s.projected_monthly_spend_usd.toFixed(2)} / mo</strong></p>
          <p style="margin-top:6px;font-size:12px;color:#94A3B8;">${s.fits_daily_budget ? '✅ Within Daily Budget Limits' : '⚠️ Exceeds Current Daily Budget Cap'}</p>
        </div>
      `;
    } catch (err) {
      alert(`Simulation error: ${err.message}`);
    }
  }

  CockpitDeck.registerView('forecaster', {
    title: 'Spend Forecaster & Runway',
    navLabel: 'Forecaster',
    icon: '📈',
    order: 65,
    shortcut: 'Alt+7',
    render: render
  });
})();
