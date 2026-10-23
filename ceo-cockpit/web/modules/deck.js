/* =============================================================================
 * CEO Cockpit — Command Deck Shell
 * A slide-out multi-panel workspace that hosts every extension module.
 * Panels self-register via CockpitDeck.registerPanel({...}) and render lazily.
 *
 * Shortcuts: Ctrl+J toggle deck · Alt+1..9 jump to panel · Esc close
 * ========================================================================== */
(function () {
  'use strict';

  const panels = [];
  const state = { open: false, activeId: null, mounted: false };
  let els = {};

  // ---------------------------------------------------------------------------
  // Shared helpers available to all panel modules
  // ---------------------------------------------------------------------------
  const esc = (s) => String(s ?? '').replace(/[&<>"']/g, (c) => (
    { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));

  async function api(path, opts = {}) {
    const init = { method: opts.method || 'GET', headers: {} };
    if (opts.body !== undefined) {
      init.method = opts.method || 'POST';
      init.headers['Content-Type'] = 'application/json';
      init.body = JSON.stringify(opts.body);
    }
    const res = await fetch(path, init);
    let data = {};
    try { data = await res.json(); } catch (_) { /* non-JSON */ }
    if (!res.ok || data.success === false) {
      const err = new Error(data.error || `Request failed (${res.status})`);
      err.status = res.status;
      err.data = data;
      throw err;
    }
    return data;
  }

  /** POST a JSON body and consume a Server-Sent Events response stream. */
  async function streamSSE(path, body, onEvent, signal) {
    const res = await fetch(path, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
      signal
    });
    const ctype = res.headers.get('Content-Type') || '';
    if (!ctype.includes('text/event-stream')) {
      let data = {};
      try { data = await res.json(); } catch (_) { /* ignore */ }
      const err = new Error(data.error || `Stream failed (${res.status})`);
      err.status = res.status;
      err.data = data;
      throw err;
    }
    const reader = res.body.getReader();
    const decoder = new TextDecoder();
    let buffer = '';
    for (;;) {
      const { value, done } = await reader.read();
      if (done) break;
      buffer += decoder.decode(value, { stream: true });
      let idx;
      while ((idx = buffer.indexOf('\n\n')) >= 0) {
        const frame = buffer.slice(0, idx);
        buffer = buffer.slice(idx + 2);
        const line = frame.split('\n').find((l) => l.startsWith('data: '));
        if (!line) continue;
        try { onEvent(JSON.parse(line.slice(6))); } catch (e) { console.warn('SSE parse', e); }
      }
    }
  }

  const fmt = {
    time: (ts) => ts ? new Date(ts * 1000).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' }) : '—',
    dateTime: (ts) => ts ? new Date(ts * 1000).toLocaleString([], { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' }) : '—',
    ago: (ts) => {
      if (!ts) return '—';
      const s = Math.max(0, Date.now() / 1000 - ts);
      if (s < 60) return `${Math.floor(s)}s ago`;
      if (s < 3600) return `${Math.floor(s / 60)}m ago`;
      if (s < 86400) return `${Math.floor(s / 3600)}h ago`;
      return `${Math.floor(s / 86400)}d ago`;
    },
    duration: (ms) => {
      if (ms == null) return '—';
      if (ms < 1000) return `${ms}ms`;
      if (ms < 60000) return `${(ms / 1000).toFixed(1)}s`;
      return `${Math.floor(ms / 60000)}m ${Math.round((ms % 60000) / 1000)}s`;
    },
    usd: (v, d = 4) => `$${(Number(v) || 0).toFixed(d)}`,
    num: (v) => (Number(v) || 0).toLocaleString()
  };

  function toast(message, level = 'info', ms = 3800) {
    let host = document.getElementById('deckToastHost');
    if (!host) {
      host = document.createElement('div');
      host.id = 'deckToastHost';
      host.className = 'deck-toast-host';
      document.body.appendChild(host);
    }
    const t = document.createElement('div');
    t.className = `deck-toast deck-toast-${level}`;
    const icon = { success: '✅', error: '⛔', warning: '⚠️', info: '💠' }[level] || '💠';
    t.innerHTML = `<span class="deck-toast-icon">${icon}</span><span>${esc(message)}</span>`;
    host.appendChild(t);
    requestAnimationFrame(() => t.classList.add('show'));
    setTimeout(() => { t.classList.remove('show'); setTimeout(() => t.remove(), 300); }, ms);
  }

  // ---------------------------------------------------------------------------
  // Deck lifecycle
  // ---------------------------------------------------------------------------
  function mount() {
    if (state.mounted) return;
    els = {
      backdrop: document.getElementById('deckBackdrop'),
      nav: document.getElementById('deckNav'),
      title: document.getElementById('deckTitle'),
      subtitle: document.getElementById('deckSubtitle'),
      icon: document.getElementById('deckIcon'),
      actions: document.getElementById('deckHeaderActions'),
      content: document.getElementById('deckContent'),
      close: document.getElementById('btnCloseDeck'),
      trigger: document.getElementById('btnOpenDeck')
    };
    if (!els.backdrop) { console.warn('Command Deck markup missing'); return; }
    state.mounted = true;

    els.close.addEventListener('click', close);
    els.backdrop.addEventListener('click', (e) => { if (e.target === els.backdrop) close(); });
    if (els.trigger) els.trigger.addEventListener('click', () => open());

    document.addEventListener('keydown', (e) => {
      const tag = (e.target && e.target.tagName) || '';
      if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'j') {
        e.preventDefault();
        state.open ? close() : open();
      } else if (e.altKey && /^[1-9]$/.test(e.key)) {
        const p = panels[Number(e.key) - 1];
        if (p) { e.preventDefault(); open(p.id); }
      } else if (e.key === 'Escape' && state.open && tag !== 'INPUT' && tag !== 'TEXTAREA') {
        close();
      }
    });

    renderNav();
    panels.forEach(registerPaletteAction);
  }

  function renderNav() {
    if (!els.nav) return;
    els.nav.innerHTML = '';
    panels.forEach((p, i) => {
      const b = document.createElement('button');
      b.className = 'deck-nav-item';
      b.id = `deckNav_${p.id}`;
      b.dataset.panel = p.id;
      b.title = `${p.title} (Alt+${i + 1})`;
      b.innerHTML = `
        <span class="deck-nav-icon">${p.icon}</span>
        <span class="deck-nav-label">${esc(p.navLabel || p.title)}</span>
        ${i < 9 ? `<span class="deck-nav-key">${i + 1}</span>` : ''}
        <span class="deck-nav-badge" id="deckBadge_${p.id}" style="display:none"></span>`;
      b.addEventListener('click', () => activate(p.id));
      els.nav.appendChild(b);
    });
  }

  function registerPaletteAction(p) {
    if (!window.Cockpit || p._paletteRegistered) return;
    p._paletteRegistered = true;
    window.Cockpit.registerAction({
      title: `Deck: ${p.title}`,
      desc: p.subtitle,
      icon: p.icon,
      action: () => open(p.id)
    });
    (p.paletteActions || []).forEach((a) => window.Cockpit.registerAction(a));
  }

  function open(panelId) {
    mount();
    if (!els.backdrop) return;
    els.backdrop.style.display = 'flex';
    requestAnimationFrame(() => els.backdrop.classList.add('open'));
    state.open = true;
    activate(panelId || state.activeId || (panels[0] && panels[0].id));
  }

  function close() {
    if (!els.backdrop) return;
    els.backdrop.classList.remove('open');
    state.open = false;
    const active = panels.find((p) => p.id === state.activeId);
    if (active && active.onHide) active.onHide();
    setTimeout(() => { if (!state.open) els.backdrop.style.display = 'none'; }, 260);
  }

  function activate(panelId) {
    const p = panels.find((x) => x.id === panelId);
    if (!p) return;
    const prev = panels.find((x) => x.id === state.activeId);
    if (prev && prev !== p && prev.onHide) prev.onHide();
    state.activeId = p.id;

    els.nav.querySelectorAll('.deck-nav-item').forEach((n) => n.classList.toggle('active', n.dataset.panel === p.id));
    els.title.textContent = p.title;
    els.subtitle.textContent = p.subtitle || '';
    els.icon.textContent = p.icon;

    els.content.querySelectorAll('.deck-panel-view').forEach((v) => { v.style.display = 'none'; });
    if (!p._view) {
      p._view = document.createElement('div');
      p._view.className = `deck-panel-view deck-view-${p.id}`;
      p._view.id = `deckView_${p.id}`;
      els.content.appendChild(p._view);
      p._actions = document.createElement('div');
      p._actions.className = 'deck-header-actions-group';
      try { p.render(p._view, p._actions); } catch (e) {
        p._view.innerHTML = `<div class="deck-empty">Panel failed to render: ${esc(e.message)}</div>`;
        console.error(e);
      }
    }
    els.actions.innerHTML = '';
    els.actions.appendChild(p._actions);
    p._view.style.display = '';
    if (p.onShow) { try { p.onShow(); } catch (e) { console.error(e); } }
  }

  function setBadge(panelId, value, tone = 'cyan') {
    const b = document.getElementById(`deckBadge_${panelId}`);
    if (!b) return;
    if (value === null || value === undefined || value === '' || value === 0) {
      b.style.display = 'none';
      return;
    }
    b.textContent = value;
    b.className = `deck-nav-badge tone-${tone}`;
    b.style.display = '';
  }

  window.CockpitDeck = {
    registerPanel(def) {
      if (!def || !def.id || typeof def.render !== 'function') throw new Error('Invalid panel definition');
      def.order = def.order ?? 100;
      panels.push(def);
      panels.sort((a, b) => a.order - b.order);
      if (state.mounted) { renderNav(); registerPaletteAction(def); }
    },
    registerView(id, def) {
      if (!id || !def) return;
      const shortcutNum = def.shortcut ? parseInt(def.shortcut.replace(/\D/g, ''), 10) : null;
      const order = def.order ?? (shortcutNum ? shortcutNum * 10 : 100);
      this.registerPanel({
        id: id,
        order: order,
        title: def.title || id,
        navLabel: def.navLabel || (def.title ? def.title.split(' ')[0] : id),
        subtitle: def.subtitle || def.desc || def.title || '',
        icon: def.icon || '📦',
        render: (view, actions) => {
          if (typeof def.mount === 'function') {
            def.mount(view);
          } else if (typeof def.render === 'function') {
            def.render(view, actions);
          }
        },
        onShow: def.onShow,
        onHide: def.onHide,
        paletteActions: def.paletteActions || []
      });
    },
    open, close, activate, setBadge, toast, api, streamSSE, esc, fmt,
    isOpen: () => state.open,
    activePanel: () => state.activeId,
    getPanels: () => panels.slice()
  };

  document.addEventListener('cockpit:ready', mount);
})();
