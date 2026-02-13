"""Knowledge Retriever - Tour API(VisitKorea) for tour information"""
import logging
import re

from app.config import get_settings
from app.services.rag.retrievers.base import BaseRetriever
from app.services.rag.tour_api_client import fetch_tour_info

logger = logging.getLogger(__name__)


def _extract_search_keywords(text: str) -> list[str]:
    """
    Extracts keywords for Tour API search from text.
    Extracts consecutive Korean characters (2+ characters) as candidates and passes them to the API.
    """
    if not text or not isinstance(text, str):
        return []
    t = text.strip()
    if len(t) < 2:
        return []

    # Extracts consecutive Korean characters (2+ characters) as candidates.
    hangul_chunks = re.findall(r"[가-힣]{2,10}", t)
    stopwords = {"오늘", "내일", "어제", "그곳", "저곳", "어떻게", "알려", "추천"}
    candidates = []
    for chunk in hangul_chunks:
        if chunk not in stopwords:
            if len(chunk) > 1 and chunk[-1] in "을를이가은는":
                candidates.append(chunk[:-1])
            candidates.append(chunk)

    seen = set()
    unique = []
    for c in sorted(candidates, key=len, reverse=True):
        if c not in seen and len(c) >= 2:
            seen.add(c)
            unique.append(c)

    prefix = t[:30].strip()
    if prefix and prefix not in seen and len(prefix) >= 2:
        unique.insert(0, prefix)

    return unique[:6]


class KnowledgeRetriever(BaseRetriever):
    """
    Retrieves tour information from the Tour API.
    Extracts keywords from text and searches the API.
    """

    def should_retrieve(self, query: str, tour_context: str) -> bool:
        combined = (query or "") + " " + (tour_context or "")
        if not get_settings().data_go_kr_service_key:
            return False
        keywords = _extract_search_keywords(combined)
        return len(keywords) > 0

    def retrieve(self, query: str, tour_context: str, **kwargs) -> str | None:
        combined = (query or "") + " " + (tour_context or "")
        keywords = _extract_search_keywords(combined)
        if not keywords:
            return None

        settings = get_settings()
        if not settings.data_go_kr_service_key:
            return None

        for kw in keywords:
            info = fetch_tour_info(settings.data_go_kr_service_key, kw)
            if info:
                lines = [f"[{info.get('장소', kw)} 관광 정보 - Tour API]"]
                for k, v in info.items():
                    if k != "장소" and v:
                        lines.append(f"- {k}: {v}")
                return "\n".join(lines)
        return None
