/* =============================================================================
 * Round 13 — Executive Knowledge Graph & Semantic Vector Memory
 * Qdrant vector memory integration, semantic indexing, and search.
 * ========================================================================== */
(function () {
  'use strict';

  const D = () => window.CockpitDeck;

  async function render(view) {
    view.innerHTML = `
      <div class="deck-split-view">
        <div class="deck-pane" style="flex: 2;">
          <div class="deck-pane-header">
            <h3>🧠 Semantic Vector Memory Vault</h3>
            <div style="display:flex;gap:8px;">
              <button class="deck-btn-sm" id="btnRefreshMemory">🔄 Refresh</button>
              <button class="deck-btn-sm primary" id="btnIndexMemoryModal">＋ Index Memory</button>
            </div>
          </div>
          <div id="memoryStatsGrid" class="deck-stats-grid">
            <div class="deck-stat-card"><span class="deck-stat-val text-cyan" id="statTotalMemories">—</span><span class="deck-stat-lbl">Indexed Vectors</span></div>
            <div class="deck-stat-card"><span class="deck-stat-val text-green" id="statQdrantStatus">Probing...</span><span class="deck-stat-lbl">Vector Engine</span></div>
            <div class="deck-stat-card"><span class="deck-stat-val text-purple">1536-D</span><span class="deck-stat-lbl">Embedding Space</span></div>
          </div>

          <div style="margin-top:16px;display:flex;gap:8px;">
            <input type="text" id="memorySearchInput" class="ws-path-input" placeholder="Search executive memory (e.g. routing strategy, budget cap, automation)..." style="flex:1;" />
            <button id="btnSearchMemory" class="ws-apply-btn" style="min-width:100px;">🔍 Search</button>
          </div>

          <div id="memoryCardsList" style="margin-top:16px;display:flex;flex-direction:column;gap:12px;max-height:480px;overflow-y:auto;padding-right:4px;">
            <p class="deck-muted" style="text-align:center;">Loading memory vault...</p>
          </div>
        </div>

        <div class="deck-pane" style="flex: 1;">
          <div class="deck-pane-header">
            <h3>📚 Category Distribution</h3>
          </div>
          <div id="memoryCategoryBreakdown" style="padding:10px;">
            <!-- Render category pills -->
          </div>
          <div style="margin-top:24px;">
            <h4>Engine Topology</h4>
            <div style="background:#0F172A;border:1px solid #1E293B;border-radius:8px;padding:12px;font-size:12px;color:#94A3B8;line-height:1.6;">
              <p>• <strong>Primary Engine:</strong> Qdrant (Port 6333)</p>
              <p>• <strong>Collection:</strong> agent_memory / ceo_vault</p>
              <p>• <strong>Persistence:</strong> JSON Hybrid Sync</p>
              <p>• <strong>Auto-Cluster:</strong> ADRs, Directives, Post-Mortems</p>
            </div>
          </div>
        </div>
      </div>

      <!-- Index Memory Modal -->
      <div id="indexMemoryModalBackdrop" style="display:none;position:fixed;top:0;left:0;right:0;bottom:0;background:rgba(0,0,0,0.7);z-index:9999;align-items:center;justify-content:center;">
        <div style="background:#1E293B;border:1px solid #334155;border-radius:12px;width:500px;padding:24px;box-shadow:0 20px 40px rgba(0,0,0,0.5);">
          <h3 style="color:#F8FAFC;margin-top:0;">Index Executive Knowledge Vector</h3>
          <label style="color:#94A3B8;font-size:12px;display:block;margin-top:12px;">Title / Subject:</label>
          <input type="text" id="newMemTitle" class="ws-path-input" placeholder="e.g. ADR-002: Local Model Failover Policy" style="width:100%;margin-top:4px;" />
          <label style="color:#94A3B8;font-size:12px;display:block;margin-top:12px;">Category:</label>
          <select id="newMemCategory" class="harness-select" style="width:100%;margin-top:4px;">
            <option value="Architecture">Architecture (ADR)</option>
            <option value="Financial">Financial / Budget</option>
            <option value="Operations">Operations / Runbook</option>
            <option value="Incident">Incident Post-Mortem</option>
            <option value="Directive">CEO Directive</option>
          </select>
          <label style="color:#94A3B8;font-size:12px;display:block;margin-top:12px;">Tags (comma separated):</label>
          <input type="text" id="newMemTags" class="ws-path-input" placeholder="failover, ollama, latency" style="width:100%;margin-top:4px;" />
          <label style="color:#94A3B8;font-size:12px;display:block;margin-top:12px;">Content / Decision Body:</label>
          <textarea id="newMemContent" class="ws-path-input" rows="4" placeholder="Detail the architecture decision, directive, or synthesis..." style="width:100%;margin-top:4px;resize:vertical;"></textarea>
          <div style="display:flex;justify-content:flex-end;gap:8px;margin-top:20px;">
            <button id="btnCancelIndexMemory" class="deck-btn-sm">Cancel</button>
            <button id="btnConfirmIndexMemory" class="ws-apply-btn">Store in Vault</button>
          </div>
        </div>
      </div>
    `;

    document.getElementById('btnRefreshMemory').addEventListener('click', () => { loadStats(); searchMemories(); });
    document.getElementById('btnSearchMemory').addEventListener('click', searchMemories);
    document.getElementById('memorySearchInput').addEventListener('keydown', (e) => {
      if (e.key === 'Enter') searchMemories();
    });

    document.getElementById('btnIndexMemoryModal').addEventListener('click', () => {
      document.getElementById('indexMemoryModalBackdrop').style.display = 'flex';
    });
    document.getElementById('btnCancelIndexMemory').addEventListener('click', () => {
      document.getElementById('indexMemoryModalBackdrop').style.display = 'none';
    });
    document.getElementById('btnConfirmIndexMemory').addEventListener('click', indexMemory);

    await loadStats();
    await searchMemories();
  }

  async function loadStats() {
    try {
      const data = await D().api('/api/memory/stats');
      document.getElementById('statTotalMemories').textContent = data.total_memories ?? 0;
      
      const q = data.qdrant || {};
      const statusEl = document.getElementById('statQdrantStatus');
      if (statusEl) {
        statusEl.textContent = q.online ? 'Qdrant (Online)' : 'Hybrid JSON (Active)';
        statusEl.className = q.online ? 'deck-stat-val text-green' : 'deck-stat-val text-purple';
      }

      const catHost = document.getElementById('memoryCategoryBreakdown');
      if (catHost && data.categories) {
        catHost.innerHTML = Object.entries(data.categories).map(([cat, count]) => `
          <div style="display:flex;justify-content:space-between;align-items:center;padding:6px 0;border-bottom:1px solid #1E293B;">
            <span style="color:#F1F5F9;font-size:13px;">${escapeHtml(cat)}</span>
            <span class="deck-badge">${count}</span>
          </div>
        `).join('');
      }
    } catch (err) {
      console.error('Failed to load memory stats:', err);
    }
  }

  async function searchMemories() {
    const q = (document.getElementById('memorySearchInput')?.value || '').trim();
    const host = document.getElementById('memoryCardsList');
    if (!host) return;

    try {
      const data = await D().api(`/api/memory/search?q=${encodeURIComponent(q)}`);
      const results = data.results || [];

      if (results.length === 0) {
        host.innerHTML = '<p class="deck-muted" style="text-align:center;padding:20px;">No matching vector memories found.</p>';
        return;
      }

      host.innerHTML = results.map(m => `
        <div style="background:#1E293B;border:1px solid #334155;border-radius:8px;padding:14px;">
          <div style="display:flex;justify-content:space-between;align-items:center;">
            <h4 style="margin:0;color:#F8FAFC;font-size:14px;">${escapeHtml(m.title)}</h4>
            <div style="display:flex;gap:6px;align-items:center;">
              <span class="deck-badge" style="background:#0284C7;color:#fff;">${Math.round((m.score || 1) * 100)}% match</span>
              <span class="deck-status-pill green">${escapeHtml(m.category || 'General')}</span>
            </div>
          </div>
          <p style="margin:8px 0;color:#CBD5E1;font-size:13px;line-height:1.5;">${escapeHtml(m.content)}</p>
          <div style="display:flex;gap:6px;flex-wrap:wrap;margin-top:8px;">
            ${(m.tags || []).map(t => `<span style="background:#0F172A;color:#94A3B8;padding:2px 8px;border-radius:4px;font-size:11px;">#${escapeHtml(t)}</span>`).join('')}
          </div>
        </div>
      `).join('');
    } catch (err) {
      host.innerHTML = `<p class="text-rose" style="padding:10px;">Search error: ${err.message}</p>`;
    }
  }

  async function indexMemory() {
    const title = document.getElementById('newMemTitle').value.trim();
    const category = document.getElementById('newMemCategory').value;
    const tags = document.getElementById('newMemTags').value.trim();
    const content = document.getElementById('newMemContent').value.trim();

    if (!title || !content) {
      alert('Title and content are required');
      return;
    }

    try {
      await D().api('/api/memory/index', {
        method: 'POST',
        body: { title, category, tags, content }
      });
      document.getElementById('indexMemoryModalBackdrop').style.display = 'none';
      await loadStats();
      await searchMemories();
    } catch (err) {
      alert(`Indexing failed: ${err.message}`);
    }
  }

  function escapeHtml(str) {
    return String(str || '').replace(/[&<>"']/g, c => ({
      '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
    }[c]));
  }

  CockpitDeck.registerView('memory_vault', {
    title: 'Executive Knowledge & Vector Memory',
    navLabel: 'Memory Vault',
    icon: '🧠',
    order: 75,
    shortcut: 'Alt+9',
    render: render
  });
})();
