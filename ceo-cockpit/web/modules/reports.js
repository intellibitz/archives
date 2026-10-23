/* =============================================================================
 * Round 18 — Board-Level Report Generator & Briefing Deck
 * Synthesizes budget financials, git releases, and swarm telemetry into briefs.
 * ========================================================================== */
(function () {
  'use strict';

  const D = () => window.CockpitDeck;

  async function render(view) {
    view.innerHTML = `
      <div class="deck-split-view">
        <div class="deck-pane" style="flex: 1;">
          <div class="deck-pane-header">
            <h3>📑 Executive Report Templates</h3>
          </div>
          <div id="reportTemplatesList" style="display:flex;flex-direction:column;gap:10px;">
            <p class="deck-muted">Loading templates...</p>
          </div>

          <div style="margin-top:20px;">
            <h4>Generated Reports Archive</h4>
            <div id="generatedReportsArchive" style="display:flex;flex-direction:column;gap:8px;max-height:240px;overflow-y:auto;padding-right:4px;">
              <p class="deck-muted">No reports generated yet.</p>
            </div>
          </div>
        </div>

        <div class="deck-pane" style="flex: 2;">
          <div class="deck-pane-header">
            <h3 id="activeReportHeading">📄 Report Preview & Export</h3>
            <div style="display:flex;gap:8px;">
              <button class="deck-btn-sm" id="btnCopyReport">📋 Copy Markdown</button>
              <button class="deck-btn-sm primary" id="btnDownloadReport">💾 Download</button>
            </div>
          </div>
          <div id="reportPreviewArea" style="background:#0F172A;border:1px solid #1E293B;border-radius:8px;padding:20px;height:480px;overflow-y:auto;color:#E2E8F0;font-family:inherit;line-height:1.6;font-size:13px;white-space:pre-wrap;">
            Select a template from the left to compile and preview a boardroom-ready executive brief.
          </div>
        </div>
      </div>
    `;

    document.getElementById('btnCopyReport').addEventListener('click', copyReport);
    document.getElementById('btnDownloadReport').addEventListener('click', downloadReport);

    await loadTemplates();
    await loadHistory();
  }

  let currentMarkdown = '';

  async function loadTemplates() {
    try {
      const data = await D().api('/api/reports/templates');
      const tpls = data.templates || [];
      const host = document.getElementById('reportTemplatesList');
      if (!host) return;

      host.innerHTML = tpls.map(t => `
        <div style="background:#1E293B;border:1px solid #334155;border-radius:8px;padding:12px;">
          <div style="display:flex;justify-content:space-between;align-items:center;">
            <strong style="color:#F8FAFC;font-size:13px;">${escapeHtml(t.name)}</strong>
            <span class="deck-badge">${escapeHtml(t.category)}</span>
          </div>
          <p style="font-size:11px;color:#94A3B8;margin:6px 0 10px;">${escapeHtml(t.description)}</p>
          <button class="deck-btn-sm primary btn-gen-report" data-id="${t.id}" style="width:100%;">⚡ Generate Brief</button>
        </div>
      `).join('');

      host.querySelectorAll('.btn-gen-report').forEach(btn => {
        btn.addEventListener('click', () => generateReport(btn.getAttribute('data-id')));
      });
    } catch (err) {
      console.error('Failed to load templates:', err);
    }
  }

  async function generateReport(templateId) {
    const preview = document.getElementById('reportPreviewArea');
    preview.textContent = 'Synthesizing live financials, git velocity, and swarm telemetry...';

    try {
      const res = await D().api('/api/reports/generate', {
        method: 'POST',
        body: { template_id: templateId }
      });
      const rep = res.report || {};
      currentMarkdown = rep.markdown || '';
      preview.textContent = currentMarkdown;
      document.getElementById('activeReportHeading').textContent = `📄 ${rep.title}`;
      await loadHistory();
    } catch (err) {
      preview.textContent = `Error generating report: ${err.message}`;
    }
  }

  async function loadHistory() {
    try {
      const data = await D().api('/api/reports/history');
      const reports = data.reports || [];
      const host = document.getElementById('generatedReportsArchive');
      if (!host) return;

      if (reports.length === 0) {
        host.innerHTML = '<p class="deck-muted" style="font-size:12px;">No past reports recorded.</p>';
        return;
      }

      host.innerHTML = reports.map(r => `
        <div class="report-hist-item" data-id="${r.id}" style="background:#0F172A;border:1px solid #1E293B;border-radius:6px;padding:8px;cursor:pointer;">
          <div style="color:#F1F5F9;font-size:12px;font-weight:bold;">${escapeHtml(r.title)}</div>
          <div style="font-size:10px;color:#64748B;margin-top:2px;">${new Date(r.created_at * 1000).toLocaleString()}</div>
        </div>
      `).join('');

      host.querySelectorAll('.report-hist-item').forEach(el => {
        el.addEventListener('click', () => {
          const rep = reports.find(x => x.id === el.getAttribute('data-id'));
          if (rep) {
            currentMarkdown = rep.markdown;
            document.getElementById('reportPreviewArea').textContent = currentMarkdown;
            document.getElementById('activeReportHeading').textContent = `📄 ${rep.title}`;
          }
        });
      });
    } catch (err) {
      console.error('Failed to load history:', err);
    }
  }

  function copyReport() {
    if (!currentMarkdown) return;
    navigator.clipboard.writeText(currentMarkdown).then(() => {
      alert('Report Markdown copied to clipboard!');
    }).catch(err => alert('Copy failed: ' + err));
  }

  function downloadReport() {
    if (!currentMarkdown) return;
    const blob = new Blob([currentMarkdown], { type: 'text/markdown;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `executive-report-${new Date().toISOString().slice(0, 10)}.md`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
  }

  function escapeHtml(str) {
    return String(str || '').replace(/[&<>"']/g, c => ({
      '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
    }[c]));
  }

  CockpitDeck.registerView('reports', {
    title: 'Board-Level Reports & RFC Generator',
    navLabel: 'Reports',
    icon: '📑',
    order: 100,
    render: render
  });
})();
