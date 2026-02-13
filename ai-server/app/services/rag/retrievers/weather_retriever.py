"""Weather Retriever - Fetches real-time weather via Open-Meteo API"""
import logging
from urllib.request import urlopen, Request
from urllib.error import URLError, HTTPError
import json

from app.services.rag.retrievers.base import BaseRetriever
from app.services.rag.retrievers.location_resolver import resolve_location

logger = logging.getLogger(__name__)

# Open-Meteo free API
OPEN_METEO_URL = "https://api.open-meteo.com/v1/forecast"

USER_AGENT = (
    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.132 Safari/537.36"
)

# Keywords related to weather/clothes - if these keywords are present, weather is retrieved
WEATHER_RELATED_KEYWORDS = [
    "날씨", "옷", "복장", "입고", "쌀쌀", "따뜻", "춥", "더우", "선선",
    "비", "눈", "우산", "외투", "코트", "재킷", "스웨터", "가벼운",
    "따뜻하게", "시원하게", "체감", "기온", "온도", "날씨에",
]

# WMO weather codes -> Korean description
WEATHER_DESC = {
    0: "맑음",
    1: "대체로 맑음",
    2: "부분적 흐림",
    3: "흐림",
    45: "안개",
    48: "서리 안개",
    51: "이슬비(약함)",
    53: "이슬비(보통)",
    55: "이슬비(강함)",
    56: "진한 이슬비(약함)",
    57: "진한 이슬비(강함)",
    61: "비(약함)",
    63: "비(보통)",
    65: "비(강함)",
    66: "진한 비(약함)",
    67: "진한 비(강함)",
    71: "눈(약함)",
    73: "눈(보통)",
    75: "눈(강함)",
    77: "진눈깨비",
    80: "소나기(약함)",
    81: "소나기(보통)",
    82: "소나기(강함)",
    85: "눈 소나기(약함)",
    86: "눈 소나기(강함)",
    95: "뇌우",
    96: "뇌우+작은 우박",
    99: "뇌우+큰 우박",
}


def _get_weather_desc(code: int) -> str:
    return WEATHER_DESC.get(code, "기타")


class WeatherRetriever(BaseRetriever):
    """Fetches current weather for location derived from tour context"""

    def should_retrieve(self, query: str, tour_context: str) -> bool:
        combined = (query or "") + " " + (tour_context or "")
        return any(kw in combined for kw in WEATHER_RELATED_KEYWORDS)

    def retrieve(self, query: str, tour_context: str, **kwargs) -> str | None:
        try:
            coords = resolve_location((query or "") + " " + (tour_context or ""))
            if not coords:
                return None
            lat, lon = coords
            params = {
                "latitude": lat,
                "longitude": lon,
                "current": "temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m,precipitation,apparent_temperature",
                "hourly": "temperature_2m,weather_code,precipitation_probability",
                "daily": "weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max",
                "timezone": "Asia/Seoul",
                "forecast_days": 2,
            }
            qs = "&".join(f"{k}={v}" for k, v in params.items())
            url = f"{OPEN_METEO_URL}?{qs}"
            req = Request(url, headers={"User-Agent": USER_AGENT})
            with urlopen(req, timeout=5) as resp:
                data = json.loads(resp.read().decode())

            curr = data.get("current", {})
            if not curr:
                return None

            temp = curr.get("temperature_2m")
            code = curr.get("weather_code", 0)
            humidity = curr.get("relative_humidity_2m")
            wind = curr.get("wind_speed_10m")
            precip = curr.get("precipitation")

            desc = _get_weather_desc(code)
            parts = [
                f"현재 날씨: {desc}",
                f"기온: {temp}°C",
                f"습도: {humidity}%",
                f"풍속: {wind} km/h",
            ]
            if precip and float(precip) > 0:
                parts.append(f"강수량: {precip} mm")
            apparent = curr.get("apparent_temperature")
            if apparent is not None:
                parts.append(f"체감기온: {apparent}°C")

            daily = data.get("daily", {})
            if daily and daily.get("temperature_2m_max"):
                max_temps = daily["temperature_2m_max"]
                min_temps = daily["temperature_2m_min"]
                if max_temps and min_temps:
                    parts.append(f"내일 예상: 최저 {min_temps[0]}°C, 최고 {max_temps[0]}°C")

            return "\n".join(parts)
        except (URLError, HTTPError, json.JSONDecodeError, KeyError) as e:
            logger.warning("Weather retrieval failed: %s", e)
            return None
