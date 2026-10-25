using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Runtime.InteropServices;
using System.Threading;
using System.Windows.Automation;

namespace AutoClickDecisions
{
    class Program
    {
        // Win32 API for cursor restoration & mouse events if needed
        [DllImport("user32.dll")]
        static extern bool GetCursorPos(out POINT lpPoint);

        [DllImport("user32.dll")]
        static extern bool SetCursorPos(int X, int Y);

        [DllImport("user32.dll")]
        static extern void mouse_event(uint dwFlags, uint dx, uint dy, uint dwData, UIntPtr dwExtraInfo);

        const uint MOUSEEVENTF_LEFTDOWN = 0x0002;
        const uint MOUSEEVENTF_LEFTUP = 0x0004;

        [StructLayout(LayoutKind.Sequential)]
        public struct POINT
        {
            public int X;
            public int Y;
        }

        // Target button names in order of priority
        static readonly string[] TargetButtonNames = new string[]
        {
            "Keep All",
            "Keep all",
            "Accept All",
            "Accept all",
            "Accept Changes",
            "Accept changes",
            "Accept",
            "Allow Always",
            "Allow always",
            "Always Allow",
            "Always Run",
            "Allow Once",
            "Allow once",
            "Allow",
            "Yes, Don't Ask",
            "Yes Don't Ask",
            "Proceed",
            "Yes",
            "OK"
        };

        static readonly HashSet<string> TargetSet = new HashSet<string>(TargetButtonNames, StringComparer.OrdinalIgnoreCase);

        static string LogFile;
        static string PidFile;

        static void Main(string[] args)
        {
            string tempPath = Path.GetTempPath();
            LogFile = Path.Combine(tempPath, "auto_click_decisions.log");
            PidFile = Path.Combine(tempPath, "auto_click_decisions.pid");

            Log("=================================================");
            Log(string.Format("Native Windows UI Automation Clicker Service starting... PID: {0}", Process.GetCurrentProcess().Id));
            Log("Target Buttons: " + string.Join(", ", TargetButtonNames));

            try
            {
                File.WriteAllText(PidFile, Process.GetCurrentProcess().Id.ToString());
            }
            catch (Exception ex)
            {
                Log("Warning: Could not write PID file: " + ex.Message);
            }

            // Register event listener for UI Automation window opened events to respond instantaneously
            try
            {
                Automation.AddAutomationEventHandler(
                    WindowPattern.WindowOpenedEvent,
                    AutomationElement.RootElement,
                    TreeScope.Children,
                    OnWindowOpened
                );
                Log("Registered UI Automation WindowOpened event listener for instant response.");
            }
            catch (Exception ex)
            {
                Log("Notice: Could not register window event listener (polling loop fallback active): " + ex.Message);
            }

            // Main Polling Loop
            int scanIntervalMs = 500;
            while (true)
            {
                try
                {
                    ScanAndClickButtons();
                }
                catch (Exception ex)
                {
                    Log("Scan error: " + ex.Message);
                }

                Thread.Sleep(scanIntervalMs);
            }
        }

        static void OnWindowOpened(object src, AutomationEventArgs e)
        {
            try
            {
                ScanAndClickButtons();
            }
            catch (Exception ex)
            {
                Log("Event handler error: " + ex.Message);
            }
        }

        static void ScanAndClickButtons()
        {
            AutomationElement root = AutomationElement.RootElement;
            if (root == null) return;

            // Search for all Button controls in the UI element tree
            PropertyCondition buttonCondition = new PropertyCondition(AutomationElement.ControlTypeProperty, ControlType.Button);
            AutomationElementCollection buttons = root.FindAll(TreeScope.Subtree, buttonCondition);

            if (buttons == null || buttons.Count == 0) return;

            foreach (AutomationElement button in buttons)
            {
                try
                {
                    if (button == null) continue;

                    string name = button.Current.Name;
                    if (string.IsNullOrWhiteSpace(name)) continue;

                    string trimmedName = name.Trim();

                    // Check if button text matches any of our target action button names
                    if (IsMatchingTarget(trimmedName))
                    {
                        if (button.Current.IsEnabled && !button.Current.IsOffscreen)
                        {
                            Log(string.Format("[DETECTED] Button '{0}' found in window '{1}'!", trimmedName, GetParentWindowName(button)));
                            ClickButton(button, trimmedName);
                            Thread.Sleep(300); // Short pause after clicking to allow UI to settle
                        }
                    }
                }
                catch (ElementNotAvailableException)
                {
                    // Element disappeared or closed
                }
                catch (Exception ex)
                {
                    Log("Button processing error: " + ex.Message);
                }
            }
        }

        static bool IsMatchingTarget(string buttonName)
        {
            if (TargetSet.Contains(buttonName)) return true;

            foreach (var target in TargetButtonNames)
            {
                if (buttonName.Equals(target, StringComparison.OrdinalIgnoreCase))
                    return true;
                if (buttonName.StartsWith(target, StringComparison.OrdinalIgnoreCase))
                    return true;
            }

            return false;
        }

        static string GetParentWindowName(AutomationElement element)
        {
            try
            {
                AutomationElement current = element;
                while (current != null)
                {
                    if (current.Current.ControlType == ControlType.Window)
                    {
                        return current.Current.Name;
                    }
                    current = TreeWalker.ControlViewWalker.GetParent(current);
                }
            }
            catch { }
            return "Unknown Window";
        }

        static void ClickButton(AutomationElement button, string buttonName)
        {
            bool invoked = false;

            // Method 1: UI Automation InvokePattern (Direct, Instant, No Cursor Move)
            try
            {
                object patternObj;
                if (button.TryGetCurrentPattern(InvokePattern.Pattern, out patternObj))
                {
                    InvokePattern invokePattern = patternObj as InvokePattern;
                    if (invokePattern != null)
                    {
                        invokePattern.Invoke();
                        Log(string.Format("[CLICKED via InvokePattern] Successfully invoked '{0}'.", buttonName));
                        invoked = true;
                    }
                }
            }
            catch (Exception ex)
            {
                Log(string.Format("InvokePattern failed for '{0}': {1}", buttonName, ex.Message));
            }

            // Method 2: Physical Mouse Click fallback if InvokePattern wasn't supported
            if (!invoked)
            {
                try
                {
                    System.Windows.Rect rect = button.Current.BoundingRectangle;
                    if (!rect.IsEmpty && rect.Width > 0 && rect.Height > 0)
                    {
                        int clickX = (int)(rect.Left + rect.Width / 2);
                        int clickY = (int)(rect.Top + rect.Height / 2);

                        POINT origPos;
                        GetCursorPos(out origPos);

                        SetCursorPos(clickX, clickY);
                        mouse_event(MOUSEEVENTF_LEFTDOWN, 0, 0, 0, UIntPtr.Zero);
                        mouse_event(MOUSEEVENTF_LEFTUP, 0, 0, 0, UIntPtr.Zero);

                        SetCursorPos(origPos.X, origPos.Y);
                        Log(string.Format("[CLICKED via MouseFallback] Clicked '{0}' at ({1}, {2}) and restored cursor.", buttonName, clickX, clickY));
                    }
                }
                catch (Exception ex)
                {
                    Log(string.Format("MouseFallback failed for '{0}': {1}", buttonName, ex.Message));
                }
            }
        }

        static void Log(string message)
        {
            string logLine = string.Format("[{0:yyyy-MM-dd HH:mm:ss}] {1}", DateTime.Now, message);
            Console.WriteLine(logLine);
            try
            {
                File.AppendAllText(LogFile, logLine + Environment.NewLine);
            }
            catch { }
        }
    }
}
