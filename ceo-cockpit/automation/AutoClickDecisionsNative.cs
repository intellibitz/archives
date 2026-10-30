// AutoClickDecisionsNative - click approval/decision buttons via Windows UI Automation.
//
// Fixed failure modes of the previous build:
//   * It matched with StartsWith, so a chat bubble titled
//     "Acceptance failed on the merged main state..." or a notification titled
//     "Allow connections from other machines and vscode.dev/agents" counted as a
//     target button.  Matching is now an exact, normalized label lookup.
//   * It scanned every window on the desktop and clicked in the background,
//     stealing focus.  It now inspects only the foreground window.
//   * It re-clicked the same element every polling pass, forever.  Clicks are
//     now cooled down, retried at most MaxAttempts times, then blacklisted.
//   * Its mouse fallback moved the cursor and ran without DPI awareness, so the
//     pointer landed wherever logical/physical coordinates diverged.  The
//     process is now Per-Monitor-V2 aware, and the cursor is only moved by an
//     explicit opt-in (--allow-mouse-fallback) that restores the pointer.
//   * Two instances could run at once (event handler + polling loop + a second
//     launcher).  A named mutex enforces a single instance and only the polling
//     loop remains.
//
// Built with the in-box .NET Framework compiler, so this file sticks to C# 5.
//
//   csc /nologo /target:exe /platform:anycpu /optimize+ /debug- ^
//       /r:UIAutomationClient.dll /r:UIAutomationTypes.dll /r:WindowsBase.dll ^
//       /out:AutoClickDecisionsNative.exe AutoClickDecisionsNative.cs
//
// Flags:  --dry-run   log decisions, never touch input
//         --self-test validate the matcher offline and exit
//         --once      one scan then exit
//         --debug     log per-scan detail and skip reasons
//         --probe-focus  verify focus lands on the target, never send a key
//         --no-keyboard  disable focus+Space activation
//         --allow-mouse-fallback  permit a physical click when no UIA pattern and
//                                 no keyboard activation works (moves the cursor)

using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Runtime.InteropServices;
using System.Text.RegularExpressions;
using System.Threading;
using System.Windows.Automation;

namespace AutoClickDecisions
{
    internal static class Program
    {
        // ------------------------------------------------------------------ //
        // Configuration
        // ------------------------------------------------------------------ //

        const int PollIntervalMs = 800;        // scan cadence
        const int HeartbeatMs = 5000;          // health-check timestamp cadence
        const int DefaultUserIdleGuardMs = 800; // never click while the user is active
        const int SameElementCooldownMs = 4000;
        const int MaxAttempts = 3;             // clicks before blacklisting
        const int BlacklistMs = 120000;
        const int MaxLabelLength = 40;         // "Acceptance failed on the merged..." is not a button
        const double MinAspect = 1.2;
        const double MaxAspect = 12.0;

        // Lower index wins when several buttons are visible at once.
        static readonly string[] TargetPriority = new string[]
        {
            "Accept All",
            "Accept All 2",
            "Accept Changes",
            "Accept edits",
            "Accept",
            "Keep All",
            "Proceed",
            "Yes, Don't Ask",
            "Yes Don't Ask",
            "Allow Always",
            "Always Allow",
            "Always Run",
            "Allow Once",
            "Allow"
        };

        static readonly Dictionary<string, string> TargetMap = BuildTargetMap();
        static readonly Dictionary<string, AttemptState> States = new Dictionary<string, AttemptState>();
        static readonly Dictionary<string, DateTime> Blacklist = new Dictionary<string, DateTime>();

        static string _logFile;
        static string _pidFile;
        static string _heartbeatFile;
        static DateTime _lastHeartbeat = DateTime.MinValue;
        static bool _debug;
        static bool _allowKeyboard = true;
        static bool _allowPostClick = true;
        static bool _forcePostClick;
        static bool _probeFocus;
        static DateTime _lastSlowScanNotice = DateTime.MinValue;

        sealed class AttemptState
        {
            public int Attempts;
            public DateTime LastClick = DateTime.MinValue;
            public DateTime FirstSeen = DateTime.MinValue;
            public DateTime LastSeen = DateTime.MinValue;
        }

        sealed class Hit
        {
            public string Canonical;
            public string Key;
            public int Priority;
            public System.Windows.Rect Rect;
            public string WindowTitle;
            public IntPtr Window;
            public AutomationElement Element;
        }

