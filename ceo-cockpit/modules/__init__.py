"""
CEO Cockpit extension module registry.

Each feature module registers HTTP routes via the @route decorator. server.py
delegates unmatched /api/* requests to dispatch() before its legacy routing.
"""

import importlib
import traceback
from typing import Callable, Dict, List, Tuple

from modules.common import log, send_json

_EXACT: Dict[Tuple[str, str], Callable] = {}
_PREFIX: List[Tuple[str, str, Callable]] = []

# Feature modules loaded at startup (order is irrelevant; each self-registers).
FEATURE_MODULES = [
    "modules.ops",
]


def route(method: str, path: str, prefix: bool = False):
    """Register a handler: fn(handler, query, tail) where tail is the prefix remainder."""
    def decorator(fn: Callable) -> Callable:
        if prefix:
            _PREFIX.append((method.upper(), path, fn))
        else:
            _EXACT[(method.upper(), path)] = fn
        return fn
    return decorator


def load_all() -> None:
    for name in FEATURE_MODULES:
        try:
            importlib.import_module(name)
            log("modules", f"Loaded feature module {name}")
        except Exception as e:
            log("modules", f"FAILED to load {name}: {e}\n{traceback.format_exc()}")


def dispatch(handler, method: str, path: str, query: Dict[str, list]) -> bool:
    """Return True if a registered module handled the request."""
    method = method.upper()
    fn = _EXACT.get((method, path))
    tail = ""
    if fn is None:
        for m, p, f in _PREFIX:
            if m == method and path.startswith(p):
                fn, tail = f, path[len(p):]
                break
    if fn is None:
        return False
    try:
        fn(handler, query, tail)
    except ValueError as e:
        send_json(handler, 400, {"success": False, "error": str(e)})
    except Exception as e:
        log("modules", f"Handler error on {method} {path}: {e}\n{traceback.format_exc()}")
        try:
            send_json(handler, 500, {"success": False, "error": f"Internal error: {e}"})
        except Exception:
            pass
    return True


def registered_routes() -> List[str]:
    routes = [f"{m} {p}" for (m, p) in _EXACT.keys()]
    routes += [f"{m} {p}*" for (m, p, _) in _PREFIX]
    return sorted(routes)
