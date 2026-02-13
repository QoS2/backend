# Quest of Seoul Backend

Claude Code가 이 프로젝트에서 작업할 때 참고하는 프로젝트 컨텍스트입니다.

**상세 규칙 및 가이드**: [AGENTS.md](./AGENTS.md) 참조.

---

## 프로젝트 개요

| 영역 | 경로 | 기술 |
|------|------|------|
| AI Server | `ai-server/` | Python 3.10+, FastAPI, Uvicorn, OpenAI, RAG |
| Backend | `spring-boot/questofseoul/` | Java 17, Spring Boot 3.3.5, Gradle |
| Admin Frontend | `frontend/administration-page/` | React 19, Vite 7, TypeScript 5.9 |

PostgreSQL(PostGIS, Pgvector)를 Spring Boot와 ai-server가 공유합니다.

---

## 핵심 명령어

```bash
# AI Server
cd ai-server && uvicorn app.main:app --reload --host 0.0.0.0 --port 8081
cd ai-server && python scripts/test_rag.py

# Spring Boot
cd spring-boot/questofseoul && ./gradlew bootRun
cd spring-boot/questofseoul && ./gradlew test

# Frontend
cd frontend/administration-page && npm run dev
cd frontend/administration-page && npm run lint
```

---

## 응답 언어

- 사용자와 소통할 때 **한국어**로 답변합니다.

---

## 주의사항

- `.env`, `.env.*` 파일 읽기 금지 (시크릿 보호)
- 패키지 설치(`pip install`, `npm install`) 전 사용자 승인 필요
- `git push`, 대규모 리팩터링 전 사용자 승인 필요
