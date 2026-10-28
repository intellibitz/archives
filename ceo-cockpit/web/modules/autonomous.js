window.Cockpit.registerAction({
  id: 'cmd-autonomous-os-start',
  label: 'Launch Model-Laddered Autonomous Loop',
  icon: '🪜',
  group: 'Executive Autonomous',
  description: 'Initiate a 24x7 self-healing, model-laddered autonomous loop (Gemini -> DeepSeek R1 -> Claude 3.7)',
  handler: () => {
    window.Cockpit.closePalette();
    const mission = prompt("Enter Executive Directive for 24x7 Self-Healing Execution:");
    if (!mission) return;

    window.Cockpit.submitPrompt(`[MODEL LADDERED AUTONOMOUS LOOP ENGAGED]: ${mission}`);

    // UI Overlay
    const outputPanel = document.getElementById('outputPanel');
    outputPanel.innerHTML = '';
    const container = document.createElement('div');
    container.style.padding = '15px';
    container.style.backgroundColor = '#0d1117';
    container.style.color = '#c9d1d9';
    container.style.fontFamily = 'monospace';
    container.style.height = '100%';
    container.style.overflowY = 'auto';
    container.innerHTML = `<h3><span style="color:#2ea043;">●</span> CEO Cockpit Model-Laddered Autonomous OS Active</h3>
    <div style="margin-bottom:12px; font-size:0.85em; color:#8b949e;">Ladder Tiers: Level 0 (Gemini 2.5 Flash $0.075/1M) → Level 1 (DeepSeek R1 $0.55/1M) → Level 2 (Claude 3.7 Sonnet $3.00/1M)</div>
    <div id="autoLog"></div>`;
    outputPanel.appendChild(container);

    const logEl = document.getElementById('autoLog');
    const addLog = (text, color = '#c9d1d9', bg = 'transparent') => {
      const el = document.createElement('div');
      el.style.color = color;
      el.style.backgroundColor = bg;
      el.style.padding = '4px 6px';
      el.style.borderRadius = '4px';
      el.style.marginBottom = '6px';
      el.textContent = text;
      logEl.appendChild(el);
      container.scrollTop = container.scrollHeight;
    };

    fetch('/api/autonomous/run', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ mission })
    }).then(async response => {
      const reader = response.body.getReader();
      const decoder = new TextDecoder('utf-8');
      while (true) {
        const { value, done } = await reader.read();
        if (done) break;
        const chunk = decoder.decode(value, { stream: true });
        const lines = chunk.split('\n');
        for (const line of lines) {
          if (line.startsWith('data: ')) {
            try {
              const data = JSON.parse(line.substring(6));
              if (data.type === 'start') addLog(`[INIT]: ${data.message} (${data.initial_model} - ${data.tier})`, '#58a6ff');
              else if (data.type === 'thought') addLog(`[${data.model} - ${data.tier} THINKING]: ${data.content}`, '#8b949e');
              else if (data.type === 'tool_call') addLog(`[EXECUTE - ${data.model}]: ${data.name}(${JSON.stringify(data.args)})`, '#d2a8ff');
              else if (data.type === 'tool_result') addLog(`[RESULT]: ${data.result}`, '#2ea043');
              else if (data.type === 'escalation') addLog(`⚠️ [BUDGET MODEL LADDER ESCALATION]: ${data.reason} (${data.from_model} ➔ ${data.to_model})`, '#d29922', '#211a00');
              else if (data.type === 'done') addLog(`🎉 [STATUS]: ${data.message}`, '#2ea043', '#032312');
              else if (data.type === 'error') addLog(`[FATAL]: ${data.message}`, '#f85149', '#2f1517');
            } catch (e) {
                // Ignore parse errors on partial chunks
            }
          }
        }
      }
    });
  }
});
