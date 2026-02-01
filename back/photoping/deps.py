from __future__ import annotations

from typing import Optional, Tuple

from flask import current_app, g, jsonify, request
from sqlalchemy.orm import Session

from photoping.config import Settings
from photoping.models import User
from photoping.security import decode_token


def get_settings() -> Settings:
    return current_app.config["SETTINGS"]


def get_db() -> Session:
    db: Optional[Session] = getattr(g, "db", None)
    if db is None:
        sessionmaker_ = current_app.config["SessionLocal"]
        db = sessionmaker_()
        g.db = db
    return db


def close_db(_: Optional[BaseException] = None) -> None:
    db: Optional[Session] = getattr(g, "db", None)
    if db is not None:
        db.close()
        g.db = None


def json_error(status_code: int, detail: str):
    return jsonify({"detail": detail}), status_code


def require_current_user(db: Session) -> Tuple[Optional[User], Optional[tuple]]:
    auth = request.headers.get("Authorization") or ""
    if not auth.lower().startswith("bearer "):
        return None, json_error(401, "Missing bearer token")

    token = auth.split(None, 1)[1].strip()
    if not token:
        return None, json_error(401, "Missing bearer token")

    settings = get_settings()
    try:
        payload = decode_token(token=token, secret=settings.jwt_secret, algorithm=settings.jwt_algorithm)
    except ValueError:
        return None, json_error(401, "Invalid token")

    user_id = payload.get("sub")
    if not user_id:
        return None, json_error(401, "Invalid token")

    user = db.query(User).filter(User.id == user_id).first()
    if user is None:
        return None, json_error(401, "User not found")
    return user, None