        // ------------------------------------------------------------------ //
        // Win32
        // ------------------------------------------------------------------ //

        [StructLayout(LayoutKind.Sequential)]
        struct POINT
        {
            public int X;
            public int Y;
        }

        [StructLayout(LayoutKind.Sequential)]
        struct LASTINPUTINFO
        {
            public uint cbSize;
            public uint dwTime;
        }

        [DllImport("user32.dll")]
        static extern IntPtr GetForegroundWindow();

        [DllImport("user32.dll")]
        static extern uint GetWindowThreadProcessId(IntPtr hWnd, out uint processId);

        [DllImport("user32.dll")]
        static extern bool GetCursorPos(out POINT point);

        [DllImport("user32.dll")]
        static extern bool SetCursorPos(int x, int y);

        [DllImport("user32.dll")]
        static extern short GetAsyncKeyState(int vKey);

        [DllImport("user32.dll")]
        static extern bool GetLastInputInfo(ref LASTINPUTINFO info);

        [DllImport("kernel32.dll")]
        static extern uint GetTickCount();

        [StructLayout(LayoutKind.Sequential)]
        struct MOUSEINPUT
        {
            public int dx;
            public int dy;
            public uint mouseData;
            public uint dwFlags;
            public uint time;
            public UIntPtr dwExtraInfo;
        }

        [StructLayout(LayoutKind.Sequential)]
        struct KEYBDINPUT
        {
            public ushort wVk;
            public ushort wScan;
            public uint dwFlags;
            public uint time;
            public UIntPtr dwExtraInfo;
        }

        [StructLayout(LayoutKind.Explicit)]
        struct INPUTUNION
        {
            [FieldOffset(0)] public MOUSEINPUT mi;
            [FieldOffset(0)] public KEYBDINPUT ki;
        }

        [StructLayout(LayoutKind.Sequential)]
        struct INPUT
        {
            public uint type;
            public INPUTUNION u;
        }

        [DllImport("user32.dll", SetLastError = true)]
        static extern uint SendInput(uint count, INPUT[] inputs, int size);

        [DllImport("user32.dll")]
        static extern IntPtr WindowFromPoint(POINT point);

        [DllImport("user32.dll")]
        static extern bool ScreenToClient(IntPtr hWnd, ref POINT point);

        [DllImport("user32.dll")]
        static extern IntPtr PostMessage(IntPtr hWnd, uint msg, IntPtr wParam, IntPtr lParam);

        [DllImport("user32.dll")]
        static extern IntPtr GetAncestor(IntPtr hWnd, uint flags);

        [DllImport("user32.dll", SetLastError = true)]
        static extern bool SetProcessDpiAwarenessContext(IntPtr context);

        [DllImport("shcore.dll")]
        static extern int SetProcessDpiAwareness(int awareness);

        [DllImport("user32.dll")]
        static extern bool SetProcessDPIAware();

        const uint MOUSEEVENTF_LEFTDOWN = 0x0002;
        const uint MOUSEEVENTF_LEFTUP = 0x0004;
        const uint INPUT_MOUSE = 0;
        const uint INPUT_KEYBOARD = 1;
        const uint KEYEVENTF_KEYUP = 0x0002;
        const ushort VK_SPACE = 0x20;
        const int VK_LBUTTON = 0x01;
        const uint WM_MOUSEMOVE = 0x0200;
        const uint WM_LBUTTONDOWN = 0x0201;
        const uint WM_LBUTTONUP = 0x0202;
        const int MK_LBUTTON = 0x0001;
        const uint GA_ROOT = 2;

        static bool SendKey(ushort virtualKey)
        {
            INPUT[] sequence = new INPUT[2];
            sequence[0].type = INPUT_KEYBOARD;
            sequence[0].u.ki.wVk = virtualKey;
            sequence[0].u.ki.dwFlags = 0;
            sequence[1].type = INPUT_KEYBOARD;
            sequence[1].u.ki.wVk = virtualKey;
            sequence[1].u.ki.dwFlags = KEYEVENTF_KEYUP;
            return SendInput(2, sequence, Marshal.SizeOf(typeof(INPUT))) == 2;
        }

