from __future__ import annotations

import datetime as dt

from jose import JWTError, jwt
from passlib.context import CryptContext


pwd_context = CryptContext(schemes=["pbkdf2_sha256"], deprecated="auto")


def hash_password(password: str) -> str:
    return pwd_context.hash(password)


def verify_password(password: str, password_hash: str) -> bool:
    return pwd_context.verify(password, password_hash)


def create_access_token(*, subject: str, secret: str, algorithm: str, expires_minutes: int) -> str:
    now = dt.datetime.now(dt.timezone.utc)
    exp = now + dt.timedelta(minutes=expires_minutes)
    to_encode = {"sub": subject, "iat": int(now.timestamp()), "exp": exp}
    return jwt.encode(to_encode, secret, algorithm=algorithm)


def decode_token(*, token: str, secret: str, algorithm: str) -> dict:
    try:
        return jwt.decode(token, secret, algorithms=[algorithm])
    except JWTError as e:
        raise ValueError("Invalid token") from e
