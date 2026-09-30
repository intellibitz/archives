"""Minimal CGI/session/MySQL helpers mirroring legacy PHP SAPI usage."""
from __future__ import annotations

import hashlib
import os
import re
import sys
import uuid
from http import cookies as http_cookies
from pathlib import Path
from typing import Any, Dict, List, Optional, Tuple
from urllib.parse import parse_qs, unquote_plus

SESSION_DIR = Path(os.environ.get("INTELLIGEEK_SESSION_DIR", "/tmp/intelligeek_sessions"))


def _parse_body() -> Dict[str, List[str]]:
    method = os.environ.get("REQUEST_METHOD", "GET").upper()
    qs = os.environ.get("QUERY_STRING", "")
    data: Dict[str, List[str]] = parse_qs(qs, keep_blank_values=True)
    if method == "POST":
        length = int(os.environ.get("CONTENT_LENGTH") or 0)
        raw = sys.stdin.buffer.read(length) if length else b""
        ctype = os.environ.get("CONTENT_TYPE", "")
        if "multipart/form-data" in ctype:
            try:
                import cgi

                form = cgi.FieldStorage(
                    fp=sys.stdin.buffer, environ=os.environ, keep_blank_values=True
                )
                for key in form.keys():
                    item = form[key]
                    if isinstance(item, list):
                        data[key] = item
                    elif getattr(item, "filename", None):
                        data[key] = [item]
                    else:
                        data[key] = [item.value if item.value is not None else ""]
            except Exception:
                pass
            return data
        body = raw.decode("utf-8", errors="replace")
        for k, v in parse_qs(body, keep_blank_values=True).items():
            data.setdefault(k, []).extend(v)
    return data


_REQUEST: Optional[Dict[str, List[str]]] = None


def _req() -> Dict[str, List[str]]:
    global _REQUEST
    if _REQUEST is None:
        _REQUEST = _parse_body()
    return _REQUEST


def get(name: str, default: str = "") -> str:
    vals = _req().get(name)
    if not vals:
        return default
    v = vals[0]
    if hasattr(v, "value"):
        return v.value or default
    return v if isinstance(v, str) else default


def post(name: str, default: str = "") -> str:
    return get(name, default)


def request(name: str, default: str = "") -> str:
    return get(name, default)


def files(name: str):
    vals = _req().get(name)
    if not vals:
        return None
    return vals[0]


def header(line: str) -> None:
    sys.stdout.write(line.rstrip("\r\n") + "\r\n")


def end_headers() -> None:
    sys.stdout.write("\r\n")


def redirect(location: str) -> None:
    header(f"Location: {location}")
    end_headers()


def html_start(content_type: str = "text/html") -> None:
    header(f"Content-Type: {content_type}")
    end_headers()


def echo(*parts: Any) -> None:
    sys.stdout.write("".join(str(p) for p in parts))


# --- sessions (cookie-backed file store) ---

_SESSION: Dict[str, Any] = {}
_SESSION_ID: Optional[str] = None
_SESSION_STARTED = False
_HEADERS_SENT = False
_PENDING_HEADERS: List[str] = []


def _load_cookie_sid() -> Optional[str]:
    raw = os.environ.get("HTTP_COOKIE", "")
    if not raw:
        return None
    c = http_cookies.SimpleCookie()
    try:
        c.load(raw)
    except http_cookies.CookieError:
        return None
    morsel = c.get("PHPSESSID")
    return morsel.value if morsel else None


def session_start() -> None:
    global _SESSION, _SESSION_ID, _SESSION_STARTED
    if _SESSION_STARTED:
        return
    _SESSION_STARTED = True
    SESSION_DIR.mkdir(parents=True, exist_ok=True)
    sid = _load_cookie_sid()
    if not sid or not re.fullmatch(r"[0-9a-fA-F-]{16,64}", sid or ""):
        sid = uuid.uuid4().hex
        _PENDING_HEADERS.append(f"Set-Cookie: PHPSESSID={sid}; Path=/")
    _SESSION_ID = sid
    path = SESSION_DIR / sid
    _SESSION = {}
    if path.exists():
        try:
            import json

            _SESSION = json.loads(path.read_text(encoding="utf-8"))
        except Exception:
            _SESSION = {}


def session_get(key: str, default=None):
    return _SESSION.get(key, default)


def session_set(key: str, value: Any) -> None:
    _SESSION[key] = value


def session_isset(key: str) -> bool:
    return key in _SESSION


def session_unset() -> None:
    _SESSION.clear()


def session_destroy() -> None:
    global _SESSION_ID
    if _SESSION_ID:
        path = SESSION_DIR / _SESSION_ID
        if path.exists():
            path.unlink()
    _SESSION.clear()


def session_save() -> None:
    if not _SESSION_STARTED or not _SESSION_ID:
        return
    import json

    SESSION_DIR.mkdir(parents=True, exist_ok=True)
    (SESSION_DIR / _SESSION_ID).write_text(json.dumps(_SESSION), encoding="utf-8")


def flush_headers(extra: Optional[List[str]] = None) -> None:
    global _HEADERS_SENT
    if _HEADERS_SENT:
        return
    for h in _PENDING_HEADERS:
        header(h)
    if extra:
        for h in extra:
            header(h)
    _HEADERS_SENT = True


# --- MySQL (pymysql preferred; mysql.connector fallback) ---

_CONN = None
_DB = None


