# Quest of Seoul AI Server (FastAPI)

투어 가이드 AI 기능을 제공하는 Python 서버.

## RAG (Retrieval Augmented Generation)

질문과 투어 컨텍스트를 분석해 아래 정보를 자동 수집하여 LLM 답변에 반영합니다. 텍스트에서 키워드를 추출하고 API로 조회합니다.

| Retriever | 용도 | API | 비고 |
|-----------|------|-----|------|
| **WeatherRetriever** | 실시간 날씨 조회 | Open-Meteo | LocationResolver 좌표 기반 |
| **KnowledgeRetriever** | 관광지 운영시간·개요·휴무일 | 한국관광공사 Tour API | searchKeyword2, detailCommon2, detailIntro2 |
| **LocationResolver** | 장소명 → 위경도 변환 | Open-Meteo Geocoding, Nominatim | 텍스트에서 키워드 추출 후 API 조회 |

## 폴더 구조

```
ai-server/
├── app/
│   ├── __init__.py
│   ├── main.py              # FastAPI 앱, 라우터 등록
│   ├── config.py            # 환경 설정 (Pydantic Settings)
│   ├── api/
│   │   └── routes/          # API 라우트
│   │       ├── health.py
│   │       └── tour_guide.py
│   ├── schemas/             # 요청/응답 스키마 (Pydantic)
│   │   ├── common.py
│   │   └── tour_guide.py
│   └── services/            # 비즈니스 로직
│       ├── tour_guide.py    # OpenAI 호출 + RAG 통합
│       └── rag/             # RAG 컨텍스트 강화
│           ├── context_enricher.py
│           ├── tour_api_client.py  # 한국관광공사 Tour API 클라이언트
│           └── retrievers/
│               ├── weather_retriever.py   # Open-Meteo 날씨
│               ├── knowledge_retriever.py # Tour API 관광지 정보
│               └── location_resolver.py   # Geocoding (Open-Meteo, Nominatim)
├── scripts/
│   └── test_tour_api.py    # Tour API 연결 테스트
├── run.py
├── requirements.txt
├── Dockerfile
└── README.md
```

## 요구사항

- Python 3.10+

## 설치 및 실행

```bash
# 가상환경
python -m venv .venv
source .venv/bin/activate  # Windows: .venv\Scripts\activate

# 의존성
pip install -r requirements.txt

# .env 설정
cp .env.example .env
# OPENAI_API_KEY=sk-... 추가

# 실행
uvicorn app.main:app --reload --host 0.0.0.0 --port 8081
# 또는
python run.py
```

## 환경변수

| 변수 | 설명 | 기본값 |
|------|------|--------|
| PORT | 서버 포트 | 8081 |
| OPENAI_API_KEY | OpenAI API 키 | (필수) |
| OPENAI_MODEL | 사용 모델 | gpt-4o-mini |
| DATA_GO_KR_SERVICE_KEY | 한국관광공사 Tour API 키 (공공데이터포털 활용신청) | - |

Tour API 키가 없으면 KnowledgeRetriever는 비활성화되고, 날씨·위치 조회(Open-Meteo)는 API 키 없이 동작합니다.

**Tour API 키 설정 시:**
1. [공공데이터포털](https://www.data.go.kr) → "한국관광공사 국문 관광정보서비스" 활용신청
2. KorService2 (searchKeyword2, detailCommon2, detailIntro2) 사용
3. 마이페이지 → 인증키 복사 (**일반인증키(Encoding)** 권장)
4. 연결 테스트: `python scripts/test_tour_api.py`

## API

### GET /health

헬스체크.

### POST /tour-guide/chat

투어 가이드 AI 채팅.

**Request:**
```json
{
  "tourContext": "투어: 경복궁...",
  "history": [
    { "role": "user", "content": "광화문에 대해 알려주세요" },
    { "role": "assistant", "content": "광화문은..." }
  ]
}
```

**Response:**
```json
{
  "text": "광화문은 경복궁의 정문으로..."
}
```