        static bool SendMouseClick()
        {
            INPUT[] sequence = new INPUT[2];
            sequence[0].type = INPUT_MOUSE;
            sequence[0].u.mi.dwFlags = MOUSEEVENTF_LEFTDOWN;
            sequence[1].type = INPUT_MOUSE;
            sequence[1].u.mi.dwFlags = MOUSEEVENTF_LEFTUP;
            return SendInput(2, sequence, Marshal.SizeOf(typeof(INPUT))) == 2;
        }

        static bool SameElement(AutomationElement a, AutomationElement b)
        {
            try
            {
                int[] idA = a.GetRuntimeId();
                int[] idB = b.GetRuntimeId();
                if (idA != null && idB != null && idA.Length > 0 && idA.Length == idB.Length)
                {
                    bool same = true;
                    for (int i = 0; i < idA.Length; i++)
                    {
                        if (idA[i] != idB[i]) { same = false; break; }
                    }
                    if (same) return true;
                }
            }
            catch (Exception) { }

            try
            {
                if (!string.Equals(a.Current.Name, b.Current.Name, StringComparison.Ordinal)) return false;
                System.Windows.Rect ra = a.Current.BoundingRectangle;
                System.Windows.Rect rb = b.Current.BoundingRectangle;
                return Math.Abs(ra.Left - rb.Left) <= 2 && Math.Abs(ra.Top - rb.Top) <= 2 &&
                       Math.Abs(ra.Width - rb.Width) <= 2 && Math.Abs(ra.Height - rb.Height) <= 2;
            }
            catch (Exception)
            {
                return false;
            }
        }

        static void EnableDpiAwareness()
        {
            // UIA reports physical pixels; without this the pointer lands elsewhere
            // on any scaled display.
            try
            {
                if (SetProcessDpiAwarenessContext(new IntPtr(-4))) return;
            }
            catch (EntryPointNotFoundException) { }
            catch (DllNotFoundException) { }
            try
            {
                if (SetProcessDpiAwareness(2) == 0) return;
            }
            catch (Exception) { }
            try { SetProcessDPIAware(); }
            catch (Exception) { }
        }

        static double IdleMs()
        {
            LASTINPUTINFO info = new LASTINPUTINFO();
            info.cbSize = (uint)Marshal.SizeOf(typeof(LASTINPUTINFO));
            if (!GetLastInputInfo(ref info)) return double.MaxValue;
            uint elapsed = GetTickCount() - info.dwTime;  // wraps safely
            return elapsed;
        }

        static int UserIdleGuardMs()
        {
            string raw = Environment.GetEnvironmentVariable("AUTOCLICK_IDLE_GUARD_MS");
            int parsed;
            if (!string.IsNullOrEmpty(raw) && int.TryParse(raw, out parsed) && parsed >= 0)
            {
                return parsed;
            }
            return DefaultUserIdleGuardMs;
        }

        static bool IsLeftButtonDown()
        {
            return (GetAsyncKeyState(VK_LBUTTON) & 0x8000) != 0;
        }

        // ------------------------------------------------------------------ //
        // Matching
        // ------------------------------------------------------------------ //

        static string NormalizeLabel(string raw)
        {
            if (raw == null) return string.Empty;
            string text = raw.Replace("&", string.Empty).Replace("\u2026", "...");
            text = Regex.Replace(text, @"\s+", " ").Trim();
            text = Regex.Replace(text, @"[\s.]+$", string.Empty);
            return text.ToLowerInvariant();
        }

        static Dictionary<string, string> BuildTargetMap()
        {
            Dictionary<string, string> map = new Dictionary<string, string>(StringComparer.Ordinal);
            foreach (string label in TargetPriority)
            {
                string key = NormalizeLabel(label);
                if (!map.ContainsKey(key)) map.Add(key, label);
            }
            return map;
        }

        static bool MatchesTarget(string rawName, out string canonical)
        {
            canonical = null;
            if (string.IsNullOrWhiteSpace(rawName)) return false;
            string normalized = NormalizeLabel(rawName);
            if (normalized.Length == 0 || normalized.Length > MaxLabelLength) return false;
            return TargetMap.TryGetValue(normalized, out canonical);
        }

        // ------------------------------------------------------------------ //
        // Logging
        // ------------------------------------------------------------------ //

        static void Log(string message)
        {
            string line = string.Format("[{0:yyyy-MM-dd HH:mm:ss}] {1}", DateTime.Now, message);
            try
            {
                File.AppendAllText(_logFile, line + Environment.NewLine);
            }
            catch (Exception) { }
            try
            {
                Console.WriteLine(line);
            }
            catch (Exception) { }
        }

