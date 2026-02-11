"""Environment Variables"""
from functools import lru_cache
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    """Application Settings (env: OPENAI_API_KEY, OPENAI_MODEL, PORT)"""

    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        extra="ignore",
        env_prefix="",  # OPENAI_API_KEY, etc.
    )

    # Server
    port: int = 8081
    host: str = "0.0.0.0"

    # OpenAI
    openai_api_key: str = ""
    openai_model: str = "gpt-4o-mini"

    @property
    def is_openai_configured(self) -> bool:
        return bool(self.openai_api_key.strip())


@lru_cache
def get_settings() -> Settings:
    return Settings()