def mysql_connect(host: str, user: str, password: str, **kwargs):
    global _CONN
    try:
        import pymysql

        _CONN = pymysql.connect(
            host=host,
            user=user,
            password=password,
            charset="utf8mb4",
            autocommit=True,
            **kwargs,
        )
    except ImportError:
        try:
            import MySQLdb  # type: ignore

            _CONN = MySQLdb.connect(host=host, user=user, passwd=password, **kwargs)
        except ImportError as exc:
            raise SystemExit(
                "MySQL driver required: pip install pymysql (legacy PHP used mysql_*)."
            ) from exc
    return _CONN


def mysql_select_db(name: str, conn=None) -> None:
    global _DB
    _DB = name
    c = conn or _CONN
    if c is None:
        raise SystemExit("No MySQL connection")
    if hasattr(c, "select_db"):
        try:
            c.select_db(name)
            return
        except Exception:
            pass
    cur = c.cursor()
    cur.execute(f"USE `{name}`")
    cur.close()


def mysql_error() -> str:
    if _CONN is None:
        return "no connection"
    try:
        return str(_CONN.error())
    except Exception:
        return "mysql error"


def mysql_real_escape_string(value: Any) -> str:
    s = "" if value is None else str(value)
    if _CONN is not None and hasattr(_CONN, "escape_string"):
        esc = _CONN.escape_string(s.encode("utf-8") if isinstance(s, str) else s)
        return esc.decode("utf-8") if isinstance(esc, bytes) else esc
    return (
        s.replace("\\", "\\\\")
        .replace("'", "\\'")
        .replace('"', '\\"')
        .replace("\x00", "\\0")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
    )


def mysql_query(sql: str, conn=None):
    c = conn or _CONN
    if c is None:
        raise SystemExit("No MySQL connection")
    cur = c.cursor()
    try:
        cur.execute(sql)
    except Exception:
        cur.close()
        raise
    _track_cursor(cur)
    return cur


def mysql_num_rows(result) -> int:
    if result is None:
        return 0
    if hasattr(result, "rowcount") and result.rowcount is not None and result.rowcount >= 0:
        # For SELECT, fetch to know size if needed
        pass
    rows = result.fetchall()
    result._cached_rows = rows  # type: ignore
    result._row_idx = 0  # type: ignore
    return len(rows)


def mysql_fetch_row(result):
    rows = getattr(result, "_cached_rows", None)
    if rows is not None:
        idx = getattr(result, "_row_idx", 0)
        if idx >= len(rows):
            return None
        result._row_idx = idx + 1  # type: ignore
        return rows[idx]
    return result.fetchone()


def mysql_fetch_array(result):
    return mysql_fetch_row(result)


def mysql_insert_id(result=None) -> int:
    if result is not None and getattr(result, "lastrowid", None):
        return int(result.lastrowid)
    if _CONN is not None and hasattr(_CONN, "insert_id"):
        try:
            return int(_CONN.insert_id())
        except Exception:
            pass
    return 0


def mysql_close(conn=None) -> None:
    global _CONN
    c = conn or _CONN
    if c is not None:
        c.close()
    if c is _CONN:
        _CONN = None


_LAST_CURSOR = None


def _track_cursor(cur) -> None:
    global _LAST_CURSOR
    _LAST_CURSOR = cur


def mysql_insert_id() -> int:
    if _LAST_CURSOR is not None and hasattr(_LAST_CURSOR, "lastrowid"):
        return int(_LAST_CURSOR.lastrowid or 0)
    if _CONN is not None and hasattr(_CONN, "insert_id"):
        return int(_CONN.insert_id())
    return 0


def mysql_affected_rows(result=None) -> int:
    if result is not None and hasattr(result, "rowcount") and result.rowcount is not None:
        return int(result.rowcount)
    if _LAST_CURSOR is not None and hasattr(_LAST_CURSOR, "rowcount"):
        rc = _LAST_CURSOR.rowcount
        return int(rc) if rc is not None and rc >= 0 else 0
    return 0


def remote_addr() -> str:
    return os.environ.get("REMOTE_ADDR") or os.environ.get("HTTP_X_FORWARDED_FOR") or ""


_COOKIES: Optional[http_cookies.SimpleCookie] = None


def _cookies() -> http_cookies.SimpleCookie:
    global _COOKIES
    if _COOKIES is None:
        _COOKIES = http_cookies.SimpleCookie()
        raw = os.environ.get("HTTP_COOKIE", "")
        if raw:
            try:
                _COOKIES.load(raw)
            except http_cookies.CookieError:
                pass
    return _COOKIES


def cookie(name: str, default: str = "") -> str:
    m = _cookies().get(name)
    return m.value if m else default


def set_cookie(name: str, value: str, max_age: int = 3600 * 24 * 30) -> None:
    _PENDING_HEADERS.append(
        f"Set-Cookie: {name}={value}; Path=/; Max-Age={max_age}"
    )


def password_mysql(plaintext: str) -> str:
    """Approximate old MySQL PASSWORD() (4.1+)*hash for local fallbacks."""
    # * + uppercase SHA1(SHA1(password))
    inner = hashlib.sha1(plaintext.encode("utf-8")).digest()
    outer = hashlib.sha1(inner).hexdigest().upper()
    return "*" + outer


def include_text(path: Path) -> str:
    return path.read_text(encoding="utf-8", errors="replace")
