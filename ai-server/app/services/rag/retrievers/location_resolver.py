"""Location Resolver - Resolves place names to coordinates via Geocoding APIs"""
import json
import logging
import re
from urllib.parse import quote
from urllib.request import Request, urlopen

logger = logging.getLogger(__name__)

OPEN_METEO_URL = "https://geocoding-api.open-meteo.com/v1/search"
NOMINATIM_URL = "https://nominatim.openstreetmap.org/search"

# Geocoding API try countryCode (KR=Korea)
DEFAULT_COUNTRY = "KR"


def resolve_location(text: str, country_code: str = DEFAULT_COUNTRY) -> tuple[float, float] | None:
    """
    Resolve place name to (lat, lon) via Geocoding APIs.
    Extracts potential place names from text and looks up via Open-Meteo → Nominatim.
    """
    keywords = _extract_keywords(text)
    if not keywords:
        return None

    for kw in keywords[:5]:  # Try up to 5 candidates
        try:
            coords = _geocode_openmeteo(kw, country_code)
            if coords:
                return coords
            coords = _geocode_nominatim(kw)
            if coords:
                return coords
        except Exception as e:
            logger.debug("Geocoding failed for %r: %s", kw, e)

    return None


def _extract_keywords(text: str) -> list[str]:
    """
    Extracts keywords for Geocoding/Tour API search from text.
    Extracts consecutive Korean characters (2+ characters) as candidates and passes them to the API.
    """
    if not text or not isinstance(text, str):
        return []
    t = text.strip()
    if len(t) < 2:
        return []

    # Extracts consecutive Korean characters (2+ characters) as candidates.
    hangul_chunks = re.findall(r"[가-힣]{2,10}", t)
    # Try to remove particles: "경복궁을" -> "경복궁" (을/를/이/가/은/는 등)
    particles = {"을", "를", "이", "가", "은", "는", "에", "에서", "의", "와", "과", "로", "으로"}
    candidates = []
    for chunk in hangul_chunks:
        if len(chunk) >= 2 and chunk not in {"오늘", "내일", "어제", "그곳", "저곳"}:
            if len(chunk) > 1 and chunk[-1] in "을를이가은는":
                candidates.append(chunk[:-1])
            candidates.append(chunk)

    # Remove duplicates, prioritize longer ones.
    seen = set()
    unique = []
    for c in sorted(candidates, key=len, reverse=True):
        if c not in seen and len(c) >= 2:
            seen.add(c)
            unique.append(c)

    # Add up to 40 characters (search by sentence unit).
    prefix = t[:40].strip()
    if prefix and prefix not in seen:
        unique.insert(0, prefix)

    return unique[:8]


def _geocode_openmeteo(query: str, country_code: str = "KR") -> tuple[float, float] | None:
    """Open-Meteo Geocoding API."""
    encoded = quote(query)
    url = f"{OPEN_METEO_URL}?name={encoded}&count=3&language=ko&countryCode={country_code}"
    req = Request(url, headers={"User-Agent": "QuestOfSeoul-AI/1.0"})
    with urlopen(req, timeout=4) as resp:
        data = json.loads(resp.read().decode())
    results = data.get("results")
    if not results:
        return None
    first = results[0]
    lat, lon = first.get("latitude"), first.get("longitude")
    if lat is not None and lon is not None:
        return (float(lat), float(lon))
    return None


def _geocode_nominatim(query: str) -> tuple[float, float] | None:
    """Nominatim(OSM) Geocoding API (fallback)."""
    encoded = quote(query)
    url = f"{NOMINATIM_URL}?q={encoded}&format=json&limit=1"
    req = Request(url, headers={"User-Agent": "QuestOfSeoul-AI/1.0"})
    with urlopen(req, timeout=4) as resp:
        data = json.loads(resp.read().decode())
    if not data or not isinstance(data, list):
        return None
    first = data[0] if data else {}
    lat, lon = first.get("lat"), first.get("lon")
    if lat is not None and lon is not None:
        return (float(lat), float(lon))
    return None