        static void LogDebug(string message)
        {
            if (_debug) Log("  [debug] " + message);
        }

        // ------------------------------------------------------------------ //
        // Main
        // ------------------------------------------------------------------ //

        static int Main(string[] args)
        {
            bool dryRun = HasFlag(args, "--dry-run") ||
                          string.Equals(Environment.GetEnvironmentVariable("AUTOCLICK_DRY_RUN"), "1", StringComparison.Ordinal);
            bool selfTest = HasFlag(args, "--self-test");
            bool once = HasFlag(args, "--once");
            bool allowMouse = HasFlag(args, "--allow-mouse-fallback");
            _debug = HasFlag(args, "--debug") ||
                     string.Equals(Environment.GetEnvironmentVariable("AUTOCLICK_DEBUG"), "1", StringComparison.Ordinal);
            _allowKeyboard = !HasFlag(args, "--no-keyboard") &&
                             !string.Equals(Environment.GetEnvironmentVariable("AUTOCLICK_NO_KEYBOARD"), "1", StringComparison.Ordinal);
            _allowPostClick = !HasFlag(args, "--no-post-click");
            _forcePostClick = HasFlag(args, "--force-posted-click");
            _probeFocus = HasFlag(args, "--probe-focus");

            string tempPath = Path.GetTempPath();
            _logFile = Path.Combine(tempPath, "auto_click_decisions.log");
            _pidFile = Path.Combine(tempPath, "auto_click_decisions.pid");
            _heartbeatFile = Path.Combine(tempPath, "auto_click_decisions.heartbeat");

            if (selfTest) return SelfTest() ? 0 : 1;

            EnableDpiAwareness();

            bool createdNew;
            using (Mutex mutex = new Mutex(true, @"Local\AutoClickDecisionsNative", out createdNew))
            {
                if (!createdNew)
                {
                    Log("Another AutoClickDecisionsNative instance is already running; exiting.");
                    return 0;
                }

                try
                {
                    File.WriteAllText(_pidFile, Process.GetCurrentProcess().Id.ToString());
                }
                catch (Exception) { }

                Log("=================================================");
                Log(string.Format(
                    "Auto-clicker starting ({0}) PID {1}",
                    dryRun ? "DRY-RUN" : "LIVE",
                    Process.GetCurrentProcess().Id));
                Log("Exact targets: " + string.Join(", ", TargetPriority));
                Log(string.Format(
                    "Policy: foreground window only, idle>{0}ms, cooldown {1}ms, {2} attempts then {3}s blacklist.",
                    UserIdleGuardMs(), SameElementCooldownMs, MaxAttempts, BlacklistMs / 1000));
                if (allowMouse)
                {
                    Log("WARNING: --allow-mouse-fallback is on; cursor may move when no UIA pattern exists.");
                }

                while (true)
                {
                    try
                    {
                        ScanAndClick(dryRun, allowMouse);
                        WriteHeartbeat();
                    }
                    catch (Exception ex)
                    {
                        Log("Scan error: " + ex.GetType().Name + ": " + ex.Message);
                    }
                    if (once) break;
                    Thread.Sleep(PollIntervalMs);
                }
            }
            return 0;
        }

        static bool HasFlag(string[] args, string flag)
        {
            if (args == null) return false;
            for (int i = 0; i < args.Length; i++)
            {
                if (string.Equals(args[i], flag, StringComparison.OrdinalIgnoreCase)) return true;
            }
            return false;
        }

        // ------------------------------------------------------------------ //
        // Scan
        // ------------------------------------------------------------------ //

