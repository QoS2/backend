"""Base Retriever Interface"""
from abc import ABC, abstractmethod


class BaseRetriever(ABC):
    """Base class for RAG retrievers"""

    @abstractmethod
    def should_retrieve(self, query: str, tour_context: str) -> bool:
        """Whether this retriever should be used for the given query."""
        pass

    @abstractmethod
    def retrieve(self, query: str, tour_context: str, **kwargs) -> str | None:
        """
        Retrieve relevant context. Returns None if retrieval fails or is not applicable.
        """
        pass
