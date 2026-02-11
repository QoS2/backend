# Quest of Seoul AI Server (FastAPI)

투어 가이드 AI 기능을 제공하는 Python 서버.

## 폴더 구조

```
ai-server/
├── app/
│   ├── __init__.py
│   ├── main.py           # FastAPI 앱, 라우터 등록
│   ├── config.py         # 환경 설정 (Pydantic Settings)
│   ├── api/
│   │   └── routes/       # API 라우트
│   │       ├── health.py
│   │       └── tour_guide.py
│   ├── schemas/          # 요청/응답 스키마 (Pydantic)
│   │   ├── common.py
│   │   └── tour_guide.py
│   └── services/         # 비즈니스 로직
│       └── tour_guide.py # OpenAI 호출
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