        static void ScanAndClick(bool dryRun, bool allowMouse)
        {
            DateTime now = DateTime.UtcNow;
            ExpireState(now);

            if (IsLeftButtonDown())
            {
                LogDebug("left mouse button is down; skipping.");
                return;
            }
            if (IdleMs() < UserIdleGuardMs())
            {
                LogDebug(string.Format("user active ({0}ms idle); skipping.", (int)IdleMs()));
                return;
            }

            IntPtr foreground = GetForegroundWindow();
            if (foreground == IntPtr.Zero) return;

            uint windowPid;
            GetWindowThreadProcessId(foreground, out windowPid);
            if (windowPid == (uint)Process.GetCurrentProcess().Id) return;

            AutomationElement root = null;
            string windowTitle;
            try
            {
                root = AutomationElement.FromHandle(foreground);
                windowTitle = root.Current.Name;
            }
            catch (Exception)
            {
                return;
            }
            if (root == null) return;

            List<Hit> hits = FindHits(root, foreground, windowTitle, now);
            LogDebug(string.Format(
                "foreground='{0}' pid={1} hits={2}", windowTitle, windowPid, hits.Count));
            if (hits.Count == 0) return;

            hits.Sort(CompareHits);
            Hit chosen = hits[0];

            AttemptState state;
            if (!States.TryGetValue(chosen.Key, out state))
            {
                state = new AttemptState();
                state.FirstSeen = now;
                States[chosen.Key] = state;
            }
            state.LastSeen = now;

            int attempt = state.Attempts + 1;
            int cx = (int)(chosen.Rect.Left + chosen.Rect.Width / 2.0);
            int cy = (int)(chosen.Rect.Top + chosen.Rect.Height / 2.0);

            if (dryRun)
            {
                Log(string.Format(
                    "[DRY-RUN] would click '{0}' in '{1}' at ({2}, {3}) attempt {4}",
                    chosen.Canonical, windowTitle, cx, cy, attempt));
                state.Attempts = attempt;
                state.LastClick = now;
            }
            else
            {
                Log(string.Format(
                    "Clicking '{0}' in '{1}' at ({2}, {3}) attempt {4}",
                    chosen.Canonical, windowTitle, cx, cy, attempt));

                string method;
                bool clicked = InvokeElement(chosen, foreground, allowMouse, out method);
                if (clicked)
                {
                    state.Attempts = attempt;
                    state.LastClick = now;
                    Log(string.Format("  -> {0}", method));
                }
                else
                {
                    // No UIA pattern worked.  Count it, so the element gets
                    // blacklisted instead of being retried forever.
                    state.Attempts = attempt;
                    state.LastClick = now;
                    Log("  -> no action pattern available; not clicking.");
                }
            }

            if (state.Attempts >= MaxAttempts)
            {
                Blacklist[chosen.Key] = now.AddMilliseconds(BlacklistMs);
                Log(string.Format(
                    "'{0}' is still present after {1} clicks; ignoring it for {2}s.",
                    chosen.Canonical, state.Attempts, BlacklistMs / 1000));
                States.Remove(chosen.Key);
            }
        }

        static int CompareHits(Hit a, Hit b)
        {
            int byPriority = a.Priority.CompareTo(b.Priority);
            if (byPriority != 0) return byPriority;
            double areaA = a.Rect.Width * a.Rect.Height;
            double areaB = b.Rect.Width * b.Rect.Height;
            return areaB.CompareTo(areaA);
        }

