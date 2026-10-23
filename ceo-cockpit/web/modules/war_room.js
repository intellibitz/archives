/* =============================================================================
 * Round 16 — Executive Incident War Room & Auto-Recovery
 * Real-time incident tracking, severity grading (P1-P4), and automated RCA.
 * ========================================================================== */
(function () {
  'use strict';

  const D = () => window.CockpitDeck;

  async function render(view) {
    view.innerHTML = `
      <div class="deck-split-view">
        <div class="deck-pane" style="flex: 2;">
          <div class="deck-pane-header">
            <h3>🚨 Executive Incident War Room</h3>
            <div style="display:flex;gap:8px;">
              <button class="deck-btn-sm" id="btnRefreshWarRoom">🔄 Refresh</button>
              <button class="deck-btn-sm primary" id="btnDeclareIncidentModal">⚡ Declare Incident</button>
            </div>
          </div>
          <div id="warRoomStatusBanner" style="padding:14px;border-radius:8px;margin-bottom:16px;display:flex;justify-content:space-between;align-items:center;">
            <!-- Dynamic War Room Banner -->
          </div>
          <div id="warRoomStatsGrid" class="deck-stats-grid">
            <div class="deck-stat-card"><span class="deck-stat-val text-rose" id="statActiveIncidents">—</span><span class="deck-stat-lbl">Active Incidents</span></div>
            <div class="deck-stat-card"><span class="deck-stat-val text-green" id="statTotalIncidents">—</span><span class="deck-stat-lbl">Total Logged</span></div>
            <div class="deck-stat-card"><span class="deck-stat-val text-cyan">Autonomous RCA</span><span class="deck-stat-lbl">Triage Engine</span></div>
          </div>

          <div style="margin-top:16px;">
            <h4>Incident Feed & Triage Board</h4>
            <div id="incidentsListHost" style="display:flex;flex-direction:column;gap:12px;margin-top:8px;max-height:420px;overflow-y:auto;padding-right:4px;">
              <p class="deck-muted">Loading incident stream...</p>
            </div>
          </div>
        </div>

        <div class="deck-pane" style="flex: 1;">
          <div class="deck-pane-header">
            <h3>📋 Severity Protocol Reference</h3>
          </div>
          <div style="display:flex;flex-direction:column;gap:10px;font-size:12px;color:#94A3B8;">
            <div style="background:#0F172A;border-left:4px solid #F43F5E;padding:8px 12px;border-radius:4px;">
              <strong style="color:#F43F5E;">P1 — Critical Outage:</strong> Hard gateway disconnect, budget exhaustion trip, total model failure.
            </div>
            <div style="background:#0F172A;border-left:4px solid #FB923C;padding:8px 12px;border-radius:4px;">
              <strong style="color:#FB923C;">P2 — High Severity:</strong> Model degraded, upstream 429 burst, high latency (>2s).
            </div>
            <div style="background:#0F172A;border-left:4px solid #FBBF24;padding:8px 12px;border-radius:4px;">
              <strong style="color:#FBBF24;">P3 — Moderate:</strong> Minor rate limits, cache misses, single-agent timeout.
            </div>
            <div style="background:#0F172A;border-left:4px solid #38BDF8;padding:8px 12px;border-radius:4px;">
              <strong style="color:#38BDF8;">P4 — Informational:</strong> Workspace hygiene alerts, temporary diagnostic clutter.
            </div>
          </div>
        </div>
      </div>

      <!-- Declare Incident Modal -->
      <div id="declareIncModalBackdrop" style="display:none;position:fixed;top:0;left:0;right:0;bottom:0;background:rgba(0,0,0,0.7);z-index:9999;align-items:center;justify-content:center;">
        <div style="background:#1E293B;border:1px solid #334155;border-radius:12px;width:480px;padding:24px;box-shadow:0 20px 40px rgba(0,0,0,0.5);">
          <h3 style="color:#F8FAFC;margin-top:0;">Declare Executive Incident</h3>
          <label style="color:#94A3B8;font-size:12px;display:block;margin-top:12px;">Incident Title:</label>
          <input type="text" id="newIncTitle" class="ws-path-input" placeholder="e.g. LiteLLM Port 4000 upstream disconnect" style="width:100%;margin-top:4px;" />
          <label style="color:#94A3B8;font-size:12px;display:block;margin-top:12px;">Severity Level:</label>
          <select id="newIncSeverity" class="harness-select" style="width:100%;margin-top:4px;">
            <option value="P1">P1 — Critical Outage</option>
            <option value="P2">P2 — High Severity</option>
            <option value="P3" selected>P3 — Moderate</option>
            <option value="P4">P4 — Informational / Hygiene</option>
          </select>
          <label style="color:#94A3B8;font-size:12px;display:block;margin-top:12px;">Affected Component:</label>
          <input type="text" id="newIncComponent" class="ws-path-input" value="LiteLLM Gateway" style="width:100%;margin-top:4px;" />
          <label style="color:#94A3B8;font-size:12px;display:block;margin-top:12px;">Details / Symptoms:</label>
          <textarea id="newIncDetails" class="ws-path-input" rows="3" placeholder="Observed error codes, latency spikes, or failed requests..." style="width:100%;margin-top:4px;resize:vertical;"></textarea>
          <div style="display:flex;justify-content:flex-end;gap:8px;margin-top:20px;">
            <button id="btnCancelDeclareInc" class="deck-btn-sm">Cancel</button>
            <button id="btnConfirmDeclareInc" class="ws-apply-btn" style="background:#E11D48;">Declare & Alert</button>
          </div>
        </div>
      </div>
    `;

    document.getElementById('btnRefreshWarRoom').addEventListener('click', loadIncidents);
    document.getElementById('btnDeclareIncidentModal').addEventListener('click', () => {
      document.getElementById('declareIncModalBackdrop').style.display = 'flex';
    });
    document.getElementById('btnCancelDeclareInc').addEventListener('click', () => {
      document.getElementById('declareIncModalBackdrop').style.display = 'none';
    });
    document.getElementById('btnConfirmDeclareInc').addEventListener('click', declareIncident);

    await loadIncidents();
  }

  async function loadIncidents() {
    try {
      const data = await D().api('/api/war-room/incidents');
      const incidents = data.incidents || [];
      const activeCount = data.active_count ?? 0;

      document.getElementById('statActiveIncidents').textContent = activeCount;
      document.getElementById('statTotalIncidents').textContent = data.total_count ?? 0;

      const banner = document.getElementById('warRoomStatusBanner');
      if (banner) {
        if (activeCount > 0) {
          banner.style.background = '#4C0519';
          banner.style.border = '1px solid #E11D48';
          banner.innerHTML = `
            <div>
              <strong style="color:#FDA4AF;font-size:14px;">⚠️ WAR ROOM ACTIVE: ${activeCount} UNRESOLVED INCIDENT(S)</strong>
              <div style="font-size:12px;color:#FECDD3;">Automated containment procedures initiated. Review and triage below.</div>
            </div>
            <span class="deck-badge" style="background:#E11D48;color:#fff;">TRIAGE NEEDED</span>
          `;
        } else {
          banner.style.background = '#064E3B';
          banner.style.border = '1px solid #059669';
          banner.innerHTML = `
            <div>
              <strong style="color:#A7F3D0;font-size:14px;">🛡️ ALL SYSTEMS NOMINAL</strong>
              <div style="font-size:12px;color:#D1FAE5;">Zero active incidents. All gateways, budgets, and services operational.</div>
            </div>
            <span class="deck-badge" style="background:#059669;color:#fff;">STANDBY</span>
          `;
        }
      }

      const host = document.getElementById('incidentsListHost');
      if (!host) return;

      if (incidents.length === 0) {
        host.innerHTML = '<p class="deck-muted" style="text-align:center;padding:20px;">No incidents recorded.</p>';
        return;
      }

      host.innerHTML = incidents.map(inc => {
        const isAct = inc.status === 'ACTIVE';
        const sevColor = inc.severity === 'P1' ? '#E11D48' : (inc.severity === 'P2' ? '#FB923C' : '#38BDF8');
        return `
          <div style="background:#1E293B;border:1px solid ${isAct ? '#E11D48' : '#334155'};border-radius:8px;padding:14px;">
            <div style="display:flex;justify-content:space-between;align-items:center;">
              <div>
                <span class="deck-badge" style="background:${sevColor};color:#fff;font-weight:bold;">${escapeHtml(inc.severity)}</span>
                <strong style="color:#F8FAFC;font-size:14px;margin-left:8px;">${escapeHtml(inc.title)}</strong>
              </div>
              <span class="deck-status-pill ${isAct ? 'rose' : 'green'}">${escapeHtml(inc.status)}</span>
            </div>
            <div style="font-size:12px;color:#94A3B8;margin-top:6px;">
              Component: <strong>${escapeHtml(inc.affected_component)}</strong> · Declared: ${new Date(inc.created_at * 1000).toLocaleString()}
            </div>
            <div style="background:#0F172A;border-radius:6px;padding:10px;margin-top:10px;font-size:12px;color:#CBD5E1;">
              <p style="margin:0 0 4px;color:#38BDF8;font-weight:bold;">Root Cause Analysis (RCA):</p>
              <p style="margin:0;">${escapeHtml(inc.rca)}</p>
              ${inc.containment ? `<p style="margin:8px 0 0;color:#A7F3D0;font-weight:bold;">Containment Plan:</p><p style="margin:2px 0 0;white-space:pre-line;">${escapeHtml(inc.containment)}</p>` : ''}
            </div>
            ${isAct ? `
              <div style="display:flex;justify-content:flex-end;gap:8px;margin-top:10px;">
                <button class="deck-btn-sm primary btn-triage-inc" data-id="${inc.id}">🧠 Auto-Triage RCA</button>
                <button class="deck-btn-sm btn-resolve-inc" data-id="${inc.id}" style="background:#059669;color:#fff;">✅ Resolve Incident</button>
              </div>
            ` : ''}
          </div>
        `;
      }).join('');

      host.querySelectorAll('.btn-triage-inc').forEach(btn => {
        btn.addEventListener('click', () => triageIncident(btn.getAttribute('data-id')));
      });
      host.querySelectorAll('.btn-resolve-inc').forEach(btn => {
        btn.addEventListener('click', () => resolveIncident(btn.getAttribute('data-id')));
      });

    } catch (err) {
      console.error('Failed to load incidents:', err);
    }
  }

  async function declareIncident() {
    const title = document.getElementById('newIncTitle').value.trim();
    const severity = document.getElementById('newIncSeverity').value;
    const component = document.getElementById('newIncComponent').value.trim();
    const details = document.getElementById('newIncDetails').value.trim();

    if (!title) {
      alert('Incident title is required');
      return;
    }

    try {
      await D().api('/api/war-room/create', {
        method: 'POST',
        body: { title, severity, affected_component: component, details }
      });
      document.getElementById('declareIncModalBackdrop').style.display = 'none';
      await loadIncidents();
    } catch (err) {
      alert(`Declaration failed: ${err.message}`);
    }
  }

  async function triageIncident(id) {
    try {
      await D().api('/api/war-room/triage', { method: 'POST', body: { id } });
      await loadIncidents();
    } catch (err) {
      alert(`Triage failed: ${err.message}`);
    }
  }

  async function resolveIncident(id) {
    try {
      await D().api('/api/war-room/resolve', {
        method: 'POST',
        body: { id, resolution_notes: 'Remediated via Executive War Room action.' }
      });
      await loadIncidents();
    } catch (err) {
      alert(`Resolve failed: ${err.message}`);
    }
  }

  function escapeHtml(str) {
    return String(str || '').replace(/[&<>"']/g, c => ({
      '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
    }[c]));
  }

  CockpitDeck.registerView('war_room', {
    title: 'Executive Incident War Room',
    navLabel: 'War Room',
    icon: '🚨',
    order: 90,
    render: render
  });
})();
