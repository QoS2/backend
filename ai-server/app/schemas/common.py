"""Common Schemas"""
from pydantic import BaseModel, Field


class HealthResponse(BaseModel):
    """Health Check Response"""

    status: str = Field(default="ok", description="Service Status")
