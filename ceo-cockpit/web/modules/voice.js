// Round 5 — Audio & Executive Voice Briefing Engine
(function() {
  'use strict';

  const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
  const synth = window.speechSynthesis;

  let recognition = null;
  let isListening = false;
  let currentUtterance = null;

  // Initialize Speech Recognition
  if (SpeechRecognition) {
    recognition = new SpeechRecognition();
    recognition.continuous = false;
    recognition.interimResults = true;
    recognition.lang = 'en-US';
  }

  // Voice Engine State
  const VoiceState = {
    selectedVoice: null,
    rate: 1.05,
    pitch: 1.0,
    volume: 1.0,
    autoReadConsensus: false
  };

  function getAvailableVoices() {
    if (!synth) return [];
    return synth.getVoices().filter(v => v.lang.startsWith('en'));
  }

  // Playback helper
  function speak(text, onEnd) {
    if (!synth) return;
    synth.cancel(); // cancel any active speech

    // Clean markdown before speaking
    const cleanText = text
      .replace(/[#*`_~]/g, '')
      .replace(/\[([^\]]+)\]\([^)]+\)/g, '$1')
      .replace(/\n+/g, ' ')
      .trim();

    if (!cleanText) return;

    currentUtterance = new SpeechSynthesisUtterance(cleanText);
    currentUtterance.rate = VoiceState.rate;
    currentUtterance.pitch = VoiceState.pitch;
    currentUtterance.volume = VoiceState.volume;

    const voices = getAvailableVoices();
    if (VoiceState.selectedVoice) {
      const matched = voices.find(v => v.name === VoiceState.selectedVoice);
      if (matched) currentUtterance.voice = matched;
    }

    currentUtterance.onend = () => {
      document.querySelectorAll('.voice-wave-active').forEach(el => el.classList.remove('voice-wave-active'));
      if (onEnd) onEnd();
    };

    currentUtterance.onerror = () => {
      document.querySelectorAll('.voice-wave-active').forEach(el => el.classList.remove('voice-wave-active'));
    };

    document.querySelectorAll('.voice-wave-container').forEach(el => el.classList.add('voice-wave-active'));
    synth.speak(currentUtterance);
  }

  function stopSpeaking() {
    if (synth) synth.cancel();
    document.querySelectorAll('.voice-wave-active').forEach(el => el.classList.remove('voice-wave-active'));
  }

  // Mount View in Command Deck
  function initVoiceView(container) {
    const isSupported = !!(SpeechRecognition && synth);
    const voices = getAvailableVoices();

    container.innerHTML = `
      <div class="deck-toolbar">
        <div class="deck-toolbar-left">
          <span class="deck-panel-title">🎙️ Audio & Executive Voice Briefing Engine</span>
          <span class="badge ${isSupported ? 'badge-accent' : 'badge-error'}">
            ${isSupported ? 'Speech API Active' : 'Speech API Unavailable'}
          </span>
        </div>
        <div class="deck-toolbar-right">
          <button class="deck-btn" id="btnStopSpeech">⏹️ Stop Audio</button>
        </div>
      </div>

      <div class="deck-content-split">
        <!-- Left: Speech Input & Dictation -->
        <div class="deck-pane deck-pane-left">
          <div class="deck-section-label">Voice Directive Dictation</div>
          <div class="voice-card">
            <div class="voice-mic-container">
              <button class="voice-mic-button" id="btnVoiceMic" title="Click to speak directive">
                🎤
              </button>
              <div class="voice-mic-label" id="voiceMicLabel">Click microphone to dictate executive directive</div>
            </div>
            <div class="voice-transcript-box" id="voiceTranscriptBox">
              <em class="text-muted">Awaiting voice capture...</em>
            </div>
            <div class="voice-actions-row">
              <button class="btn btn-primary" id="btnDispatchDictation" disabled>
                🚀 Send Captured Directive to Cockpit
              </button>
            </div>
          </div>

          <div class="deck-section-label" style="margin-top: 14px;">Voice Engine Tuning</div>
          <div class="voice-tuning-card">
            <div class="tuning-row">
              <label>Voice Profile</label>
              <select id="selectVoiceProfile" class="deck-select">
                ${voices.map(v => `<option value="${v.name}">${v.name} (${v.lang})</option>`).join('')}
              </select>
            </div>
            <div class="tuning-row">
              <label>Pacing (Speed)</label>
              <input type="range" id="rangeSpeechRate" min="0.8" max="1.4" step="0.05" value="${VoiceState.rate}" />
              <span id="rateVal" class="badge">${VoiceState.rate}x</span>
            </div>
          </div>
        </div>

        <!-- Right: Executive Audio Briefing Player -->
        <div class="deck-pane deck-pane-right">
          <div class="deck-section-label">Executive Audio Briefing Player</div>
          <div class="voice-card">
            <div class="voice-briefing-header">
              <span class="badge badge-accent">Briefing Synthesizer</span>
              <div class="voice-wave-container">
                <span class="wave-bar"></span>
                <span class="wave-bar"></span>
                <span class="wave-bar"></span>
                <span class="wave-bar"></span>
                <span class="wave-bar"></span>
              </div>
            </div>
            <div class="voice-briefing-text" id="voiceBriefingText">
              Ready to generate verbal audio briefing from the active executive output or consensus.
            </div>
            <div class="voice-briefing-controls">
              <button class="btn btn-primary" id="btnReadActiveOutput">
                🔊 Read Active Model Synthesis
              </button>
              <button class="deck-btn" id="btnTestBriefing">
                🧪 Test Voice Greeting
              </button>
            </div>
          </div>
        </div>
      </div>
    `;

    // Elements
    const btnMic = container.querySelector('#btnVoiceMic');
    const micLabel = container.querySelector('#voiceMicLabel');
    const transcriptBox = container.querySelector('#voiceTranscriptBox');
    const btnDispatch = container.querySelector('#btnDispatchDictation');
    const selectVoice = container.querySelector('#selectVoiceProfile');
    const rangeRate = container.querySelector('#rangeSpeechRate');
    const rateVal = container.querySelector('#rateVal');
    const btnStop = container.querySelector('#btnStopSpeech');
    const btnReadActive = container.querySelector('#btnReadActiveOutput');
    const btnTest = container.querySelector('#btnTestBriefing');
    const briefingText = container.querySelector('#voiceBriefingText');

    let capturedText = '';

    // Tuning handlers
    selectVoice.addEventListener('change', () => {
      VoiceState.selectedVoice = selectVoice.value;
    });
    rangeRate.addEventListener('input', () => {
      VoiceState.rate = parseFloat(rangeRate.value);
      rateVal.textContent = `${VoiceState.rate.toFixed(2)}x`;
    });

    // Mic Dictation
    if (recognition) {
      recognition.onstart = () => {
        isListening = true;
        btnMic.classList.add('recording');
        micLabel.textContent = 'Listening... Speak your executive directive clearly.';
      };

      recognition.onresult = (event) => {
        let interim = '';
        for (let i = event.resultIndex; i < event.results.length; ++i) {
          if (event.results[i].isFinal) {
            capturedText += event.results[i][0].transcript + ' ';
          } else {
            interim += event.results[i][0].transcript;
          }
        }
        transcriptBox.innerHTML = `<strong>${capturedText}</strong> <span class="text-muted">${interim}</span>`;
        btnDispatch.disabled = !capturedText.trim();
      };

      recognition.onerror = (e) => {
        micLabel.textContent = `Mic error: ${e.error}`;
        btnMic.classList.remove('recording');
        isListening = false;
      };

      recognition.onend = () => {
        isListening = false;
        btnMic.classList.remove('recording');
        micLabel.textContent = capturedText.trim() ? 'Dictation complete.' : 'Click microphone to dictate executive directive.';
      };

      btnMic.addEventListener('click', () => {
        if (isListening) {
          recognition.stop();
        } else {
          capturedText = '';
          transcriptBox.innerHTML = '<em class="text-muted">Listening...</em>';
          recognition.start();
        }
      });
    } else {
      btnMic.disabled = true;
      micLabel.textContent = 'Speech Recognition is not supported in this browser.';
    }

    // Dispatch voice directive to main prompt input
    btnDispatch.addEventListener('click', () => {
      const promptEl = document.getElementById('executivePrompt');
      if (promptEl && capturedText.trim()) {
        promptEl.value = capturedText.trim();
        promptEl.focus();
        if (window.CockpitDeck) window.CockpitDeck.close();
        if (window.Cockpit) window.Cockpit.notify('Voice directive injected into executive cockpit.', 'info');
      }
    });

    // Test Audio
    btnTest.addEventListener('click', () => {
      const greeting = 'Welcome back, CEO. Antigravity Mission Control is fully nominal, budget governor active, and all multi-agent swarms ready for strategic deployment.';
      briefingText.textContent = greeting;
      speak(greeting);
    });

    // Read Active Output
    btnReadActive.addEventListener('click', () => {
      const outputEl = document.getElementById('outputContent');
      const text = outputEl ? outputEl.innerText : '';
      if (text && text.trim()) {
        briefingText.textContent = text.substring(0, 400) + '...';
        speak(text);
      } else {
        briefingText.textContent = 'No active synthesis available in main terminal.';
      }
    });

    btnStop.addEventListener('click', stopSpeaking);
  }

  // Export Global Voice API
  window.CockpitVoice = {
    speak,
    stop: stopSpeaking,
    isAvailable: !!synth
  };

  // Register with Command Deck
  if (window.CockpitDeck) {
    window.CockpitDeck.registerView('voice', {
      title: 'Executive Voice Engine',
      icon: '🎙️',
      shortcut: 'Alt+3',
      mount: initVoiceView
    });
  }
})();
