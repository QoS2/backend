"""Tour Guide AI Service"""
import logging
from openai import OpenAI

from app.config import get_settings
from app.schemas.tour_guide import ChatMessage, TourGuideChatResponse

logger = logging.getLogger(__name__)

SYSTEM_PROMPT = """당신은 한국의 역사와 문화에 정통한 'Quest of Seoul' AI 투어 가이드입니다.
사용자가 방문 중인 투어와 관련된 질문에 친근하고 전문적으로 답변하세요.
답변은 2~4문장 정도로 간결하게, 한국어로 작성하세요.
제공된 투어/스텝/가이드 정보를 활용해 정확하고 흥미로운 답변을 해주세요.
정보가 없는 경우 일반적인 한국 역사·문화 지식으로 답변할 수 있습니다."""

DISABLED_MESSAGE = "AI 가이드가 비활성화되어 있습니다. ai-server에 OPENAI_API_KEY를 설정해주세요."
ERROR_MESSAGE = "죄송합니다. AI 응답 생성 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요."
EMPTY_MESSAGE = "답변을 생성할 수 없습니다."


class TourGuideService:
    """Tour Guide AI Chat Service"""

    def __init__(self) -> None:
        self._settings = get_settings()

    def chat(self, tour_context: str, history: list[ChatMessage]) -> TourGuideChatResponse:
        """
        Generate AI Response based on Tour Context and Conversation History
        """
        if not self._settings.is_openai_configured:
            return TourGuideChatResponse(text=DISABLED_MESSAGE)

        try:
            client = OpenAI(api_key=self._settings.openai_api_key)
            messages = [
                {
                    "role": "system",
                    "content": f"{SYSTEM_PROMPT}\n\n[투어 컨텍스트]\n{tour_context or ''}",
                },
            ]
            for h in history or []:
                role = "user" if h.role == "user" else "assistant"
                messages.append({"role": role, "content": h.content or ""})

            completion = client.chat.completions.create(
                model=self._settings.openai_model,
                messages=messages,
                max_tokens=300,
                temperature=0.7,
            )

            text = (completion.choices[0].message.content or "").strip()
            return TourGuideChatResponse(text=text or EMPTY_MESSAGE)

        except Exception as e:
            logger.exception("OpenAI API error: %s", e)
            return TourGuideChatResponse(text=ERROR_MESSAGE)