        static List<Hit> FindHits(AutomationElement root, IntPtr window, string windowTitle, DateTime now)
        {
            List<Hit> hits = new List<Hit>();

            CacheRequest cache = new CacheRequest();
            cache.Add(AutomationElement.NameProperty);
            cache.Add(AutomationElement.IsEnabledProperty);
            cache.Add(AutomationElement.IsOffscreenProperty);
            cache.Add(AutomationElement.BoundingRectangleProperty);
            cache.TreeFilter = Automation.ControlViewCondition;

            Condition condition = new PropertyCondition(
                AutomationElement.ControlTypeProperty, ControlType.Button);

            AutomationElementCollection buttons;
            Stopwatch watch = Stopwatch.StartNew();
            using (cache.Activate())
            {
                try
                {
                    buttons = root.FindAll(TreeScope.Descendants, condition);
                }
                catch (Exception)
                {
                    return hits;
                }
            }
            watch.Stop();
            if (watch.ElapsedMilliseconds > 2000 && (now - _lastSlowScanNotice).TotalSeconds > 60)
            {
                _lastSlowScanNotice = now;
                Log(string.Format(
                    "Notice: UI Automation scan of '{0}' took {1}ms.", windowTitle, watch.ElapsedMilliseconds));
            }
            if (buttons == null || buttons.Count == 0)
            {
                LogDebug("no Button elements in the foreground window.");
                return hits;
            }
            LogDebug(string.Format("foreground window exposes {0} Button elements.", buttons.Count));

            foreach (AutomationElement button in buttons)
            {
                try
                {
                    if (button == null) continue;

                    string rawName = button.Cached.Name;
                    string canonical;
                    if (!MatchesTarget(rawName, out canonical))
                    {
                        LogDebug("skip (label not an exact target): '" + Shorten(rawName) + "'");
                        continue;
                    }

                    if (!button.Cached.IsEnabled)
                    {
                        LogDebug("skip (disabled): '" + canonical + "'");
                        continue;
                    }
                    if (button.Cached.IsOffscreen)
                    {
                        LogDebug("skip (offscreen): '" + canonical + "'");
                        continue;
                    }

                    System.Windows.Rect rect = button.Cached.BoundingRectangle;
                    if (rect.IsEmpty)
                    {
                        LogDebug("skip (empty rect): '" + canonical + "'");
                        continue;
                    }
                    if (rect.Width < 20 || rect.Height < 12 || rect.Height > 80)
                    {
                        LogDebug(string.Format("skip (size {0}x{1}): '{2}'",
                            (int)rect.Width, (int)rect.Height, canonical));
                        continue;
                    }

                    double aspect = rect.Width / rect.Height;
                    if (aspect < MinAspect || aspect > MaxAspect)
                    {
                        LogDebug(string.Format("skip (aspect {0:F1}): '{1}'", aspect, canonical));
                        continue;
                    }

                    string key = BuildKey(canonical, button, window);
                    DateTime until;
                    if (Blacklist.TryGetValue(key, out until) && now < until)
                    {
                        LogDebug("skip (blacklisted): '" + canonical + "'");
                        continue;
                    }

                    AttemptState state;
                    if (States.TryGetValue(key, out state))
                    {
                        if (state.Attempts >= MaxAttempts) continue;
                        if ((now - state.LastClick).TotalMilliseconds < SameElementCooldownMs)
                        {
                            LogDebug("skip (cooldown): '" + canonical + "'");
                            continue;
                        }
                    }

                    Hit hit = new Hit();
                    hit.Canonical = canonical;
                    hit.Key = key;
                    hit.Priority = PriorityOf(canonical);
                    hit.Rect = rect;
                    hit.WindowTitle = windowTitle;
                    hit.Window = window;
                    hit.Element = button;
                    hits.Add(hit);
                }
                catch (ElementNotAvailableException) { }
                catch (InvalidOperationException) { }
                catch (Exception) { }
            }
            return hits;
        }

        static int PriorityOf(string canonical)
        {
            for (int i = 0; i < TargetPriority.Length; i++)
            {
                if (string.Equals(TargetPriority[i], canonical, StringComparison.Ordinal)) return i;
            }
            return TargetPriority.Length;
        }

        static string BuildKey(string canonical, AutomationElement element, IntPtr window)
        {
            string id = string.Empty;
            try
            {
                int[] runtimeId = element.GetRuntimeId();
                if (runtimeId != null && runtimeId.Length > 0)
                {
                    id = string.Join(".", runtimeId.Select(v => v.ToString()).ToArray());
                }
            }
            catch (Exception) { }
            if (id.Length == 0)
            {
                id = string.Format("{0}:{1}", canonical, element.Current.BoundingRectangle);
            }
            return canonical + "|" + id + "|" + window.ToInt64().ToString();
        }

        // ------------------------------------------------------------------ //
        // Action
        // ------------------------------------------------------------------ //

