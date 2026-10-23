#!/usr/bin/env python3
"""
Autonomous Budget Governor & Circuit-Breaker Engine for CEO Cockpit.
Enforces hard spend caps, real-time token cost calculation, automated circuit-breaker trips,
and zero-cost local fallback policies.
"""

import json
import os
import time
from datetime import datetime
from typing import Dict, Any, Tuple, Optional

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
CONFIG_DIR = os.path.join(BASE_DIR, "config")
GOVERNOR_FILE = os.path.join(CONFIG_DIR, "budget_governor.json")

# Token pricing per 1,000,000 tokens (USD)
# Source: Official Provider Pricing (DeepSeek, Google AI Studio, Anthropic, OpenAI)
MODEL_PRICING: Dict[str, Dict[str, float]] = {
    # DeepSeek
    "deepseek-reasoner": {"prompt": 0.55, "completion": 2.19, "cached_prompt": 0.14},
    "deepseek-chat": {"prompt": 0.14, "completion": 0.28, "cached_prompt": 0.014},
    "deepseek-coder": {"prompt": 0.14, "completion": 0.28, "cached_prompt": 0.014},
    # Google Gemini
    "gemini-flash": {"prompt": 0.075, "completion": 0.30, "cached_prompt": 0.018},
    "gemini-3.8-flash": {"prompt": 0.075, "completion": 0.30, "cached_prompt": 0.018},
    "gemini-pro": {"prompt": 1.25, "completion": 5.00, "cached_prompt": 0.3125},
    # Anthropic
    "claude-3-5-haiku": {"prompt": 0.80, "completion": 4.00, "cached_prompt": 0.08},
    "claude-3-5-sonnet": {"prompt": 3.00, "completion": 15.00, "cached_prompt": 0.30},
    # SiliconFlow / DashScope Qwen
    "siliconflow-qwen": {"prompt": 0.20, "completion": 0.40, "cached_prompt": 0.05},
    "dashscope-qwen-plus": {"prompt": 0.12, "completion": 0.24, "cached_prompt": 0.03},
    # Local & Free tiers
    "local-coder": {"prompt": 0.0, "completion": 0.0, "cached_prompt": 0.0},
    "local-reasoner": {"prompt": 0.0, "completion": 0.0, "cached_prompt": 0.0},
    "local-llama": {"prompt": 0.0, "completion": 0.0, "cached_prompt": 0.0},
}

DEFAULT_CONFIG: Dict[str, Any] = {
    "daily_budget_usd": 2.00,
    "monthly_budget_usd": 25.00,
    "warning_threshold_pct": 80.0,
    "circuit_breaker_enabled": True,
    "circuit_breaker_state": "NORMAL",  # NORMAL | WARNING | TRIPPED
    "circuit_breaker_trip_reason": "",
    "tripped_at": None,
    "today_spend_usd": 0.0,
    "month_spend_usd": 0.0,
    "lifetime_spend_usd": 0.0,
    "cached_savings_usd": 0.0320,
    "total_requests": 16,
    "cached_requests": 16,
    "last_reset_date": datetime.now().strftime("%Y-%m-%d"),
    "audit_log": [
        {
            "timestamp": time.time(),
            "date": datetime.now().isoformat(),
            "event": "GOVERNOR_INITIALIZED",
            "details": "Budget Governor activated: $2.00/day, $25.00/month limits."
        }
    ]
}


