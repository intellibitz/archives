/* =============================================================================
 * Round 19 — Environment & Secrets Guardian
 * Audits environment variables, AI provider credentials, and token hygiene.
 * ========================================================================== */
(function () {
  'use strict';

  const D = () => window.CockpitDeck;

  async function render(view) {
    view.innerHTML = `
      <div class="deck-split-view">
        <div class="deck-pane" style="flex: 2;">
          <div class="deck-pane-header">
            <h3>🔑 Environment & Secrets Guardian</h3>
            <button class="deck-btn-sm" id="btnRefreshSecrets">🔄 Re-Audit</button>
          </div>
          <div id="secretsStatsGrid" class="deck-stats-grid">
            <div class="deck-stat-card"><span class="deck-stat-val text-green" id="statSecGrade">—</span><span class="deck-stat-lbl">Security Rating</span></div>
            <div class="deck-stat-card"><span class="deck-stat-val text-cyan" id="statConfiguredCount">—</span><span class="deck-stat-lbl">Configured Providers</span></div>
            <div class="deck-stat-card"><span class="deck-stat-val text-purple">Zero Leak</span><span class="deck-stat-lbl">Data Protection</span></div>
          </div>

          <div style="margin-top:16px;">
            <h4>Provider API Keys & Token Audit (Strictly Masked)</h4>
            <div class="deck-table-container">
              <table class="deck-table" id="secretsAuditTable">
                <thead>
                  <tr>
                    <th>Provider</th>
                    <th>Env Variable</th>
                    <th>Status</th>
                    <th>Masked Value</th>
                    <th>Action</th>
                  </tr>
                </thead>
                <tbody id="secretsAuditBody">
                  <tr><td colspan="5" class="deck-muted">Auditing credentials...</td></tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>

        <div class="deck-pane" style="flex: 1;">
          <div class="deck-pane-header">
            <h3>🛡️ Security Best Practices</h3>
          </div>
          <div id="secretsRecsList" style="display:flex;flex-direction:column;gap:10px;margin-top:10px;">
            <p class="deck-muted">Loading recommendations...</p>
          </div>
        </div>
      </div>
    `;

    document.getElementById('btnRefreshSecrets').addEventListener('click', loadAudit);
    await loadAudit();
  }

  async function loadAudit() {
    try {
      const [auditData, recsData] = await Promise.all([
        D().api('/api/secrets/audit'),
        D().api('/api/secrets/recommendations')
      ]);

      const a = auditData.audit || {};
      document.getElementById('statSecGrade').textContent = (a.hygiene_grade || 'GRADE_A').replace('GRADE_', 'Grade ');
      document.getElementById('statConfiguredCount').textContent = `${a.configured_count ?? 0} / ${a.total_providers ?? 0}`;

      const tbody = document.getElementById('secretsAuditBody');
      if (tbody && a.credentials) {
        tbody.innerHTML = a.credentials.map(c => `
          <tr>
            <td>
              <strong>${escapeHtml(c.name)}</strong>
              <div style="font-size:11px;color:#64748B;">${escapeHtml(c.role)}</div>
            </td>
            <td><code>${escapeHtml(c.env_variable)}</code></td>
            <td>
              <span class="deck-status-pill ${c.status === 'CONFIGURED' ? 'green' : (c.required ? 'rose' : 'gray')}">
                ${escapeHtml(c.status)}
              </span>
            </td>
            <td><code style="color:#38BDF8;">${escapeHtml(c.masked_value)}</code></td>
            <td>
              <button class="deck-btn-sm btn-verify-key" data-id="${c.provider_id}">Verify</button>
            </td>
          </tr>
        `).join('');

        tbody.querySelectorAll('.btn-verify-key').forEach(btn => {
          btn.addEventListener('click', () => verifyKey(btn.getAttribute('data-id'), btn));
        });
      }

      const recHost = document.getElementById('secretsRecsList');
      if (recHost && recsData.recommendations) {
        recHost.innerHTML = recsData.recommendations.map(r => `
          <div style="background:#0F172A;border:1px solid #1E293B;border-radius:6px;padding:10px;">
            <div style="color:#A7F3D0;font-size:12px;font-weight:bold;">${escapeHtml(r.title)}</div>
            <p style="font-size:11px;color:#94A3B8;margin:4px 0 0;">${escapeHtml(r.detail)}</p>
          </div>
        `).join('');
      }

    } catch (err) {
      console.error('Audit failed:', err);
    }
  }

  async function verifyKey(providerId, btn) {
    btn.disabled = true;
    btn.textContent = '...';
    try {
      const res = await D().api('/api/secrets/verify', {
        method: 'POST',
        body: { provider_id: providerId }
      });
      alert(res.message);
    } catch (err) {
      alert(`Verification failed: ${err.message}`);
    } finally {
      btn.disabled = false;
      btn.textContent = 'Verify';
    }
  }

  function escapeHtml(str) {
    return String(str || '').replace(/[&<>"']/g, c => ({
      '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
    }[c]));
  }

  CockpitDeck.registerView('secrets', {
    title: 'Environment & Secrets Guardian',
    navLabel: 'Secrets',
    icon: '🔑',
    order: 105,
    render: render
  });
})();
