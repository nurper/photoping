from typing import Tuple

from sqlalchemy import create_engine
from sqlalchemy.engine import Engine
from sqlalchemy.orm import Session, sessionmaker

from photoping.models import Base


def create_engine_and_sessionmaker(database_url: str) -> Tuple[Engine, sessionmaker]:
    connect_args = {"check_same_thread": False} if database_url.startswith("sqlite") else {}
    engine = create_engine(database_url, connect_args=connect_args)
    SessionLocal = sessionmaker(bind=engine, autoflush=False, autocommit=False)
    return engine, SessionLocal


def init_db(engine: Engine) -> None:
    Base.metadata.create_all(bind=engine)
