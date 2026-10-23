"""
Request security guard for the CEO Cockpit.

The cockpit service runs as LocalSystem and exposes command-execution features,
so every request must originate from this machine's own browser UI:

  * Host header must be a loopback name (defeats DNS-rebinding attacks).
  * State-changing requests (POST/DELETE/PUT) carrying an Origin header must
    come from the cockpit's own origin (defeats cross-site request forgery
    from any other web page open in the browser).
  * Non-browser local clients (curl, PowerShell) send no Origin and are allowed.
"""

import os
from typing import Optional, Tuple

PORT = int(os.environ.get("COCKPIT_PORT", "8080"))
BIND_HOST = os.environ.get("COCKPIT_BIND_HOST", "127.0.0.1")

_LOOPBACK_NAMES = {"127.0.0.1", "localhost", "[::1]"}
_extra = {h.strip().lower() for h in os.environ.get("COCKPIT_ALLOWED_HOSTS", "").split(",") if h.strip()}
ALLOWED_HOSTS = _LOOPBACK_NAMES | _extra

PRIMARY_ORIGIN = f"http://127.0.0.1:{PORT}"
ALLOWED_ORIGINS = {f"http://{h}:{PORT}" for h in ALLOWED_HOSTS} | {f"http://{h}" for h in ALLOWED_HOSTS}


def _host_name(host_header: str) -> str:
    host = (host_header or "").strip().lower()
    if host.startswith("["):
        return host.split("]")[0] + "]"
    return host.split(":")[0]


def check_request(method: str, headers) -> Tuple[bool, Optional[str]]:
    host = headers.get("Host", "")
    if host and _host_name(host) not in ALLOWED_HOSTS:
        return False, f"Host '{host}' is not permitted"

    if method.upper() in ("POST", "DELETE", "PUT", "PATCH"):
        origin = headers.get("Origin")
        if origin and origin.lower().rstrip("/") not in ALLOWED_ORIGINS:
            return False, f"Cross-origin request from '{origin}' rejected"
    return True, None


def cors_origin(headers) -> str:
    origin = (headers.get("Origin") or "").lower().rstrip("/")
    return origin if origin in ALLOWED_ORIGINS else PRIMARY_ORIGIN