        static bool InvokeElement(Hit hit, IntPtr expectedForeground, bool allowMouse, out string method)
        {
            method = "none";
            AutomationElement element = hit.Element;
            if (element == null)
            {
                method = "element vanished";
                return false;
            }

            object pattern;

            if (_forcePostClick)
            {
                // Diagnostics: prove the posted-click path works on an app whose
                // UIA patterns do work, without moving the cursor.
                string forced;
                if (TryPostedClick(hit, expectedForeground, out forced))
                {
                    method = forced + " [forced]";
                    return true;
                }
                return false;
            }

            // 1. InvokePattern: the direct, cursor-free action.
            try
            {
                if (element.TryGetCurrentPattern(InvokePattern.Pattern, out pattern))
                {
                    ((InvokePattern)pattern).Invoke();
                    method = "InvokePattern";
                    return true;
                }
            }
            catch (Exception) { }

            // 2. Toggle / select, for checkboxes and list items.
            try
            {
                if (element.TryGetCurrentPattern(TogglePattern.Pattern, out pattern))
                {
                    ((TogglePattern)pattern).Toggle();
                    method = "TogglePattern";
                    return true;
                }
            }
            catch (Exception) { }

            try
            {
                if (element.TryGetCurrentPattern(SelectionItemPattern.Pattern, out pattern))
                {
                    ((SelectionItemPattern)pattern).Select();
                    method = "SelectionItemPattern";
                    return true;
                }
            }
            catch (Exception) { }

            // 3. No action pattern (Chromium/Electron buttons land here).  Post a
            //    click straight to the element's HWND: same effect, cursor stays put.
            string postedMethod;
            if (TryPostedClick(hit, expectedForeground, out postedMethod))
            {
                method = postedMethod;
                return true;
            }

            // 4. Focus the element and activate it with Space (verified first).
            string keyboardMethod;
            if (TryKeyboardActivate(element, expectedForeground, out keyboardMethod))
            {
                method = keyboardMethod;
                return true;
            }

            if (!allowMouse) return false;

            // Explicit opt-in only: this moves the pointer, so re-check that the
            // expected window is still in front.
            if (GetForegroundWindow() != expectedForeground) return false;
            if (IsLeftButtonDown()) return false;

            POINT before;
            if (!GetCursorPos(out before)) return false;

            int x = (int)(hit.Rect.Left + hit.Rect.Width / 2.0);
            int y = (int)(hit.Rect.Top + hit.Rect.Height / 2.0);
            if (!SetCursorPos(x, y)) return false;
            Thread.Sleep(80);
            SendMouseClick();
            Thread.Sleep(100);
            SetCursorPos(before.X, before.Y);

            method = "mouse fallback (cursor restored)";
            return true;
        }

        static bool TryPostedClick(Hit hit, IntPtr expectedForeground, out string method)
        {
            method = "none";
            if (!_allowPostClick) return false;

            if (GetForegroundWindow() != expectedForeground) return false;
            if (IsLeftButtonDown()) return false;

            int cx = (int)(hit.Rect.Left + hit.Rect.Width / 2.0);
            int cy = (int)(hit.Rect.Top + hit.Rect.Height / 2.0);

            POINT screen;
            screen.X = cx;
            screen.Y = cy;

            IntPtr target = WindowFromPoint(screen);
            if (target == IntPtr.Zero) return false;

            // Never post into a different top-level window than the scanned one.
            if (GetAncestor(target, GA_ROOT) != expectedForeground) return false;

            POINT client;
            client.X = cx;
            client.Y = cy;
            if (!ScreenToClient(target, ref client)) return false;
            if (client.X < 0 || client.Y < 0 || client.X > 0x7FFF || client.Y > 0x7FFF) return false;

            IntPtr position = (IntPtr)((client.Y << 16) | (client.X & 0xFFFF));

            PostMessage(target, WM_MOUSEMOVE, IntPtr.Zero, position);
            Thread.Sleep(20);
            PostMessage(target, WM_LBUTTONDOWN, (IntPtr)MK_LBUTTON, position);
            Thread.Sleep(40);
            PostMessage(target, WM_LBUTTONUP, IntPtr.Zero, position);
            Thread.Sleep(40);

            method = "posted click (cursor untouched)";
            return true;
        }

        static bool TryKeyboardActivate(AutomationElement element, IntPtr expectedForeground, out string method)
        {
            method = "none";
            if (!_allowKeyboard) return false;

            try
            {
                element.SetFocus();
            }
            catch (Exception)
            {
                LogDebug("SetFocus failed.");
                return false;
            }

            // Only press a key if focus provably landed on this very element;
            // otherwise a stray Space could type into an editor.
            AutomationElement focused = null;
            try
            {
                focused = AutomationElement.FocusedElement;
            }
            catch (Exception) { }

            if (focused == null)
            {
                LogDebug("focus verification: no focused element.");
                return false;
            }
            if (!SameElement(focused, element))
            {
                LogDebug("focus verification: focus landed on '" + SafeName(focused) + "', not the target.");
                return false;
            }
            if (GetForegroundWindow() != expectedForeground)
            {
                LogDebug("focus verification: foreground changed.");
                return false;
            }
            if (IsLeftButtonDown()) return false;

            if (_probeFocus)
            {
                Log("PROBE: focus verified on '" + SafeName(element) +
                    "'; Space not sent (--probe-focus).");
                return false;
            }

            if (!SendKey(VK_SPACE))
            {
                LogDebug("Space key dispatch failed.");
                return false;
            }
            method = "focus + Space (cursor untouched)";
            return true;
        }

