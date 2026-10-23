/* =============================================================================
 * Round 12 — Autonomous Workflow Orchestration & Scheduled Jobs
 * Executive task automation, background maintenance, and execution telemetry.
 * ========================================================================== */
(function () {
  'use strict';

  const D = () => window.CockpitDeck;

  async function render(view) {
    view.innerHTML = `
      <div class="deck-split-view">
        <div class="deck-pane" style="flex: 2;">
          <div class="deck-pane-header">
            <h3>⏱️ Active Workflow Automation Schedules</h3>
            <div style="display:flex;gap:8px;">
              <button class="deck-btn-sm" id="btnRefreshScheduler">🔄 Refresh</button>
              <button class="deck-btn-sm primary" id="btnNewJobModal">＋ Add Schedule</button>
            </div>
          </div>
          <div id="schedulerStats" class="deck-stats-grid">
            <div class="deck-stat-card"><span class="deck-stat-val text-cyan" id="statActiveJobs">—</span><span class="deck-stat-lbl">Active Schedules</span></div>
            <div class="deck-stat-card"><span class="deck-stat-val text-green" id="statTotalJobs">—</span><span class="deck-stat-lbl">Total Tasks</span></div>
            <div class="deck-stat-card"><span class="deck-stat-val text-purple">Autonomous</span><span class="deck-stat-lbl">Execution Mode</span></div>
          </div>
          <div class="deck-table-container" style="margin-top:16px;">
            <table class="deck-table" id="schedulerJobsTable">
              <thead>
                <tr>
                  <th>Job Name</th>
                  <th>Frequency</th>
                  <th>Status</th>
                  <th>Last Execution</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody id="schedulerJobsBody">
                <tr><td colspan="5" class="deck-muted" style="text-align:center;">Loading scheduled workflows...</td></tr>
              </tbody>
            </table>
          </div>
        </div>
        <div class="deck-pane" style="flex: 1;">
          <div class="deck-pane-header">
            <h3>⚡ Autonomous Job Execution Log</h3>
          </div>
          <div id="schedulerRunLog" style="background:#0F172A;border:1px solid #1E293B;border-radius:8px;padding:12px;font-family:monospace;font-size:12px;color:#94A3B8;height:420px;overflow-y:auto;">
            <p>Ready for job triggers. Select 'Run Now' on any workflow to execute immediately.</p>
          </div>
        </div>
      </div>

      <!-- Add Job Modal -->
      <div id="addJobModalBackdrop" style="display:none;position:fixed;top:0;left:0;right:0;bottom:0;background:rgba(0,0,0,0.7);z-index:9999;align-items:center;justify-content:center;">
        <div style="background:#1E293B;border:1px solid #334155;border-radius:12px;width:440px;padding:24px;box-shadow:0 20px 40px rgba(0,0,0,0.5);">
          <h3 style="color:#F8FAFC;margin-top:0;">Add Scheduled Workflow</h3>
          <label style="color:#94A3B8;font-size:12px;display:block;margin-top:12px;">Workflow Name:</label>
          <input type="text" id="newJobName" class="ws-path-input" placeholder="e.g. Database Backup & Index Vacuum" style="width:100%;margin-top:4px;" />
          <label style="color:#94A3B8;font-size:12px;display:block;margin-top:12px;">Cron Expression:</label>
          <input type="text" id="newJobCron" class="ws-path-input" value="0 * * * *" style="width:100%;margin-top:4px;" />
          <label style="color:#94A3B8;font-size:12px;display:block;margin-top:12px;">Frequency Description:</label>
          <input type="text" id="newJobFreq" class="ws-path-input" value="Hourly" style="width:100%;margin-top:4px;" />
          <label style="color:#94A3B8;font-size:12px;display:block;margin-top:12px;">Description:</label>
          <input type="text" id="newJobDesc" class="ws-path-input" placeholder="Autonomous verification action..." style="width:100%;margin-top:4px;" />
          <div style="display:flex;justify-content:flex-end;gap:8px;margin-top:20px;">
            <button id="btnCancelAddJob" class="deck-btn-sm">Cancel</button>
            <button id="btnConfirmAddJob" class="ws-apply-btn">Create Workflow</button>
          </div>
        </div>
      </div>
    `;

    document.getElementById('btnRefreshScheduler').addEventListener('click', loadSchedulerData);
    document.getElementById('btnNewJobModal').addEventListener('click', () => {
      document.getElementById('addJobModalBackdrop').style.display = 'flex';
    });
    document.getElementById('btnCancelAddJob').addEventListener('click', () => {
      document.getElementById('addJobModalBackdrop').style.display = 'none';
    });
    document.getElementById('btnConfirmAddJob').addEventListener('click', createJob);

    await loadSchedulerData();
  }

  async function loadSchedulerData() {
    try {
      const data = await D().api('/api/scheduler/jobs');
      const jobs = data.jobs || [];

      document.getElementById('statActiveJobs').textContent = data.enabled_count ?? 0;
      document.getElementById('statTotalJobs').textContent = data.total_count ?? 0;

      const tbody = document.getElementById('schedulerJobsBody');
      if (!tbody) return;

      if (jobs.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" class="deck-muted" style="text-align:center;">No scheduled jobs configured.</td></tr>';
        return;
      }

      tbody.innerHTML = jobs.map(j => `
        <tr>
          <td>
            <strong>${escapeHtml(j.name)}</strong>
            <div style="font-size:11px;color:#64748B;">${escapeHtml(j.description)}</div>
          </td>
          <td>
            <span class="deck-badge">${escapeHtml(j.interval_human || j.cron)}</span>
          </td>
          <td>
            <span class="deck-status-pill ${j.enabled ? 'green' : 'gray'}">${j.enabled ? 'Active' : 'Paused'}</span>
          </td>
          <td>
            <div style="font-size:12px;">${j.last_run ? new Date(j.last_run * 1000).toLocaleTimeString() : 'Pending'}</div>
            <div style="font-size:10px;color:#64748B;">${j.last_status || 'NOMINAL'} (${j.last_duration_ms || 0}ms)</div>
          </td>
          <td>
            <div style="display:flex;gap:6px;">
              <button class="deck-btn-sm primary btn-run-job" data-id="${j.id}">⚡ Run</button>
              <button class="deck-btn-sm btn-toggle-job" data-id="${j.id}">${j.enabled ? 'Pause' : 'Resume'}</button>
            </div>
          </td>
        </tr>
      `).join('');

      tbody.querySelectorAll('.btn-run-job').forEach(btn => {
        btn.addEventListener('click', () => runJob(btn.getAttribute('data-id')));
      });

      tbody.querySelectorAll('.btn-toggle-job').forEach(btn => {
        btn.addEventListener('click', () => toggleJob(btn.getAttribute('data-id')));
      });

    } catch (err) {
      console.error('Failed to load scheduler:', err);
    }
  }

  async function runJob(jobId) {
    appendLog(`[${new Date().toLocaleTimeString()}] Triggering manual execution for job: ${jobId}...`);
    try {
      const res = await D().api('/api/scheduler/run', { method: 'POST', body: { id: jobId } });
      appendLog(`[${new Date().toLocaleTimeString()}] ✅ ${res.output}`);
      await loadSchedulerData();
    } catch (err) {
      appendLog(`[${new Date().toLocaleTimeString()}] ❌ Failed to run job ${jobId}: ${err.message}`);
    }
  }

  async function toggleJob(jobId) {
    try {
      await D().api('/api/scheduler/toggle', { method: 'POST', body: { id: jobId } });
      await loadSchedulerData();
    } catch (err) {
      alert(`Toggle failed: ${err.message}`);
    }
  }

  async function createJob() {
    const name = document.getElementById('newJobName').value.trim();
    const cron = document.getElementById('newJobCron').value.trim();
    const freq = document.getElementById('newJobFreq').value.trim();
    const desc = document.getElementById('newJobDesc').value.trim();

    if (!name) {
      alert('Job name is required');
      return;
    }

    try {
      await D().api('/api/scheduler/create', {
        method: 'POST',
        body: { name, cron, interval_human: freq, description: desc }
      });
      document.getElementById('addJobModalBackdrop').style.display = 'none';
      await loadSchedulerData();
      appendLog(`[${new Date().toLocaleTimeString()}] 🌟 Created new workflow schedule: ${name}`);
    } catch (err) {
      alert(`Create failed: ${err.message}`);
    }
  }

  function appendLog(line) {
    const logBox = document.getElementById('schedulerRunLog');
    if (logBox) {
      const p = document.createElement('p');
      p.style.margin = '4px 0';
      p.textContent = line;
      logBox.appendChild(p);
      logBox.scrollTop = logBox.scrollHeight;
    }
  }

  function escapeHtml(str) {
    return String(str || '').replace(/[&<>"']/g, c => ({
      '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
    }[c]));
  }

  CockpitDeck.registerView('scheduler', {
    title: 'Autonomous Workflow Scheduler',
    navLabel: 'Scheduler',
    icon: '⏱️',
    order: 70,
    shortcut: 'Alt+8',
    render: render
  });
})();