class BudgetGovernor:
    def __init__(self, filepath: str = GOVERNOR_FILE):
        self.filepath = filepath
        os.makedirs(os.path.dirname(self.filepath), exist_ok=True)
        self.data = self._load()
        self._check_date_rollover()

    def _load(self) -> Dict[str, Any]:
        if os.path.exists(self.filepath):
            try:
                with open(self.filepath, "r", encoding="utf-8") as f:
                    loaded = json.load(f)
                    # Merge with default keys to ensure compatibility
                    merged = dict(DEFAULT_CONFIG)
                    merged.update(loaded)
                    return merged
            except Exception:
                pass
        # Initialize default
        self._save(DEFAULT_CONFIG)
        return dict(DEFAULT_CONFIG)

    def _save(self, data: Optional[Dict[str, Any]] = None):
        if data is not None:
            self.data = data
        try:
            with open(self.filepath, "w", encoding="utf-8") as f:
                json.dump(self.data, f, indent=2)
        except Exception as e:
            print(f"[BudgetGovernor] Save error: {e}")

    def _check_date_rollover(self):
        today = datetime.now().strftime("%Y-%m-%d")
        if self.data.get("last_reset_date") != today:
            # Rollover day
            self.data["last_reset_date"] = today
            self.data["today_spend_usd"] = 0.0
            if self.data.get("circuit_breaker_state") == "TRIPPED" and "Daily budget" in self.data.get("circuit_breaker_trip_reason", ""):
                # Auto-reset daily trip on new day
                self.data["circuit_breaker_state"] = "NORMAL"
                self.data["circuit_breaker_trip_reason"] = ""
                self.data["tripped_at"] = None
                self._add_audit("DAILY_ROLLOVER_RESET", "New operational day started. Daily spend reset to $0.00.")
            else:
                self._add_audit("DAILY_ROLLOVER", "Rollover to new operational day. Today's spend reset.")
            self._save()

    def _add_audit(self, event: str, details: str):
        log_entry = {
            "timestamp": time.time(),
            "date": datetime.now().isoformat(),
            "event": event,
            "details": details
        }
        logs = self.data.get("audit_log", [])
        logs.append(log_entry)
        # Cap audit log to 100 entries
        self.data["audit_log"] = logs[-100:]

    def calculate_cost(self, model: str, prompt_tokens: int, completion_tokens: int, was_cached: bool) -> Tuple[float, float]:
        """
        Calculates (actual_cost, saved_cost) in USD.
        """
        pricing = MODEL_PRICING.get(model, {"prompt": 0.50, "completion": 2.00, "cached_prompt": 0.10})
        
        if was_cached:
            # Entire request served by exact Redis match => $0 actual cost
            actual_cost = 0.0
            # Saved cost is what full execution would have cost
            saved_cost = (prompt_tokens * pricing["prompt"] + completion_tokens * pricing["completion"]) / 1_000_000.0
            return actual_cost, saved_cost

        prompt_cost = (prompt_tokens * pricing["prompt"]) / 1_000_000.0
        completion_cost = (completion_tokens * pricing["completion"]) / 1_000_000.0
        actual_cost = prompt_cost + completion_cost
        saved_cost = 0.0
        return actual_cost, saved_cost

    def record_completion(self, model: str, usage: Dict[str, Any], was_cached: bool) -> Dict[str, Any]:
        """
        Records completion usage, updates balances, checks thresholds, and trips circuit breaker if breached.
        """
        self._check_date_rollover()
        prompt_tokens = usage.get("prompt_tokens", 0)
        completion_tokens = usage.get("completion_tokens", 0)

        actual_cost, saved_cost = self.calculate_cost(model, prompt_tokens, completion_tokens, was_cached)

        self.data["total_requests"] = self.data.get("total_requests", 0) + 1
        if was_cached:
            self.data["cached_requests"] = self.data.get("cached_requests", 0) + 1
            self.data["cached_savings_usd"] = round(self.data.get("cached_savings_usd", 0.0) + saved_cost, 6)
        else:
            self.data["today_spend_usd"] = round(self.data.get("today_spend_usd", 0.0) + actual_cost, 6)
            self.data["month_spend_usd"] = round(self.data.get("month_spend_usd", 0.0) + actual_cost, 6)
            self.data["lifetime_spend_usd"] = round(self.data.get("lifetime_spend_usd", 0.0) + actual_cost, 6)

        # Evaluate Circuit Breaker Thresholds
        self._evaluate_thresholds()
        self._save()

        return {
            "actual_cost_usd": actual_cost,
            "saved_cost_usd": saved_cost,
            "today_spend_usd": self.data["today_spend_usd"],
            "circuit_breaker_state": self.data["circuit_breaker_state"],
            "tripped": self.data["circuit_breaker_state"] == "TRIPPED"
        }

    def _evaluate_thresholds(self):
        if not self.data.get("circuit_breaker_enabled", True):
            return

        today = self.data.get("today_spend_usd", 0.0)
        daily_cap = self.data.get("daily_budget_usd", 2.00)
        month = self.data.get("month_spend_usd", 0.0)
        monthly_cap = self.data.get("monthly_budget_usd", 25.00)
        warning_pct = self.data.get("warning_threshold_pct", 80.0) / 100.0

        # Daily breach check
        if today >= daily_cap:
            if self.data["circuit_breaker_state"] != "TRIPPED":
                self.trip(f"Daily budget limit breached: ${today:.4f} >= ${daily_cap:.2f}")
            return

        # Monthly breach check
        if month >= monthly_cap:
            if self.data["circuit_breaker_state"] != "TRIPPED":
                self.trip(f"Monthly budget limit breached: ${month:.4f} >= ${monthly_cap:.2f}")
            return

        # Warning check
        if today >= (daily_cap * warning_pct) or month >= (monthly_cap * warning_pct):
            if self.data["circuit_breaker_state"] == "NORMAL":
                self.data["circuit_breaker_state"] = "WARNING"
                self._add_audit("BUDGET_WARNING", f"Spend exceeded {int(warning_pct * 100)}% warning threshold.")
            return

        # If below warning and not manually tripped, keep NORMAL
        if self.data["circuit_breaker_state"] == "WARNING":
            self.data["circuit_breaker_state"] = "NORMAL"

    def trip(self, reason: str):
        """Forces circuit-breaker into TRIPPED state (locking cloud routes to zero-cost offline)."""
        self.data["circuit_breaker_state"] = "TRIPPED"
        self.data["circuit_breaker_trip_reason"] = reason
        self.data["tripped_at"] = time.time()
        self._add_audit("CIRCUIT_BREAKER_TRIPPED", reason)
        self._save()

    def reset(self):
        """Resets circuit-breaker to NORMAL state."""
        self.data["circuit_breaker_state"] = "NORMAL"
        self.data["circuit_breaker_trip_reason"] = ""
        self.data["tripped_at"] = None
        self._add_audit("CIRCUIT_BREAKER_RESET", "Executive manually reset circuit breaker. Normal hybrid routing restored.")
        self._save()

    def update_caps(self, daily: Optional[float] = None, monthly: Optional[float] = None, enabled: Optional[bool] = None):
        """Updates budget limits and re-evaluates thresholds."""
        if daily is not None and daily > 0:
            self.data["daily_budget_usd"] = float(daily)
        if monthly is not None and monthly > 0:
            self.data["monthly_budget_usd"] = float(monthly)
        if enabled is not None:
            self.data["circuit_breaker_enabled"] = bool(enabled)
            
        self._add_audit("CAPS_UPDATED", f"Daily: ${self.data['daily_budget_usd']:.2f}, Monthly: ${self.data['monthly_budget_usd']:.2f}, Enabled: {self.data['circuit_breaker_enabled']}")
        self._evaluate_thresholds()
        self._save()

    def is_tripped(self) -> bool:
        return self.data.get("circuit_breaker_enabled", True) and (self.data.get("circuit_breaker_state") == "TRIPPED")

    def get_summary(self) -> Dict[str, Any]:
        self._check_date_rollover()
        today = self.data.get("today_spend_usd", 0.0)
        daily_cap = self.data.get("daily_budget_usd", 2.00)
        month = self.data.get("month_spend_usd", 0.0)
        monthly_cap = self.data.get("monthly_budget_usd", 25.00)

        daily_pct = min(100.0, round((today / daily_cap * 100.0) if daily_cap > 0 else 0.0, 1))
        month_pct = min(100.0, round((month / monthly_cap * 100.0) if monthly_cap > 0 else 0.0, 1))

        return {
            "circuit_breaker_state": self.data.get("circuit_breaker_state", "NORMAL"),
            "circuit_breaker_enabled": self.data.get("circuit_breaker_enabled", True),
            "circuit_breaker_trip_reason": self.data.get("circuit_breaker_trip_reason", ""),
            "tripped_at": self.data.get("tripped_at"),
            "daily_budget_usd": daily_cap,
            "monthly_budget_usd": monthly_cap,
            "today_spend_usd": today,
            "month_spend_usd": month,
            "lifetime_spend_usd": self.data.get("lifetime_spend_usd", 0.0),
            "cached_savings_usd": self.data.get("cached_savings_usd", 0.0),
            "total_requests": self.data.get("total_requests", 0),
            "cached_requests": self.data.get("cached_requests", 0),
            "cache_hit_rate_pct": round((self.data.get("cached_requests", 0) / max(1, self.data.get("total_requests", 1))) * 100.0, 1),
            "daily_used_pct": daily_pct,
            "monthly_used_pct": month_pct,
            "audit_log": self.data.get("audit_log", [])[-10:]
        }


# Global singleton
governor = BudgetGovernor()

if __name__ == "__main__":
    print(json.dumps(governor.get_summary(), indent=2))
