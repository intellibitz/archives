// Round 8 — Git Hygiene & Release Mission Control (Frontend View)
(function() {
  'use strict';

  function initGitView(container) {
    container.innerHTML = `
      <div class="deck-toolbar">
        <div class="deck-toolbar-left">
          <span class="deck-panel-title">🌿 Git Hygiene & Release Mission Control</span>
          <span class="badge badge-accent" id="gitBranchBadge">main</span>
          <span class="badge" id="gitCleanBadge">Checking state...</span>
        </div>
        <div class="deck-toolbar-right">
          <button class="deck-btn" id="btnRefreshGit">🔄 Refresh</button>
          <button class="deck-btn btn-primary" id="btnStageAll">➕ Stage All Changes</button>
        </div>
      </div>

      <div class="deck-content-split">
        <!-- Left: Change Lists & Commit Assistant -->
        <div class="deck-pane deck-pane-left">
          <div class="deck-section-label">Uncommitted Changes & Footprint</div>
          <div class="git-files-card">
            <div class="git-section-subhead">
              <span>Staged for Commit</span>
              <span class="badge badge-subtle" id="stagedCountBadge">0</span>
            </div>
            <div class="git-files-list" id="stagedList">
              <div class="deck-empty">No files staged.</div>
            </div>

            <div class="git-section-subhead" style="margin-top: 10px;">
              <span>Unstaged & Untracked</span>
              <span class="badge badge-subtle" id="unstagedCountBadge">0</span>
            </div>
            <div class="git-files-list" id="unstagedList">
              <div class="deck-empty">Working tree clean.</div>
            </div>
          </div>

          <div class="deck-section-label" style="margin-top: 14px;">Atomic Release & Commit Assistant</div>
          <div class="git-commit-box">
            <div class="git-commit-tools">
              <button class="deck-btn" id="btnAiSuggestCommit">✨ AI Generate Commit Message</button>
            </div>
            <textarea id="commitMessageInput" class="deck-textarea" rows="2"
              placeholder="feat(scope): concise summary of verified functional changes..."></textarea>
            <div class="git-commit-actions">
              <button class="btn btn-primary" id="btnCommitChanges">
                📦 Create Atomic Verified Commit
              </button>
            </div>
          </div>
        </div>

        <!-- Right: Live Diff Inspector & Commit Timeline -->
        <div class="deck-pane deck-pane-right">
          <div class="deck-section-label" id="diffPaneLabel">Working Tree Diff Inspector</div>
          <div class="git-diff-card">
            <div class="git-diff-toolbar">
              <span id="diffTargetMeta" class="git-diff-meta">all changes</span>
              <div class="git-diff-toggle">
                <button class="deck-btn active" id="btnDiffUnstaged">Unstaged Diff</button>
                <button class="deck-btn" id="btnDiffStaged">Staged Diff</button>
              </div>
            </div>
            <pre class="git-diff-viewer" id="diffViewer">Select a file on the left or click Unstaged/Staged Diff to inspect changes.</pre>
          </div>

          <div class="deck-section-label" style="margin-top: 14px;">Recent Verified Commits</div>
          <div class="git-timeline" id="recentCommitsList">
            <div class="deck-empty">Loading history...</div>
          </div>
        </div>
      </div>
    `;

    // Elements
    const branchBadge = container.querySelector('#gitBranchBadge');
    const cleanBadge = container.querySelector('#gitCleanBadge');
    const stagedList = container.querySelector('#stagedList');
    const unstagedList = container.querySelector('#unstagedList');
    const stagedCountBadge = container.querySelector('#stagedCountBadge');
    const unstagedCountBadge = container.querySelector('#unstagedCountBadge');
    const commitInput = container.querySelector('#commitMessageInput');
    const btnAiSuggest = container.querySelector('#btnAiSuggestCommit');
    const btnCommit = container.querySelector('#btnCommitChanges');
    const btnStageAll = container.querySelector('#btnStageAll');
    const btnRefresh = container.querySelector('#btnRefreshGit');
    const diffViewer = container.querySelector('#diffViewer');
    const diffMeta = container.querySelector('#diffTargetMeta');
    const btnDiffUnstaged = container.querySelector('#btnDiffUnstaged');
    const btnDiffStaged = container.querySelector('#btnDiffStaged');
    const recentCommitsList = container.querySelector('#recentCommitsList');

    let currentGitState = null;
    let showingStagedDiff = false;

    async function loadGitStatus() {
      try {
        const res = await fetch('/api/git/status');
        const data = await res.json();
        currentGitState = data;
        renderGitState(data);
      } catch (err) {
        console.error('Git status error:', err);
      }
    }

    function renderGitState(data) {
      branchBadge.textContent = `🌿 ${data.branch} ${data.ahead ? `↑${data.ahead}` : ''} ${data.behind ? `↓${data.behind}` : ''}`;
      cleanBadge.textContent = data.clean ? '✓ Tree Clean' : `● Dirty (${data.staged_count + data.unstaged_count + data.untracked_count})`;
      cleanBadge.className = data.clean ? 'badge badge-success' : 'badge badge-accent';

      stagedCountBadge.textContent = data.staged_count;
      unstagedCountBadge.textContent = data.unstaged_count + data.untracked_count;

      // Render staged list
      if (!data.staged.length) {
        stagedList.innerHTML = '<div class="deck-empty">No files staged.</div>';
      } else {
        stagedList.innerHTML = data.staged.map(f => `
          <div class="git-file-row" data-path="${f.path}">
            <span class="badge badge-accent">${f.status}</span>
            <span class="git-file-path" title="${f.path}">${f.path}</span>
            <button class="deck-btn-xs btn-inspect-diff" data-path="${f.path}" data-staged="true">Diff</button>
          </div>
        `).join('');
      }

      // Render unstaged list
      const combinedUnstaged = [
        ...data.unstaged.map(f => ({ ...f, type: 'modified' })),
        ...data.untracked.map(p => ({ path: p, status: '?', type: 'untracked' }))
      ];

      if (!combinedUnstaged.length) {
        unstagedList.innerHTML = '<div class="deck-empty">Working tree clean.</div>';
      } else {
        unstagedList.innerHTML = combinedUnstaged.map(f => `
          <div class="git-file-row" data-path="${f.path}">
            <span class="badge ${f.status === '?' ? 'badge-subtle' : 'badge-error'}">${f.status}</span>
            <span class="git-file-path" title="${f.path}">${f.path}</span>
            <button class="deck-btn-xs btn-inspect-diff" data-path="${f.path}" data-staged="false">Diff</button>
          </div>
        `).join('');
      }

      // Render Recent Commits
      const commits = data.recent_commits || [];
      recentCommitsList.innerHTML = commits.map(c => `
        <div class="git-commit-row">
          <span class="git-commit-bullet">●</span>
          <span class="git-commit-text font-mono">${c}</span>
        </div>
      `).join('');

      // Wire Diff Click Handlers
      container.querySelectorAll('.btn-inspect-diff').forEach(btn => {
        btn.addEventListener('click', () => {
          loadDiff(btn.dataset.path, btn.dataset.staged === 'true');
        });
      });
    }

    async function loadDiff(file = '', staged = false) {
      showingStagedDiff = staged;
      btnDiffStaged.classList.toggle('active', staged);
      btnDiffUnstaged.classList.toggle('active', !staged);
      diffMeta.textContent = `${file || 'all files'} (${staged ? 'staged' : 'unstaged'})`;

      try {
        const url = `/api/git/diff?staged=${staged}${file ? `&file=${encodeURIComponent(file)}` : ''}`;
        const res = await fetch(url);
        const data = await res.json();
        renderDiffText(data.diff || 'No diff detected.');
      } catch (err) {
        diffViewer.textContent = `Error loading diff: ${err.message}`;
      }
    }

    function renderDiffText(rawDiff) {
      if (!rawDiff || !rawDiff.trim()) {
        diffViewer.innerHTML = '<span class="text-muted">No changes detected.</span>';
        return;
      }
      const lines = rawDiff.split('\n');
      diffViewer.innerHTML = lines.map(line => {
        const esc = line.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
        if (line.startsWith('+') && !line.startsWith('+++')) return `<span class="diff-add">${esc}</span>`;
        if (line.startsWith('-') && !line.startsWith('---')) return `<span class="diff-del">${esc}</span>`;
        if (line.startsWith('@@')) return `<span class="diff-hunk">${esc}</span>`;
        return esc;
      }).join('\n');
    }

    btnDiffUnstaged.addEventListener('click', () => loadDiff('', false));
    btnDiffStaged.addEventListener('click', () => loadDiff('', true));

    // Stage All
    btnStageAll.addEventListener('click', async () => {
      try {
        const res = await fetch('/api/git/stage', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ paths: ['*'] })
        });
        const data = await res.json();
        if (data.success) {
          renderGitState(data.state);
          if (window.Cockpit) window.Cockpit.notify('All changes staged successfully.', 'success');
        }
      } catch (err) {
        alert('Stage failed: ' + err.message);
      }
    });

    // AI Suggest Commit Message
    btnAiSuggest.addEventListener('click', async () => {
      btnAiSuggest.disabled = true;
      btnAiSuggest.textContent = '⚡ Synthesizing Message...';
      try {
        const res = await fetch('/api/git/suggest-commit', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: '{}' });
        const data = await res.json();
        if (data.message) {
          commitInput.value = data.message;
          commitInput.focus();
        }
      } catch (err) {
        console.error('Suggest error:', err);
      } finally {
        btnAiSuggest.disabled = false;
        btnAiSuggest.textContent = '✨ AI Generate Commit Message';
      }
    });

    // Commit Changes
    btnCommit.addEventListener('click', async () => {
      const msg = commitInput.value.trim();
      if (!msg) {
        commitInput.focus();
        return;
      }

      btnCommit.disabled = true;
      btnCommit.textContent = '📦 Committing...';
      try {
        const res = await fetch('/api/git/commit', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ message: msg })
        });
        const data = await res.json();
        if (!data.success) throw new Error(data.error || 'Commit failed');

        commitInput.value = '';
        renderGitState(data.state);
        loadDiff('', false);
        if (window.Cockpit) window.Cockpit.notify('Atomic commit created successfully.', 'success');
      } catch (err) {
        alert('Commit failed: ' + err.message);
      } finally {
        btnCommit.disabled = false;
        btnCommit.textContent = '📦 Create Atomic Verified Commit';
      }
    });

    btnRefresh.addEventListener('click', () => {
      loadGitStatus();
      loadDiff('', showingStagedDiff);
    });

    loadGitStatus();
    loadDiff('', false);
  }

  // Register in Command Deck
  if (window.CockpitDeck) {
    window.CockpitDeck.registerView('git', {
      title: 'Git Hygiene & Release',
      icon: '🌿',
      shortcut: 'Alt+6',
      mount: initGitView
    });
  }
})();
