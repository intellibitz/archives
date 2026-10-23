// Round 9 — Real-Time Activity Event Stream & Audit Trail (Frontend View)
(function() {
  'use strict';

  function initActivityView(container) {
    container.innerHTML = `
      <div class="deck-toolbar">
        <div class="deck-toolbar-left">
          <span class="deck-panel-title">📡 Live Operational Event Stream & Audit Trail</span>
          <span class="badge badge-accent" id="activityLiveBadge">● Live SSE Connected</span>
        </div>
        <div class="deck-toolbar-right">
          <div class="activity-filters" id="activityFilters">
            <button class="deck-btn-xs filter-chip active" data-filter="">All</button>
            <button class="deck-btn-xs filter-chip" data-filter="info">Info</button>
            <button class="deck-btn-xs filter-chip" data-filter="success">Success</button>
            <button class="deck-btn-xs filter-chip" data-filter="warning">Warning</button>
            <button class="deck-btn-xs filter-chip" data-filter="error">Error</button>
          </div>
          <button class="deck-btn" id="btnExportAudit">📑 Export Audit Trail</button>
          <button class="deck-btn" id="btnClearActivity">🧹 Clear Display</button>
        </div>
      </div>

      <div class="activity-layout">
        <div class="activity-stream-card">
          <div class="activity-feed" id="activityFeed">
            <div class="deck-empty">Awaiting incoming operational events...</div>
          </div>
        </div>
      </div>
    `;

    // Elements
    const feed = container.querySelector('#activityFeed');
    const liveBadge = container.querySelector('#activityLiveBadge');
    const filters = container.querySelector('#activityFilters');
    const btnExport = container.querySelector('#btnExportAudit');
    const btnClear = container.querySelector('#btnClearActivity');

    let allEvents = [];
    let activeFilter = '';
    let eventSource = null;

    // Severity Filter chips
    filters.querySelectorAll('.filter-chip').forEach(chip => {
      chip.addEventListener('click', () => {
        filters.querySelectorAll('.filter-chip').forEach(c => c.classList.remove('active'));
        chip.classList.add('active');
        activeFilter = chip.dataset.filter;
        renderFeed();
      });
    });

    function addEvent(evt) {
      allEvents.unshift(evt);
      if (allEvents.length > 300) allEvents.pop();
      renderFeed();
    }

    function renderFeed() {
      const filtered = activeFilter
        ? allEvents.filter(e => (e.level || 'info').toLowerCase() === activeFilter)
        : allEvents;

      if (!filtered.length) {
        feed.innerHTML = '<div class="deck-empty">No events matching current filter.</div>';
        return;
      }

      feed.innerHTML = filtered.map(e => {
        const timeStr = e.time || new Date().toLocaleTimeString();
        const levelClass = getLevelBadgeClass(e.level);
        return `
          <div class="activity-event-row ${e.level || 'info'}">
            <div class="event-time">${timeStr}</div>
            <span class="badge ${levelClass}">${e.source || 'cockpit'}</span>
            <div class="event-body">
              <strong class="event-title">${escapeHtml(e.title || '')}</strong>
              ${e.detail ? `<span class="event-detail">${escapeHtml(e.detail)}</span>` : ''}
            </div>
            <span class="event-level-pill">${e.level || 'info'}</span>
          </div>
        `;
      }).join('');
    }

    function getLevelBadgeClass(level) {
      switch (level) {
        case 'success': return 'badge-success';
        case 'warning': return 'badge-accent';
        case 'error': return 'badge-error';
        default: return 'badge-subtle';
      }
    }

    function escapeHtml(str) {
      return String(str).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
    }

    // Connect SSE
    function connectStream() {
      if (eventSource) eventSource.close();
      eventSource = new EventSource('/api/activity/stream');

      eventSource.onopen = () => {
        liveBadge.textContent = '● Live SSE Connected';
        liveBadge.className = 'badge badge-success';
      };

      eventSource.onmessage = (msg) => {
        try {
          const data = JSON.parse(msg.data);
          if (data.type === 'snapshot' && data.events) {
            allEvents = data.events.reverse();
            renderFeed();
          } else if (data.type === 'event' && data.event) {
            addEvent(data.event);
          }
        } catch (err) {
          console.error('SSE parse error:', err);
        }
      };

      eventSource.onerror = () => {
        liveBadge.textContent = '○ Reconnecting...';
        liveBadge.className = 'badge badge-accent';
      };
    }

    // Export Audit Trail
    btnExport.addEventListener('click', () => {
      const json = JSON.stringify(allEvents, null, 2);
      navigator.clipboard.writeText(json);
      if (window.Cockpit) window.Cockpit.notify('Audit ledger copied to clipboard (JSON format).', 'success');
    });

    btnClear.addEventListener('click', () => {
      allEvents = [];
      renderFeed();
    });

    connectStream();
  }

  // Register in Command Deck
  if (window.CockpitDeck) {
    window.CockpitDeck.registerView('activity', {
      title: 'Activity Stream & Audit',
      icon: '📡',
      shortcut: 'Alt+7',
      mount: initActivityView
    });
  }
})();
