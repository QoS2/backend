from contextlib import asynccontextmanager
import logging

from fastapi import FastAPI
from dotenv import load_dotenv

from app.config import get_settings
from app.api.routes import health, tour_guide

load_dotenv()

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(name)s - %(levelname)s - %(message)s",
)
logger = logging.getLogger(__name__)


@asynccontextmanager
async def lifespan(app: FastAPI):
    """Application Lifecycle (start/stop)"""
    logger.info("Quest of Seoul AI Server starting")
    yield
    logger.info("Quest of Seoul AI Server shutting down")


def create_app() -> FastAPI:
    """Create FastAPI Application"""
    settings = get_settings()
    app = FastAPI(
        title="Quest of Seoul AI Server",
        description="Tour Guide AI",
        version="0.1.0",
        lifespan=lifespan,
    )

    # Register Routers
    app.include_router(health.router)
    app.include_router(tour_guide.router)

    return app


app = create_app()


if __name__ == "__main__":
    import uvicorn

    settings = get_settings()
    uvicorn.run(
        "app.main:app",
        host=settings.host,
        port=settings.port,
        reload=True,
    )
