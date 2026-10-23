"""
Round 11 — Spend Forecaster & Runway Predictor.

Delivers predictive financial burn telemetry, runway estimations,
model mix impact simulation, and budget exhaustion warnings.
"""

import time
from typing import Any, Dict, List
from budget_governor import governor, MODEL_PRICING
from modules import route
from modules.common import data_path, read_json_body, read_jsonl, send_json

ANALYTICS_LEDGER_FILE = data_path("spend_ledger.jsonl")


def _get_recent_burn_rate() -> Dict[str, Any]:
    records = read_jsonl(ANALYTICS_LEDGER_FILE, limit=500)
    now = time.time()
    one_hour_ago = now - 3600
    one_day_ago = now - 86400

    past_hour_spend = 0.0
    past_hour_tokens = 0
    past_day_spend = 0.0
    past_day_tokens = 0

    model_counts: Dict[str, int] = {}
    model_spend: Dict[str, float] = {}

    for r in records:
        ts = r.get("timestamp", 0)
        cost = float(r.get("cost_usd", 0.0))
        tokens = int(r.get("total_tokens", 0))
        m = r.get("model", "unknown")

        if ts >= one_day_ago:
            past_day_spend += cost
            past_day_tokens += tokens
            model_counts[m] = model_counts.get(m, 0) + 1
            model_spend[m] = model_spend.get(m, 0.0) + cost

        if ts >= one_hour_ago:
            past_hour_spend += cost
            past_hour_tokens += tokens

    summary = governor.get_summary()
    daily_cap = summary.get("daily_budget_usd", 5.0)
    today_spend = summary.get("today_spend_usd", 0.0)
    rem_daily = max(0.0, daily_cap - today_spend)

    # Hourly velocity in USD
    burn_per_hour = max(past_hour_spend, past_day_spend / 24.0, 0.001)
    hours_runway = rem_daily / burn_per_hour if burn_per_hour > 0 else 999.0

    return {
        "burn_per_hour_usd": round(burn_per_hour, 6),
        "projected_24h_burn_usd": round(burn_per_hour * 24, 4),
        "past_hour_tokens": past_hour_tokens,
        "past_day_tokens": past_day_tokens,
        "past_day_spend_usd": round(past_day_spend, 4),
        "daily_cap_usd": daily_cap,
        "today_spend_usd": round(today_spend, 4),
        "remaining_daily_usd": round(rem_daily, 4),
        "hours_to_daily_cap": round(min(999.0, hours_runway), 1),
        "days_runway": round(rem_daily / (burn_per_hour * 24), 2) if burn_per_hour > 0 else 999.0,
        "risk_level": "CRITICAL" if hours_runway < 2 else ("WARNING" if hours_runway < 6 else "NOMINAL"),
        "model_distribution": {
            m: {"calls": model_counts[m], "spend_usd": round(model_spend[m], 5)}
            for m in sorted(model_spend.keys(), key=lambda k: model_spend[k], reverse=True)
        }
    }


@route("GET", "/api/forecast/runway")
def handle_forecast_runway(handler, query, tail):
    burn = _get_recent_burn_rate()
    summary = governor.get_summary()
    send_json(handler, 200, {
        "success": True,
        "runway": burn,
        "governor": summary,
        "timestamp": time.time()
    })


@route("GET", "/api/forecast/burn-rate")
def handle_forecast_burn(handler, query, tail):
    burn = _get_recent_burn_rate()
    send_json(handler, 200, {
        "success": True,
        "burn_rate": burn
    })


@route("POST", "/api/forecast/simulate")
def handle_forecast_simulate(handler, query, tail):
    body = read_json_body(handler) or {}
    target_model = body.get("model", "deepseek-reasoner")
    daily_queries = int(body.get("queries_per_day", 100))
    avg_input_tokens = int(body.get("avg_input_tokens", 2500))
    avg_output_tokens = int(body.get("avg_output_tokens", 1000))

    meta = MODEL_PRICING.get(target_model, {})
    p_in = float(meta.get("prompt", 0.50))
    p_out = float(meta.get("completion", 2.00))

    cost_per_query = (avg_input_tokens * p_in + avg_output_tokens * p_out) / 1_000_000.0
    daily_cost = cost_per_query * daily_queries
    monthly_cost = daily_cost * 30.0

    summary = governor.get_summary()
    daily_cap = summary.get("daily_budget_usd", 5.0)

    send_json(handler, 200, {
        "success": True,
        "model": target_model,
        "pricing": {"prompt_per_m": p_in, "completion_per_m": p_out},
        "simulation": {
            "queries_per_day": daily_queries,
            "cost_per_query_usd": round(cost_per_query, 6),
            "cost_per_query_cents": round(cost_per_query * 100, 4),
            "projected_daily_spend_usd": round(daily_cost, 4),
            "projected_monthly_spend_usd": round(monthly_cost, 2),
            "fits_daily_budget": daily_cost <= daily_cap,
            "budget_utilization_pct": round((daily_cost / daily_cap) * 100.0, 1) if daily_cap > 0 else 0
        }
    })
