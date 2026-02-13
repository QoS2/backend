"""Vector Retriever - Pgvector similarity search for tour knowledge (history, culture, etc.)"""
import json
import logging
from urllib.request import Request, urlopen
from urllib.error import HTTPError, URLError
from urllib.parse import urlencode

from app.config import get_settings
from app.services.rag.retrievers.base import BaseRetriever

logger = logging.getLogger(__name__)

# If the question is based on history/culture/description, use vector search
KNOWLEDGE_KEYWORDS = [
    "역사", "유래", "설명", "의미", "뭐야", "무엇", "어떻게", "왜", "배경",
    "문화", "건축", "이름", "만든", "지었다", "세운", "알려", "소개",
]

OPENAI_EMBEDDING_URL = "https://api.openai.com/v1/embeddings"
EMBEDDING_MODEL = "text-embedding-3-small"
EMBEDDING_DIM = 1536


def _embed_text(text: str) -> list[float] | None:
    """OpenAI Embeddings API for text embedding."""
    settings = get_settings()
    if not settings.openai_api_key:
        return None
    try:
        body = json.dumps({
            "model": EMBEDDING_MODEL,
            "input": (text[:8000] if len(text) > 8000 else text),
            "dimensions": EMBEDDING_DIM,
        }).encode("utf-8")
        req = Request(
            OPENAI_EMBEDDING_URL,
            data=body,
            headers={
                "Content-Type": "application/json",
                "Authorization": f"Bearer {settings.openai_api_key}",
            },
            method="POST",
        )
        with urlopen(req, timeout=15) as resp:
            data = json.loads(resp.read().decode())
        items = data.get("data", [])
        if not items:
            return None
        return items[0].get("embedding")
    except (URLError, HTTPError, json.JSONDecodeError, KeyError) as e:
        logger.warning("Embedding failed: %s", e)
        return None


class VectorRetriever(BaseRetriever):
    """
    Pgvector similarity search for tour knowledge.
    Shared with Spring Boot's tour_knowledge_embeddings table.
    """

    def should_retrieve(self, query: str, tour_context: str) -> bool:
        if not get_settings().is_database_configured:
            return False
        combined = (query or "") + " " + (tour_context or "")
        return any(kw in combined for kw in KNOWLEDGE_KEYWORDS) or len(combined.strip()) >= 5

    def retrieve(self, query: str, tour_context: str, **kwargs) -> str | None:
        settings = get_settings()
        if not settings.is_database_configured:
            return None

        combined = (query or "").strip() or (tour_context or "")[:200]
        if not combined:
            return None

        embedding = _embed_text(combined)
        if not embedding:
            return None

        try:
            import psycopg
        except ImportError:
            logger.warning("psycopg not installed, VectorRetriever disabled")
            return None

        vector_str = "[" + ",".join(str(x) for x in embedding) + "]"
        limit = 5

        try:
            with psycopg.connect(settings.database_url) as conn:
                with conn.cursor() as cur:
                    cur.execute(
                        """
                        SELECT content FROM tour_knowledge_embeddings
                        WHERE embedding IS NOT NULL
                        ORDER BY embedding <=> %s::vector
                        LIMIT %s
                        """,
                        (vector_str, limit),
                    )
                    rows = cur.fetchall()
            if not rows:
                return None
            parts = [r[0] for r in rows if r[0]]
            if not parts:
                return None
            return "\n---\n".join(parts[:3])  # Only top 3
        except Exception as e:
            logger.warning("Vector search failed: %s", e)
            return None
