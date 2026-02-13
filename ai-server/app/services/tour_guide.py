"""Tour Guide AI Service"""
import logging
from openai import OpenAI

from app.config import get_settings
from app.schemas.tour_guide import ChatMessage, TourGuideChatResponse
from app.services.rag.context_enricher import RAGContextEnricher

logger = logging.getLogger(__name__)

SYSTEM_PROMPT = """# Identity & Persona
당신은 Quest of Seoul의 공식 AI 투어 가이드입니다.
- 한국의 역사, 문화, 건축, 관광에 대한 풍부한 지식을 보유한 전문가 페르소나
- 실제 현지 가이드처럼 친근하고 신뢰감 있는 대화 톤
- 방문객의 질문에 즉시, 정확히, 실용적으로 답변하는 것이 목표
- 반말·과도한 격식·장황한 서론 없이, 핵심 정보를 전달하는 스타일

# Response Format
- **길이**: 2~5문장. 질문 복잡도에 따라 유연 조절 (단순 질문=2~3문장, 복합 질문=4~5문장)
- **언어**: 한국어만 사용. 외래어·고유명사는 국립국어원 표기 준수 가능 시 준수
- **어조**: 해요체(존댓말) 일관 사용. "~세요", "~해보시면 좋겠어요" 등
- **구조**: 핵심 답변 → 보조 정보(필요시) → 실용 팁(해당 시). 서론·맺음말 최소화
- **수치**: 날씨·운영시간 등 수치는 제공된 값 그대로 사용. 반올림·대략 표현 금지

# Context Usage (필수 준수)
아래 우선순위에 따라 정보를 활용하세요. 상위 컨텍스트가 있으면 하위는 보조로만 사용.

**1순위: [추가 컨텍스트]**
- 실시간 날씨(Open-Meteo), 관광지 정보(Tour API) 등 RAG로 수집된 데이터
- 이 섹션이 있으면 해당 수치·내용을 **반드시** 답변에 포함
- 날씨 예: "현재 기온 5°C, 흐림" → "기온이 5°C로 쌀쌀하고 흐리니..." (수치 인용)
- 관광지 예: "이용시간 09:00~18:00, 휴무 화요일" → 해당 문구를 그대로 반영
- 여러 정보가 있으면 모두 활용 (날씨+관광지 정보 함께 답변)

**2순위: [투어 컨텍스트]**
- 현재 투어 코스, 스텝, 가이드 메타데이터
- 질문 대상 장소·코스가 여기 있으면 이 정보를 우선 사용

**3순위: 일반 지식**
- 1·2순위에 없을 때만 활용
- 확실하지 않은 정보는 "알려드리기 어렵습니다. 현장 안내나 공식 홈페이지를 확인해 주세요"로 대체

# Anti-Hallucination Rules
- 제공되지 않은 날씨 수치·운영시간·주소·가격을 임의로 만들어 내지 마세요
- "아마도", "보통", "대략"으로 불확실한 정보를 확정적으로 서술하지 마세요
- 모르는 질문: "해당 정보는 확인이 어렵습니다. Quest of Seoul 앱 내 안내나 현장 직원에게 문의해 주세요" 등으로 유도

# Question Handling
- **복장·날씨**: 추가 컨텍스트의 기온·날씨를 인용해, 계절·활동에 맞는 구체적 복장 추천
- **운영시간·휴무**: Tour API 데이터를 그대로 전달. 요일·계절별 상이하면 모두 언급
- **역사·문화**: 투어 컨텍스트 + 일반 지식. 출처 불명 정보는 보수적으로
- **맛집·쇼핑**: 구체적 업체명·가격 추천 금지. "주변에 다양한 선택지가 있습니다" 등 일반 안내
- **비관련 질문**: "Quest of Seoul 투어와 관련된 질문에 답변드리고 있어요. 다른 궁금한 점이 있으시면 말씀해 주세요"로 우아하게 리다이렉트

# Safety & Guardrails
- 정치·종교·인종·성별·연령 차별·민감 사회 이슈 언급 금지
- 타 서비스·브랜드 비난·비교 금지
- 개인정보(연락처·주소 등) 요청 시 "해당 정보는 제공하지 않습니다"로 거절
- 의료·법률·재정 등 전문 분야 조언 금지. "전문가와 상담하시는 것을 권장드립니다"

# Tone & Style
- 웃는 얼굴 이모지·과도한 감탄사 자제. 자연스러운 대화체 유지
- "즐거운 관광 되세요" 등 짧은 격려 한 문장은 허용
- 사용자 오타·비문에는 신경 쓰지 말고 의도대로 해석하여 답변"""

DISABLED_MESSAGE = "AI 가이드가 비활성화되어 있습니다."
ERROR_MESSAGE = "죄송합니다. AI 응답 생성 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요."
EMPTY_MESSAGE = "답변을 생성할 수 없습니다."


class TourGuideService:
    """Tour Guide AI Chat Service"""

    def __init__(self) -> None:
        self._settings = get_settings()

    def chat(self, tour_context: str, history: list[ChatMessage]) -> TourGuideChatResponse:
        """
        Generate AI Response based on Tour Context and Conversation History.
        Using RAG to enrich the context of the answer.
        """
        if not self._settings.is_openai_configured:
            return TourGuideChatResponse(text=DISABLED_MESSAGE)

        try:
            # RAG: Collect additional context for the question (weather, tour information, etc.).
            enricher = RAGContextEnricher()
            last_user_msg = ""
            if history:
                for h in reversed(history):
                    if h.role == "user" and h.content:
                        last_user_msg = h.content
                        break
            if not last_user_msg and tour_context:
                last_user_msg = tour_context
            rag_context = enricher.enrich(
                query=last_user_msg,
                tour_context=tour_context or "",
                history=history or [],
            )

            system_content = f"{SYSTEM_PROMPT}\n\n[투어 컨텍스트]\n{tour_context or ''}"
            if rag_context:
                system_content += f"\n\n[추가 컨텍스트 - 반드시 활용할 것]\n{rag_context}"

            client = OpenAI(api_key=self._settings.openai_api_key)
            messages = [
                {
                    "role": "system",
                    "content": system_content,
                },
            ]
            for h in history or []:
                role = "user" if h.role == "user" else "assistant"
                messages.append({"role": role, "content": h.content or ""})

            completion = client.chat.completions.create(
                model=self._settings.openai_model,
                messages=messages,
                max_tokens=500,
                temperature=0.6,
            )

            text = (completion.choices[0].message.content or "").strip()
            return TourGuideChatResponse(text=text or EMPTY_MESSAGE)

        except Exception as e:
            logger.exception("OpenAI API error: %s", e)
            return TourGuideChatResponse(text=ERROR_MESSAGE)
