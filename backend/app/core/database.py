"""
MANO Database Configuration.
Async SQLAlchemy engine with SQLite for development.

WHY ASYNC?
Our FastAPI endpoints are async. If we use a regular (sync) database connection,
every DB query blocks the event loop and prevents other requests from being processed.
Async DB I/O lets the server handle multiple requests concurrently — e.g., one request
is waiting for DB while another runs GPU inference.

TO SWITCH TO POSTGRESQL IN PRODUCTION:
Change DATABASE_URL to:
    "postgresql+asyncpg://user:pass@host:5432/mano"
And install: pip install asyncpg
"""
from sqlalchemy.ext.asyncio import create_async_engine, AsyncSession, async_sessionmaker
from sqlalchemy.orm import DeclarativeBase
from pathlib import Path

# SQLite file lives next to the app directory
_db_path = Path(__file__).resolve().parent.parent / "mano.db"
DATABASE_URL = f"sqlite+aiosqlite:///{_db_path}"

# The engine is the connection pool — it manages a pool of database connections.
engine = create_async_engine(
    DATABASE_URL,
    echo=False,  # Set True to see raw SQL in logs (noisy but useful for debugging)
)

# The session factory creates new sessions (one per request).
async_session = async_sessionmaker(engine, class_=AsyncSession, expire_on_commit=False)


class Base(DeclarativeBase):
    """
    All ORM models inherit from this.
    SQLAlchemy uses it to track which tables need to be created.
    """
    pass


async def create_tables():
    """
    Creates all tables that don't exist yet.
    Called once during app startup (in main.py lifespan).
    """
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)


async def get_db():
    """
    FastAPI dependency that provides a database session.
    
    Usage in a route:
        @router.get("/patients")
        async def list_patients(db: AsyncSession = Depends(get_db)):
            ...
    
    The session is automatically closed after the request finishes.
    """
    async with async_session() as session:
        yield session
