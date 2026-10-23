/* =============================================================================
 * Round 14 — Automated Architecture & Code Compliance Reviewer
 * Static AST validation, secret leak detection, and compliance scoring.
 * ========================================================================== */
(function () {
  'use strict';

  const D = () => window.CockpitDeck;

  async function render(view) {
    view.innerHTML = `
      <div class="deck-split-view">
        <div class="deck-pane" style="flex: 2;">
          <div class="deck-pane-header">
            <h3>🛡️ Architecture & Security Compliance Audit</h3>
            <button class="deck-btn-sm" id="btnRunComplianceScan">🔄 Run Audit</button>
          </div>
          <div id="complianceStatsGrid" class="deck-stats-grid">
            <div class="deck-stat-card"><span class="deck-stat-val text-green" id="statCompScore">—</span><span class="deck-stat-lbl">Hygiene Score</span></div>
            <div class="deck-stat-card"><span class="deck-stat-val text-cyan" id="statCompGrade">—</span><span class="deck-stat-lbl">Security Grade</span></div>
            <div class="deck-stat-card"><span class="deck-stat-val text-purple" id="statScannedFiles">—</span><span class="deck-stat-lbl">Scanned Files</span></div>
            <div class="deck-stat-card"><span class="deck-stat-val text-rose" id="statViolations">—</span><span class="deck-stat-lbl">Violations</span></div>
          </div>

          <div style="margin-top:16px;">
            <h4>Compliance Findings & Security Audit</h4>
            <div id="complianceFindingsList" class="deck-table-container" style="max-height:400px;overflow-y:auto;">
              <p class="deck-muted" style="text-align:center;padding:20px;">Scanning workspace...</p>
            </div>
          </div>
        </div>

        <div class="deck-pane" style="flex: 1;">
          <div class="deck-pane-header">
            <h3>🔒 Live Secret Redaction Sanitizer</h3>
          </div>
          <p style="font-size:12px;color:#94A3B8;margin-top:0;">Test instant regex sanitization on sensitive tokens before forwarding to public AI model gateways.</p>
          <div class="deck-form-group">
            <label style="font-size:12px;color:#94A3B8;">Input Raw Prompt / Trace:</label>
            <textarea id="rawRedactInput" class="ws-path-input" rows="5" style="width:100%;resize:vertical;" placeholder="Paste sample trace with keys like sk-proj-..."></textarea>
            <button id="btnRedactText" class="ws-apply-btn" style="width:100%;margin-top:8px;">Sanitize & Redact Secrets</button>
          </div>
          <div id="redactOutputCard" style="margin-top:16px;display:none;background:#0F172A;border:1px solid #1E293B;border-radius:8px;padding:12px;font-family:monospace;font-size:11px;color:#38BDF8;word-break:break-all;">
          </div>
        </div>
      </div>
    `;

    document.getElementById('btnRunComplianceScan').addEventListener('click', loadComplianceScan);
    document.getElementById('btnRedactText').addEventListener('click', testRedaction);

    await loadComplianceScan();
  }

  async function loadComplianceScan() {
    try {
      const data = await D().api('/api/compliance/scan');
      const c = data.compliance || {};

      const scoreEl = document.getElementById('statCompScore');
      const gradeEl = document.getElementById('statCompGrade');
      const filesEl = document.getElementById('statScannedFiles');
      const violEl = document.getElementById('statViolations');

      if (scoreEl) {
        scoreEl.textContent = `${c.score ?? 100}/100`;
        scoreEl.className = c.score >= 90 ? 'deck-stat-val text-green' : (c.score >= 70 ? 'deck-stat-val text-amber' : 'deck-stat-val text-rose');
      }
      if (gradeEl) {
        gradeEl.textContent = (c.grade || 'GRADE_A').replace('GRADE_', 'Grade ');
      }
      if (filesEl) {
        filesEl.textContent = c.scanned_files ?? 0;
      }
      if (violEl) {
        const count = (c.findings || []).length;
        violEl.textContent = count;
        violEl.className = count === 0 ? 'deck-stat-val text-green' : 'deck-stat-val text-rose';
      }

      const host = document.getElementById('complianceFindingsList');
      if (!host) return;

      const findings = c.findings || [];
      if (findings.length === 0) {
        host.innerHTML = `
          <div style="background:#064E3B;border:1px solid #059669;border-radius:8px;padding:16px;text-align:center;color:#A7F3D0;">
            <strong>✅ 100% Clean Workspace Compliance</strong>
            <p style="margin:4px 0 0;font-size:12px;">Zero plain-text secret leaks, zero syntax errors, and zero binary bloat discovered.</p>
          </div>
        `;
        return;
      }

      host.innerHTML = findings.map(f => {
        const badgeColor = f.severity === 'CRITICAL' ? 'rose' : (f.severity === 'ERROR' ? 'amber' : 'gray');
        return `
          <div style="background:#1E293B;border:1px solid #334155;border-radius:6px;padding:10px;margin-bottom:8px;">
            <div style="display:flex;justify-content:space-between;align-items:center;">
              <span class="deck-badge" style="background:#0F172A;color:#38BDF8;font-family:monospace;">${escapeHtml(f.file)}</span>
              <span class="deck-status-pill ${badgeColor}">${escapeHtml(f.severity)}</span>
            </div>
            <p style="margin:6px 0 0;color:#F1F5F9;font-size:12px;">${escapeHtml(f.message)}</p>
          </div>
        `;
      }).join('');

    } catch (err) {
      console.error('Compliance scan failed:', err);
    }
  }

  async function testRedaction() {
    const text = document.getElementById('rawRedactInput').value;
    if (!text) return;

    try {
      const data = await D().api('/api/compliance/redact', {
        method: 'POST',
        body: { text }
      });
      const out = document.getElementById('redactOutputCard');
      out.style.display = 'block';
      out.innerHTML = `
        <div style="color:#A7F3D0;font-weight:bold;margin-bottom:6px;">Protected Output (${data.matches_found} tokens sanitized):</div>
        <div>${escapeHtml(data.redacted_text)}</div>
      `;
    } catch (err) {
      alert(`Redaction error: ${err.message}`);
    }
  }

  function escapeHtml(str) {
    return String(str || '').replace(/[&<>"']/g, c => ({
      '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
    }[c]));
  }

  CockpitDeck.registerView('compliance', {
    title: 'Architecture & Security Compliance',
    navLabel: 'Compliance',
    icon: '🛡️',
    order: 80,
    render: render
  });
})();
