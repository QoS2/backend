"""Simple in-memory TTL cache for RAG API results."""
import threading
import time
from typing import Any

_cache: dict[str, tuple[Any, float]] = {}
_lock = threading.Lock()
_DEFAULT_TTL = 300  # 5 minutes


def get(key: str) -> Any | None:
    with _lock:
        if key not in _cache:
            return None
        val, ts = _cache[key]
        if time.time() - ts > _DEFAULT_TTL:
            del _cache[key]
            return None
        return val


def set(key: str, value: Any, ttl: int = _DEFAULT_TTL) -> None:
    with _lock:
        _cache[key] = (value, time.time())
