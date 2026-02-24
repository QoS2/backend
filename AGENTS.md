# Quest of Seoul Backend - AI Agent Guide

이 프로젝트에서 AI 코딩 에이전트(Cursor, GitHub Copilot 등)가 따라야 할 규칙, 프로젝트 지식, 명령어, 경계를 정의합니다.

---

## 1. Commands (명령어)

에이전트가 자주 참조할 실행 명령어. **파일 스코프 검증을 우선**하고, 전체 빌드는 명시적으로 요청된 경우에만 실행합니다.

### AI Server (Python / FastAPI)

```bash
# 단일 파일 포맷 (Black)
cd ai-server && python -m black app/api/routes/tour_guide.py

# 단일 파일 린트 (ruff 기본 설정 시)
cd ai-server && ruff check app/services/rag/vector_retriever.py

# RAG 테스트 스크립트
cd ai-server && python scripts/test_rag.py

# 서버 실행 (디버그/개발)
cd ai-server && uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

### Spring Boot (Java 17 / Gradle)

```bash
# 단일 테스트 클래스
cd spring-boot/questofseoul && ./gradlew test --tests "com.app.questofseoul.service.VectorSearchServiceTest"

# 전체 테스트
cd spring-boot/questofseoul && ./gradlew test

# 서버 실행
cd spring-boot/questofseoul && ./gradlew bootRun
```

### Frontend (React / Vite / TypeScript)

```bash
# 단일 파일 타입체크
cd frontend/administration-page && npx tsc --noEmit

# 린트 (전체 또는 특정 파일)
cd frontend/administration-page && npm run lint
cd frontend/administration-page && npx eslint src/pages/ToursPage.tsx --fix

# 포맷 (Prettier)
cd frontend/administration-page && npx prettier --write src/components/ui/Button.tsx

