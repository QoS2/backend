"""Tour Guide Chat Schema"""
from pydantic import BaseModel, Field


class ChatMessage(BaseModel):
    """Chat Message"""

    role: str = Field(..., description="user | assistant")
    content: str = Field(default="", description="Message Content")


class TourGuideChatRequest(BaseModel):
    """Tour Guide Chat Request"""

    tour_context: str = Field(
        default="",
        alias="tourContext",
        description="Tour Context (Step, Guide Information, etc.)",
    )
    history: list[ChatMessage] = Field(
        default_factory=list,
        description="Conversation History",
    )

    model_config = {"populate_by_name": True}


class TourGuideChatResponse(BaseModel):
    """Tour Guide Chat Response"""

    text: str = Field(..., description="AI Generated Response")
