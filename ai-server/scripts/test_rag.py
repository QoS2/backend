#!/usr/bin/env python3
"""Test RAG components and Tour Guide Chat"""
import json
import os
import sys

# Add ai-server app path
sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))

def test_knowledge_retriever():
    """KnowledgeRetriever - Tour API test"""
    print("\n=== 1. KnowledgeRetriever (Tour API) ===")
    from app.services.rag.retrievers.knowledge_retriever import KnowledgeRetriever
    retriever = KnowledgeRetriever()
    query, ctx = "경복궁 이용시간 알려줘", "투어: 경복궁"
    if retriever.should_retrieve(query, ctx):
        result = retriever.retrieve(query, ctx)
        if result:
            print("Success:", result[:200] + "..." if len(result) > 200 else result)
        else:
            print("No result")
    else:
        print("should_retrieve=False")

def test_weather_retriever():
    """WeatherRetriever test"""
    print("\n=== 2. WeatherRetriever ===")
    from app.services.rag.retrievers.weather_retriever import WeatherRetriever
    retriever = WeatherRetriever()
    query, ctx = "오늘 경복궁 날씨 어때?", "경복궁"
    if retriever.should_retrieve(query, ctx):
        result = retriever.retrieve(query, ctx)
        if result:
            print("Success:", result)
        else:
            print("No result")
    else:
        print("should_retrieve=False")

def test_vector_retriever():
    """VectorRetriever - Pgvector test"""
    print("\n=== 3. VectorRetriever (Pgvector) ===")
    from app.config import get_settings
    if not get_settings().is_database_configured:
        print("DATABASE_URL not configured - VectorRetriever skipped")
        return
    from app.services.rag.retrievers.vector_retriever import VectorRetriever
    retriever = VectorRetriever()
    query, ctx = "광화문에 대해 알려줘", "투어: 경복궁"
    if retriever.should_retrieve(query, ctx):
        result = retriever.retrieve(query, ctx)
        if result:
            print("Success:", result[:300] + "..." if len(result) > 300 else result)
        else:
            print("No result")
    else:
        print("should_retrieve=False")

def test_context_enricher():
    """RAGContextEnricher - entire pipeline"""
    print("\n=== 4. RAGContextEnricher (entire pipeline) ===")
    from app.services.rag.context_enricher import RAGContextEnricher
    enricher = RAGContextEnricher()
    query = "경복궁 날씨랑 이용시간 알려줘"
    tour_context = "투어: 경복궁 산책\n- 스팟 1: 광화문\n- 스팟 2: 근정전"
    history = []
    ctx = enricher.enrich(query, tour_context, history)
    if ctx:
        print("Context collected:")
        print(ctx[:500] + "..." if len(ctx) > 500 else ctx)
    else:
        print("No context")

def test_tour_guide_chat():
    """Tour Guide Chat API simulation"""
    print("\n=== 5. TourGuideService.chat (actual LLM call) ===")
    from app.services.tour_guide import TourGuideService
    from app.schemas.tour_guide import ChatMessage
    svc = TourGuideService()
    history = [ChatMessage(role="user", content="경복궁 이용시간이 어떻게 되나요?")]
    tour_context = "투어: 경복궁\n- 스팟: 광화문"
    try:
        resp = svc.chat(tour_context, history)
        if resp.text and "비활성화" not in resp.text:
            print("Response:", resp.text[:400] + "..." if len(resp.text) > 400 else resp.text)
        else:
            print("Error:", resp.text)
    except Exception as e:
        print("Error:", e)

if __name__ == "__main__":
    print("RAG test started")
    test_knowledge_retriever()
    test_weather_retriever()
    test_vector_retriever()
    test_context_enricher()
    test_tour_guide_chat()
    print("\n=== Test completed ===")
