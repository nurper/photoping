from __future__ import annotations

from email_validator import EmailNotValidError, validate_email
from flask import Blueprint, jsonify, request

from photoping.deps import get_db, get_settings, json_error
from photoping.models import User
from photoping.security import create_access_token, hash_password, verify_password


bp = Blueprint("auth", __name__, url_prefix="/auth")


def _parse_json() -> dict:
    data = request.get_json(silent=True)
    return data if isinstance(data, dict) else {}


def _validate_email(email_raw: str) -> str:
    email_raw = (email_raw or "").strip()
    try:
        return validate_email(email_raw, check_deliverability=False).email
    except EmailNotValidError as e:
        raise ValueError(str(e))


@bp.post("/register")
def register():
    data = _parse_json()
    email_raw = data.get("email")
    password = data.get("password")

    if not isinstance(password, str) or len(password) < 6:
        return json_error(400, "Password must be at least 6 characters")

    try:
        email = _validate_email(email_raw)
    except ValueError as e:
        return json_error(400, str(e) or "Invalid email")

    db = get_db()
    existing = db.query(User).filter(User.email == email).first()
    if existing is not None:
        return json_error(400, "Email already registered")

    user = User(email=email, password_hash=hash_password(password))
    db.add(user)
    db.commit()

    return jsonify({"message": "ok"})


@bp.post("/login")
def login():
    data = _parse_json()
    email_raw = data.get("email")
    password = data.get("password")

    if not isinstance(password, str) or not password:
        return json_error(401, "Invalid email or password")

    try:
        email = _validate_email(email_raw)
    except ValueError:
        return json_error(401, "Invalid email or password")

    db = get_db()
    user = db.query(User).filter(User.email == email).first()
    if user is None or not verify_password(password, user.password_hash):
        return json_error(401, "Invalid email or password")

    settings = get_settings()
    token = create_access_token(
        subject=user.id,
        secret=settings.jwt_secret,
        algorithm=settings.jwt_algorithm,
        expires_minutes=settings.jwt_expires_minutes,
    )
    return jsonify({"access_token": token, "token_type": "bearer"})
