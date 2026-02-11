"""Tour Guide Chat Route"""
from fastapi import APIRouter

from app.schemas.tour_guide import TourGuideChatRequest, TourGuideChatResponse
from app.services.tour_guide import TourGuideService

router = APIRouter(prefix="/tour-guide", tags=["tour-guide"])


@router.post("/chat", response_model=TourGuideChatResponse)
async def chat(request: TourGuideChatRequest) -> TourGuideChatResponse:
    """
    Tour Guide AI Chat

    - **tourContext**: Tour/Step/Guide Information (camelCase)
    - **history**: Previous Conversation [{ role, content }]
    """
    service = TourGuideService()
    return service.chat(
        tour_context=request.tour_context,
        history=request.history,
    )