        static string SafeName(AutomationElement element)
        {
            try
            {
                return element.Current.Name;
            }
            catch (Exception)
            {
                return "?";
            }
        }

        static void WriteHeartbeat()
        {
            DateTime now = DateTime.UtcNow;
            if ((now - _lastHeartbeat).TotalMilliseconds < HeartbeatMs) return;
            _lastHeartbeat = now;
            try
            {
                File.WriteAllText(
                    _heartbeatFile,
                    ((long)(now - new DateTime(1970, 1, 1, 0, 0, 0, DateTimeKind.Utc)).TotalSeconds)
                        .ToString());
            }
            catch (Exception) { }
        }

        static void ExpireState(DateTime now)
        {
            List<string> stale = new List<string>();
            foreach (KeyValuePair<string, AttemptState> pair in States)
            {
                if ((now - pair.Value.LastSeen).TotalSeconds > 300) stale.Add(pair.Key);
            }
            foreach (string key in stale) States.Remove(key);

            List<string> expired = new List<string>();
            foreach (KeyValuePair<string, DateTime> pair in Blacklist)
            {
                if (now >= pair.Value) expired.Add(pair.Key);
            }
            foreach (string key in expired) Blacklist.Remove(key);
        }

        // ------------------------------------------------------------------ //
        // Offline verification
        // ------------------------------------------------------------------ //

        static bool SelfTest()
        {
            int failures = 0;

            Action<string, bool> check = delegate(string name, bool ok)
            {
                Console.WriteLine("  " + (ok ? "PASS" : "FAIL") + "  " + name);
                if (!ok) failures++;
            };

            Console.WriteLine("Self-test: normalisation");
            check("whitespace collapsed", NormalizeLabel("  Accept   All  ") == "accept all");
            check("accelerator stripped", NormalizeLabel("&Accept") == "accept");
            check("ellipsis stripped", NormalizeLabel("Accept...") == "accept");
            check("unicode ellipsis stripped", NormalizeLabel("Accept\u2026") == "accept");
            check("case folded", NormalizeLabel("ACCEPT ALL") == "accept all");

            Console.WriteLine("Self-test: matcher");
            string ignoredCanonical;

            // The exact labels observed being mis-clicked in the field log.
            string[] falsePositives = new string[]
            {
                "Acceptance failed on the merged main state, which could mean a flaky test or something broken by other agents",
                "Acceptance Rate",
                "Acceptance",
                "Allowed",
                "Allow connections from other machines and vscode.dev/agents",
                "Allow network egress",
                "Allow bypass permissions mode",
                "Keep all changes saved",
                "Proceed to checkout"
            };
            foreach (string name in falsePositives)
            {
                bool matched = MatchesTarget(name, out ignoredCanonical);
                check("rejects '" + Shorten(name) + "'", !matched);
            }

            string[] truePositives = new string[]
            {
                "Accept All",
                "Accept All 2",
                "Accept Changes",
                "Accept edits",
                "Accept",
                "  Accept   ",
                "&Accept",
                "Accept\u2026",
                "Keep All",
                "Proceed",
                "Yes, Don't Ask",
                "Yes Don't Ask",
                "Allow Always",
                "Always Allow",
                "Always Run",
                "Allow Once",
                "Allow"
            };
            foreach (string name in truePositives)
            {
                string canonical;
                bool matched = MatchesTarget(name, out canonical);
                check("accepts '" + Shorten(name) + "'", matched && canonical != null);
            }

            check("length guard rejects a padded exact label",
                  !MatchesTarget("Accept" + new string(' ', 0) + " All" + new string('x', 40), out ignoredCanonical));

            Console.WriteLine();
            if (failures > 0)
            {
                Console.WriteLine(string.Format("FAILED ({0} checks).", failures));
                return false;
            }
            Console.WriteLine("All self-tests passed.");
            return true;
        }

        static string Shorten(string text)
        {
            if (text == null) return string.Empty;
            if (text.Length <= 46) return text;
            return text.Substring(0, 43) + "...";
        }
    }
}
