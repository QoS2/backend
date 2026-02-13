"""RAG Context Enricher - Orchestrates retrievers and augments prompt context"""
import logging
from app.services.rag.retrievers.weather_retriever import WeatherRetriever
from app.services.rag.retrievers.knowledge_retriever import KnowledgeRetriever

logger = logging.getLogger(__name__)


class RAGContextEnricher:
    """
    Enriches the context of the answer using RAG.
    Retrieves weather, knowledge base, etc. information and returns it as a text.
    Used to enrich the context of the answer.
    """

    def __init__(self) -> None:
        self.retrievers = [
            WeatherRetriever(),
            KnowledgeRetriever(),
        ]

    def enrich(self, query: str, tour_context: str, history: list | None = None) -> str:
        """
        Retrieves weather, knowledge base, etc. information and returns it as a text.
        Used to enrich the context of the answer.
        """
        parts: list[str] = []
        # 마지막 사용자 메시지 추출 (history 우선, 없으면 tour_context 또는 query)
        last_message = query or ""
        if history and len(history) > 0:
            for h in reversed(history):
                if getattr(h, "role", None) == "user" and getattr(h, "content", ""):
                    last_message = str(h.content)
                    break
        if not last_message and tour_context:
            last_message = tour_context

        for retriever in self.retrievers:
            if not retriever.should_retrieve(last_message, tour_context):
                continue
            try:
                ctx = retriever.retrieve(last_message, tour_context)
                if ctx and ctx.strip():
                    parts.append(ctx.strip())
            except Exception as e:
                logger.warning("Retriever %s failed: %s", type(retriever).__name__, e)

        if not parts:
            return ""
        return "\n\n".join(parts)
