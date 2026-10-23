/* =============================================================================
 * Round 3 — Ops Console (Autonomous Tool Execution Engine)
 * Curated runbooks + guarded custom commands with live-streamed output.
 * ========================================================================== */
(function () {
  'use strict';
  const D = () => window.CockpitDeck;

  const st = {
    runbooks: [],
    workspace: '',
    current: null,       // job summary currently displayed
    lines: [],
    abort: null,
    running: false,
    assessTimer: null
  };
  let ui = {};

  function render(root, actions) {
    const { esc } = D();
    root.innerHTML = `
      <div class="ops-layout">
        <div class="ops-left">
          <div class="deck-card ops-custom">
            <div class="deck-card-title">⌨️ Direct Command <span class="deck-muted" id="opsCwdLabel"></span></div>
            <div class="ops-cmd-row">
              <span class="ops-prompt-glyph">PS&gt;</span>
              <input id="opsCmdInput" class="deck-input mono" placeholder="e.g. git status   ·   npm test   ·   python -m pytest -q" autocomplete="off" spellcheck="false" />
              <button id="opsCmdRun" class="deck-btn primary">▶ Run</button>
            </div>
            <div class="ops-guard" id="opsGuard"><span class="guard-dot ok"></span>Safety gate armed — destructive operations are refused automatically.</div>
          </div>
          <div class="deck-card ops-runbooks">
            <div class="deck-card-title">📚 Runbook Library</div>
            <div id="opsRunbookList" class="ops-runbook-list"><div class="deck-skeleton"></div></div>
          </div>
        </div>
        <div class="ops-right">
          <div class="deck-card ops-terminal-card">
            <div class="ops-term-head">
              <div class="ops-term-meta">
                <span class="status-pill idle" id="opsStatusPill">IDLE</span>
                <span class="ops-term-title" id="opsTermTitle">No run selected</span>
              </div>
              <div class="ops-term-stats">
                <span id="opsTermDuration">—</span>
                <span id="opsTermLines">0 lines</span>
              </div>
            </div>
            <pre class="ops-terminal" id="opsTerminal"><span class="term-hint">Select a runbook or type a command to begin.\nOutput streams here in real time.</span></pre>
            <div class="ops-term-actions">
              <button class="deck-btn ghost" id="opsCopy">📋 Copy Output</button>
              <button class="deck-btn ghost" id="opsSendAI">🧠 Ask AI to Analyze</button>
              <button class="deck-btn danger" id="opsCancel" disabled>⏹ Cancel</button>
            </div>
          </div>
          <div class="deck-card ops-history-card">
            <div class="deck-card-title">🕘 Run History <button class="deck-link" id="opsRefreshHistory">refresh</button></div>
            <div id="opsHistory" class="ops-history"></div>
          </div>
        </div>
      </div>`;
    actions.innerHTML = `<span class="deck-chip">🛡️ Guarded Execution</span>`;

    ui = {
      input: root.querySelector('#opsCmdInput'),
      run: root.querySelector('#opsCmdRun'),
      guard: root.querySelector('#opsGuard'),
      list: root.querySelector('#opsRunbookList'),
      term: root.querySelector('#opsTerminal'),
      pill: root.querySelector('#opsStatusPill'),
      title: root.querySelector('#opsTermTitle'),
      dur: root.querySelector('#opsTermDuration'),
      lineCount: root.querySelector('#opsTermLines'),
      cancel: root.querySelector('#opsCancel'),
      copy: root.querySelector('#opsCopy'),
      sendAI: root.querySelector('#opsSendAI'),
      history: root.querySelector('#opsHistory'),
      cwd: root.querySelector('#opsCwdLabel')
    };

    ui.run.addEventListener('click', () => runCustom());
    ui.input.addEventListener('keydown', (e) => { if (e.key === 'Enter') runCustom(); });
    ui.input.addEventListener('input', () => {
      clearTimeout(st.assessTimer);
      st.assessTimer = setTimeout(assess, 250);
    });
    ui.cancel.addEventListener('click', cancel);
    ui.copy.addEventListener('click', async () => {
      try { await navigator.clipboard.writeText(st.lines.join('\n')); D().toast('Output copied', 'success'); }
      catch (_) { D().toast('Clipboard unavailable', 'error'); }
    });
    ui.sendAI.addEventListener('click', sendToAI);
    root.querySelector('#opsRefreshHistory').addEventListener('click', loadHistory);

    loadRunbooks();
    loadHistory();
  }

  async function loadRunbooks() {
    const { api, esc } = D();
    try {
      const data = await api('/api/ops/runbooks');
      st.runbooks = data.runbooks || [];
      st.workspace = data.workspace || '';
      ui.cwd.textContent = st.workspace ? `· ${st.workspace}` : '';
      const groups = {};
      st.runbooks.forEach((r) => { (groups[r.group] = groups[r.group] || []).push(r); });
      ui.list.innerHTML = Object.entries(groups).map(([g, items]) => `
        <div class="ops-group">
          <div class="ops-group-title">${esc(g)}</div>
          ${items.map((r) => `
            <button class="ops-runbook" data-id="${esc(r.id)}" id="opsRb_${esc(r.id)}" title="${esc(r.command)}">
              <span class="ops-rb-icon">${r.icon}</span>
              <span class="ops-rb-text"><span class="ops-rb-title">${esc(r.title)}</span><span class="ops-rb-desc">${esc(r.desc)}</span></span>
              <span class="ops-rb-go">▶</span>
            </button>`).join('')}
        </div>`).join('');
      ui.list.querySelectorAll('.ops-runbook').forEach((b) => b.addEventListener('click', () => runRunbook(b.dataset.id)));
    } catch (e) {
      ui.list.innerHTML = `<div class="deck-empty">Runbooks unavailable: ${esc(e.message)}</div>`;
    }
  }

  async function assess() {
    const cmd = ui.input.value.trim();
    if (!cmd) { setGuard(true); return; }
    try {
      const r = await D().api('/api/ops/assess', { body: { command: cmd } });
      setGuard(r.allowed, r.reason);
    } catch (_) { /* ignore transient */ }
  }

  function setGuard(ok, reason) {
    ui.guard.className = `ops-guard ${ok ? '' : 'blocked'}`;
    ui.guard.innerHTML = ok
      ? '<span class="guard-dot ok"></span>Safety gate armed — destructive operations are refused automatically.'
      : `<span class="guard-dot bad"></span>Blocked by safety gate: <strong>${D().esc(reason)}</strong>`;
    ui.run.disabled = !ok;
  }

  function runRunbook(id) { execute({ runbook_id: id }); }

  function runCustom() {
    const cmd = ui.input.value.trim();
    if (!cmd) { ui.input.focus(); return; }
    execute({ command: cmd });
  }

  function setStatus(status) {
    const map = { running: 'RUNNING', succeeded: 'SUCCESS', failed: 'FAILED', timeout: 'TIMEOUT', cancelled: 'CANCELLED', idle: 'IDLE', blocked: 'BLOCKED' };
    ui.pill.className = `status-pill ${status}`;
    ui.pill.textContent = map[status] || status.toUpperCase();
  }

  function appendLines(lines) {
    const { esc } = D();
    if (st.lines.length === 0) ui.term.innerHTML = '';
    st.lines.push(...lines);
    const frag = lines.map((l) => {
      let cls = '';
      if (/error|fail|exception|traceback|fatal/i.test(l)) cls = 'term-err';
      else if (/warn/i.test(l)) cls = 'term-warn';
      else if (/\b(ok|success|healthy|passed|clean)\b/i.test(l)) cls = 'term-ok';
      return `<span class="${cls}">${esc(l)}</span>\n`;
    }).join('');
    const nearBottom = ui.term.scrollHeight - ui.term.scrollTop - ui.term.clientHeight < 60;
    ui.term.insertAdjacentHTML('beforeend', frag);
    if (nearBottom) ui.term.scrollTop = ui.term.scrollHeight;
    ui.lineCount.textContent = `${st.lines.length} lines`;
  }

  async function execute(body) {
    const { streamSSE, toast, fmt } = D();
    if (st.abort) st.abort.abort();   // detach previous view; job continues server-side
    st.abort = new AbortController();
    st.lines = [];
    st.running = true;
    ui.term.innerHTML = '<span class="term-hint">Launching…</span>';
    ui.title.textContent = body.command || (st.runbooks.find((r) => r.id === body.runbook_id) || {}).title || 'Run';
    ui.dur.textContent = '0ms';
    ui.lineCount.textContent = '0 lines';
    ui.cancel.disabled = false;
    setStatus('running');
    D().setBadge('ops', '●', 'amber');

    try {
      await streamSSE('/api/ops/run', body, (ev) => {
        if (ev.type === 'start') { st.current = ev.job; ui.title.textContent = ev.job.title; }
        else if (ev.type === 'lines') appendLines(ev.lines);
        else if (ev.type === 'heartbeat') ui.dur.textContent = fmt.duration(ev.elapsed_ms);
        else if (ev.type === 'exit') {
          st.current = ev.job;
          ui.dur.textContent = fmt.duration(ev.job.duration_ms);
          setStatus(ev.job.status);
          if (st.lines.length === 0) ui.term.innerHTML = '<span class="term-hint">(no output)</span>';
          toast(`${ev.job.title}: ${ev.job.status} (exit ${ev.job.exit_code})`, ev.job.status === 'succeeded' ? 'success' : 'error');
        }
      }, st.abort.signal);
    } catch (e) {
      if (e.name === 'AbortError') return;
      if (e.status === 403) {
        setStatus('blocked');
        ui.term.innerHTML = `<span class="term-err">${D().esc(e.message)}</span>`;
        toast(e.message, 'warning', 5000);
      } else {
        setStatus('failed');
        appendLines([`[cockpit] ${e.message}`]);
      }
    } finally {
      st.running = false;
      ui.cancel.disabled = true;
      D().setBadge('ops', null);
      loadHistory();
    }
  }

  async function cancel() {
    if (!st.current) return;
    try { await D().api('/api/ops/cancel', { body: { job_id: st.current.id } }); }
    catch (e) { D().toast(e.message, 'error'); }
  }

  async function loadHistory() {
    const { api, esc, fmt } = D();
    try {
      const data = await api('/api/ops/history?limit=25');
      const rows = data.history || [];
      if (!rows.length) { ui.history.innerHTML = '<div class="deck-empty small">No runs yet.</div>'; return; }
      ui.history.innerHTML = rows.map((r) => `
        <button class="ops-hist-row" data-id="${esc(r.id)}">
          <span class="status-dot ${esc(r.status)}"></span>
          <span class="ops-hist-title">${esc(r.title)}</span>
          <span class="ops-hist-meta">${fmt.duration(r.duration_ms)} · ${fmt.ago(r.finished || r.started)}</span>
        </button>`).join('');
      ui.history.querySelectorAll('.ops-hist-row').forEach((b) => b.addEventListener('click', () => loadJob(b.dataset.id)));
    } catch (e) {
      ui.history.innerHTML = `<div class="deck-empty small">${esc(e.message)}</div>`;
    }
  }

  async function loadJob(id) {
    const { api, fmt } = D();
    try {
      const data = await api(`/api/ops/jobs/${encodeURIComponent(id)}`);
      if (st.abort) st.abort.abort();
      st.current = data.job;
      st.lines = [];
      ui.term.innerHTML = '';
      ui.title.textContent = data.job.title;
      ui.dur.textContent = fmt.duration(data.job.duration_ms);
      setStatus(data.job.status);
      appendLines(data.output || []);
      if (!(data.output || []).length) ui.term.innerHTML = '<span class="term-hint">(no output captured)</span>';
    } catch (e) { D().toast(e.message, 'error'); }
  }

  function sendToAI() {
    if (!st.lines.length || !window.Cockpit) { D().toast('Nothing to analyze yet', 'warning'); return; }
    const tail = st.lines.slice(-150).join('\n');
    const title = (st.current && st.current.title) || 'command';
    const status = (st.current && st.current.status) || 'unknown';
    window.Cockpit.setPrompt(
      `Analyze the output of "${title}" (status: ${status}). Give me an executive verdict, root cause of any failures, and the exact next actions.\n\n<ops_output>\n${tail}\n</ops_output>`
    );
    D().close();
    D().toast('Output loaded into the directive composer', 'info');
  }

  document.addEventListener('cockpit:ready', () => {
    D().registerPanel({
      id: 'ops', order: 10, icon: '🛠️', title: 'Ops Console', navLabel: 'Ops',
      subtitle: 'Run runbooks & guarded commands in the active workspace with live output',
      render,
      onShow: () => { if (ui.input && !st.running) ui.input.focus(); },
      paletteActions: [
        { title: 'Ops: Run Infrastructure Health Check', desc: 'Stream the full health check in the Ops Console', icon: '🩺', action: () => { D().open('ops'); setTimeout(() => runRunbook('health'), 50); } },
        { title: 'Ops: Repository Status', desc: 'git status in the active workspace', icon: '🌿', action: () => { D().open('ops'); setTimeout(() => runRunbook('git-status'), 50); } }
      ]
    });
  });
})();
