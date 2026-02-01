from __future__ import annotations

import os
from dataclasses import dataclass
from pathlib import Path


@dataclass(frozen=True)
class Settings:
    database_url: str
    jwt_secret: str
    jwt_algorithm: str
    jwt_expires_minutes: int
    uploads_dir: str

    @staticmethod
    def from_env() -> "Settings":
        base_dir = Path(__file__).resolve().parents[1]
        default_db_path = (base_dir / "photoping.db").as_posix()
        default_uploads_dir = (base_dir / "uploads").as_posix()
        return Settings(
            database_url=os.getenv("PHOTOPING_DATABASE_URL", f"sqlite:///{default_db_path}"),
            jwt_secret=os.getenv("PHOTOPING_JWT_SECRET", "dev-secret-change-me"),
            jwt_algorithm=os.getenv("PHOTOPING_JWT_ALGORITHM", "HS256"),
            jwt_expires_minutes=int(os.getenv("PHOTOPING_JWT_EXPIRES_MINUTES", "43200")),
            uploads_dir=os.getenv("PHOTOPING_UPLOADS_DIR", default_uploads_dir),
        )
