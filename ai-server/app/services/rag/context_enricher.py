"""RAG Context Enricher - Orchestrates retrievers and augments prompt context"""
import logging
from app.services.rag.retrievers.weather_retriever import WeatherRetriever
from app.services.rag.retrievers.knowledge_retriever import KnowledgeRetriever
from app.services.rag.retrievers.vector_retriever import VectorRetriever

logger = logging.getLogger(__name__)


class RAGContextEnricher:
    """
    Enriches the context of the answer using RAG.
    Retrieves weather, knowledge base, vector (Pgvector), etc. and returns as text.
    """

    def __init__(self) -> None:
        self.retrievers = [
            WeatherRetriever(),
            KnowledgeRetriever(),
            VectorRetriever(),
        ]

    def enrich(self, query: str, tour_context: str, history: list | None = None) -> str:
        """
        Retrieves weather, knowledge base, etc. information and returns it as a text.
        Used to enrich the context of the answer.
        """
        parts: list[str] = []
        # Last user message extraction (history first, then tour_context or query)
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