# 개발 서버
cd frontend/administration-page && npm run dev
```

### 원칙

- **수정한 파일만** 검증: 린트, 타입체크, 포맷은 수정된 파일 위주로 실행
- **전체 빌드/테스트**: 사용자가 명시적으로 요청할 때만
- **패키지 설치** (`pip install`, `npm install`, `./gradlew dependencies`): 사용자 승인 후에만 수행

---

## 2. Project Knowledge (프로젝트 지식)

### Tech Stack (버전 포함)

| 영역 | 기술 | 버전 |
|------|------|------|
| AI Server | Python | 3.10+ |
| AI Server | FastAPI | ≥0.115.0 |
| AI Server | Uvicorn | ≥0.32.0 |
| AI Server | OpenAI SDK | ≥1.0.0 |
| AI Server | Pydantic Settings | ≥2.0.0 |
| Backend | Java | 17 |
| Backend | Spring Boot | 3.3.5 |
| Backend | Gradle | (wrapper 사용) |
| Backend | PostgreSQL + PostGIS + Pgvector | Hibernate 6.5.3 |
| Frontend | React | 19.x |
| Frontend | Vite | 7.x |
| Frontend | TypeScript | 5.9.x |
| Frontend | React Router | 7.x |
| Frontend | TanStack Query | 5.x |
| Frontend | React Hook Form + Zod | - |
| Frontend | @dnd-kit | 6.x, 10.x |

### 아키텍처 개요

- **ai-server**: 투어 가이드 AI (OpenAI + RAG). 한국관광공사 Tour API, Open-Meteo, Pgvector 연동
- **spring-boot/questofseoul**: 메인 백엔드. 투어, 스팟, 런, 미션, 채팅, 인증, S3 업로드
- **frontend/administration-page**: 관리자 페이지 (투어/스팟 CRUD, Enum 관리 등)
- **공유**: PostgreSQL(PostGIS, Pgvector) — Spring Boot와 ai-server가 동일 DB 사용

### RAG 구조 (ai-server)

| Retriever | 용도 | 데이터 소스 |
|-----------|------|-------------|
| WeatherRetriever | 실시간 날씨, 내일 예보 | Open-Meteo |
| KnowledgeRetriever | 관광지 운영시간, 개요, 휴무일 | 한국관광공사 Tour API |
| VectorRetriever | 역사·문화·가이드 지식 | Pgvector (OpenAI Embedding) |
| LocationResolver | 장소명 → 위경도 | Open-Meteo Geocoding, Nominatim |

---

## 3. File Structure (파일 구조)

```
backend/
├── ai-server/                    # Python FastAPI
│   ├── app/
│   │   ├── main.py                # 앱 진입, 라우터 등록
│   │   ├── config.py             # Pydantic Settings (env)
│   │   ├── api/routes/            # API 라우트 (health, tour_guide)
│   │   ├── schemas/               # Pydantic 요청/응답 모델
│   │   └── services/              # 비즈니스 로직
│   │       └── rag/               # RAG: context_enricher, retrievers
│   ├── scripts/                  # 테스트 스크립트 (test_rag, test_tour_api)
│   ├── .env.example
│   └── requirements.txt
│
├── spring-boot/questofseoul/      # Java Spring Boot
│   ├── src/main/java/com/app/questofseoul/
│   │   ├── QuestofseoulApplication.java
│   │   ├── config/               # 설정 (Security, Pgvector, Dotenv 등)
│   │   ├── controller/           # REST 컨트롤러
│   │   │   └── admin/            # 관리자 API (/api/v1/admin/*)
│   │   ├── domain/entity/        # JPA 엔티티
│   │   ├── domain/enums/
│   │   ├── dto/                  # 요청/응답 DTO
│   │   ├── exception/
│   │   ├── repository/           # JPA Repository
│   │   ├── service/
│   │   └── security/
│   ├── build.gradle
│   └── .env.example
│
├── frontend/administration-page/  # React + Vite
│   ├── src/
│   │   ├── main.tsx, App.tsx
│   │   ├── pages/                # ToursPage, DashboardPage, LoginPage, EnumsPage
│   │   ├── components/           # Layout, ui (Button, Input, Modal 등)
│   │   └── context/
│   └── package.json
│
├── .vscode/                      # launch.json, tasks.json, settings.json
└── AGENTS.md                     # 본 문서
```

### 핵심 참조 포인트

- **라우트 추가**: `ai-server/app/main.py`에 라우터 등록
- **RAG Retriever 추가**: `ai-server/app/services/rag/retrievers/` 패턴 따르기
- **Spring 컨트롤러**: `@RestController`, `@RequestMapping("/api/v1/...")`, `@RequiredArgsConstructor`
- **공개 API**: `/api/v1` (인증: `@SecurityRequirement(name = "bearerAuth")`)
- **관리자 API**: `/api/v1/admin/*` (인증: `sessionAuth`)
- **Swagger UI**: Spring Boot 실행 시 `/swagger-ui.html`
- **미션 스키마 단일 문서**: `spring-boot/questofseoul/docs/API.md` (4.6.1)
- **프론트 공통 컴포넌트**: `frontend/administration-page/src/components/ui/`

---

## 4. Code Style & Conventions

### Python (ai-server)

- **포맷**: Black
- **타입 힌트**: 함수 시그니처에 명시
- **환경 설정**: `app/config.py`의 `Settings` 사용, `.env` 로드
- **API 라우트**: `app/api/routes/*.py` — `APIRouter` + `response_model`
- **스키마**: `app/schemas/` — Pydantic `BaseModel`, camelCase alias (`alias="tourContext"`)

```python
# ✅ Good: Pydantic 스키마 (alias for JSON camelCase)
class TourGuideChatRequest(BaseModel):
    tour_context: str = Field(..., alias="tourContext")
    history: list[ChatMessage] = Field(default_factory=list)
    model_config = {"populate_by_name": True}

# ✅ Good: 라우트 - 서비스 주입, response_model
@router.post("/chat", response_model=TourGuideChatResponse)
async def chat(request: TourGuideChatRequest) -> TourGuideChatResponse:
    service = TourGuideService()
    return service.chat(tour_context=request.tour_context, history=request.history)
```

### Java (Spring Boot)

- **Lombok**: `@RequiredArgsConstructor`, `@Sl4j`, `@Getter`/`@Setter` 등 적극 활용
- **Controller**: `@RestController` + `@RequestMapping("/api/v1/...")`, `ResponseEntity<T>`
- **Validation**: `@Valid` + `@RequestBody`/`@RequestParam`
- **OpenAPI**: `@Operation`, `@SecurityRequirement` 명시
- **패키지**: `com.app.questofseoul.{controller|service|repository|domain|dto|exception}`

```java
// ✅ Good: Controller 패턴
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "투어", description = "Tour API")
public class TourController {
    private final TourDetailService tourDetailService;

    @Operation(summary = "투어 디테일 조회")
    @GetMapping("/tours/{tourId}")
    public ResponseEntity<TourDetailResponse> getTourDetail(@PathVariable Long tourId) {
        return ResponseEntity.ok(tourDetailService.getTourDetail(tourId, userId));
    }
}
```

### TypeScript / React (Frontend)

- **함수형 컴포넌트** + hooks
- **폼**: React Hook Form + Zod resolver
- **데이터 페칭**: TanStack Query
- **스타일**: Tailwind CSS (설정 시) 또는 인라인/모듈
- **camelCase**: JSON/API 통신 시 camelCase 유지

---

## 5. API Conventions

### REST 규칙

- **Base path**: `/api/v1` (Spring Boot), FastAPI는 prefix 없음 (`/tour-guide`, `/health`)
- **인증**: JWT Bearer (`bearerAuth`) 또는 세션 (`sessionAuth`) — Swagger `@SecurityRequirement` 참조
- **에러 응답**: Spring `GlobalExceptionHandler` 패턴 따르기

### 주요 API 엔드포인트

| Method | Path | 설명 |
|--------|------|------|
| GET | `/api/v1/tours` | 투어 목록 (thumbnailUrl, counts, tags 포함) |
| GET | `/api/v1/tours/{tourId}` | 투어 디테일 (mapSpots에 썸네일 통합) |
| POST | `/api/v1/tours/{tourId}/runs` | RUN 시작/계속 (body: `{ mode: START\|CONTINUE }`) |
| POST | `/api/v1/tour-runs/{runId}/proximity` | 근접 감지 (단일 message 반환 + nextApi 체이닝) |
| GET | `/api/v1/tour-runs/{runId}/spots/{spotId}/chat-session` | 채팅 세션 획득 |
| GET | `/api/v1/chat-sessions/{sessionId}/turns` | 채팅 히스토리 (nextScriptApi, hasNextScript 포함) |
| POST | `/api/v1/chat-sessions/{sessionId}/messages` | 채팅 메시지 전송 |
| GET | `/api/v1/spots/{spotId}/detail` | 스팟 상세 |
| GET | `/api/v1/spots/{spotId}/guide` | 스팟 가이드 (assets 용어 사용) |
| POST | `/api/v1/tour-runs/{runId}/missions/{stepId}/submit` | 미션 제출 |
| POST | `/api/v1/tour-runs/{runId}/treasures/{spotId}/collect` | 보물 수집 |

### 필드 네이밍 규칙

- **좌표**: `lat` / `lng` (latitude/longitude가 아닌 축약형 사용)
- **진행률**: `completedCount` / `totalCount` / `completedSpotIds`
- **미디어**: `assets` (media가 아닌 assets 용어 통일)
- **텍스트**: `text` (textEn, textKo 등이 아닌 language 파라미터로 분리)
- **순차 재생**: `delayMs` (밀리초 단위 딜레이), `nextApi` (다음 턴 API 경로)
- **RUN 모드**: `START` / `CONTINUE` (RESTART 없음)

### 환경변수 (각 서비스 `.env`)

| 변수 | 설명 | 서비스 |
|------|------|--------|
| `OPENAI_API_KEY` | OpenAI API 키 | ai-server |
| `DATA_GO_KR_SERVICE_KEY` | 한국관광공사 Tour API | ai-server |
| `DATABASE_URL` | PostgreSQL (Pgvector, RAG용) | ai-server |
| `DB_URL` | PostgreSQL JDBC URL | Spring Boot |
| `DB_USERNAME` / `DB_PASSWORD` | PostgreSQL 계정 | Spring Boot |
| `PORT` | 서버 포트 (기본 8000) | ai-server |
| `SERVER_PORT` | 서버 포트 (기본 8080) | Spring Boot |

---

## 6. Do's and Don'ts

### Do

- Pydantic `alias`로 API 요청/응답 **camelCase** 유지
- Spring Controller는 `ResponseEntity<T>` 반환
- 새 Retriever 추가 시 `app/services/rag/retrievers/` 패턴 따르기
- 엔티티/스키마 변경 시 연관 Repository, Service, DTO 함께 확인
- `.env.example`에 새 환경변수 문서화
- 수정한 파일 위주로 린트/포맷/타입체크 실행

### Don't

- `OPENAI_API_KEY`, `DATA_GO_KR_SERVICE_KEY`, `DATABASE_URL`, `DB_URL`, `DB_PASSWORD` 등 **시크릿 하드코딩 금지**
- `node_modules/`, `ai-server/.venv/`, `spring-boot/questofseoul/build/` 직접 수정 금지
- 전체 빌드/테스트 없이 대규모 리팩터링 하지 말 것
- 패키지 추가 (`pip install`, `npm install`, Gradle dependency) — **사용자 승인 후**
- Git `push`, `force push`, `rebase` — **사용자 승인 후**
- 기존 RAG Retriever 로직을 API 키 없이 동작하도록 설계 (키 없으면 skip)

---

## 7. Boundaries (경계)

### ✅ 항상 수행

- 수정한 파일에 대해 린트, 포맷, 타입체크(가능 시) 실행
- 기존 코드 스타일 및 패턴 유지
- 한국어 주석/로그 사용 (프로젝트 언어)

### ⚠️ 먼저 물어보기

- 패키지/의존성 추가
- Git push, rebase, force push
- 데이터베이스 스키마/마이그레이션 변경
- `.env`, CI/CD 설정 수정
- 대규모 리팩터링 (여러 파일 동시 수정)

### 🚫 절대 금지

- 시크릿/API 키 코드에 포함
- `vendor/`, `node_modules/`, `.venv/`, `build/` 직접 편집
- 테스트를 삭제하거나 실패하는 상태로 두기 (실패 시 수정 또는 사용자에게 보고)
- 사용자 승인 없이 `git push` 실행

---

## 8. PR Checklist

- [ ] 커밋 메시지: `feat(scope): short description` 형식
- [ ] 수정 영역별: 린트, 타입체크, 단위 테스트 통과
- [ ] diff는 작고 집중 — 변경 사항 요약 포함
- [ ] 불필요한 로그/주석 제거
- [ ] 시크릿/민감 정보 포함 여부 확인

---

## 9. When Stuck (막혔을 때)

- 모호한 요구사항: **명확한 질문** 또는 **짧은 계획 제안** 후 진행
- 큰 추측적 변경이 필요할 때: **사용자 확인 후** 진행
- 기존 패턴과 충돌: `AGENTS.md` 및 관련 소스 참조 후 일관되게 적용

---

## 10. Good & Bad Examples

### Python - Retriever

```python
# ✅ Good: 설정 체크 후 skip, 명확한 인터페이스
def retrieve(self, query: str, context: str) -> str | None:
    if not self._settings.is_tour_api_configured:
        return None
    # ...

# ❌ Bad: API 키 하드코딩, 예외 무시
def retrieve(self, q):
    r = requests.get(url, params={"key": "hardcoded"})
    return r.json()
```

### Java - Controller

```java
// ✅ Good: DTO 반환, @Valid, @Operation
@PostMapping("/tours/{tourId}/runs")
public ResponseEntity<RunResponse> handleRun(
    @PathVariable Long tourId,
    @Valid @RequestBody RunRequest request) {
    return ResponseEntity.ok(tourRunService.handleRun(tourId, userId, request));
}

// ❌ Bad: raw Map, validation 없음
@PostMapping("/runs")
public Map<String, Object> run(@RequestBody Map m) { ... }
```

### React - Form

```tsx
// ✅ Good: React Hook Form + Zod
const schema = z.object({ title: z.string().min(1) });
const { register, handleSubmit } = useForm({
  resolver: zodResolver(schema),
});
```

---

*이 문서는 프로젝트 변경에 따라 주기적으로 업데이트됩니다. 새 워크플로우나 패턴이 생기면 AGENTS.md에 반영하세요.*
