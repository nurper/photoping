from __future__ import annotations

from pathlib import Path
from typing import Optional

from flask import Flask, jsonify, send_from_directory
from flask_cors import CORS

from photoping.config import Settings
from photoping.db import create_engine_and_sessionmaker, init_db
from photoping.deps import close_db
from photoping.routers.auth import bp as auth_bp
from photoping.routers.photo import bp as photo_bp


def create_app(settings: Optional[Settings] = None) -> Flask:
    settings = settings or Settings.from_env()

    app = Flask("photoping")
    app.config["SETTINGS"] = settings

    engine, sessionmaker_ = create_engine_and_sessionmaker(settings.database_url)
    init_db(engine)

    app.config["ENGINE"] = engine
    app.config["SessionLocal"] = sessionmaker_

    uploads_dir = Path(settings.uploads_dir)
    uploads_dir.mkdir(parents=True, exist_ok=True)
    app.config["UPLOADS_DIR"] = uploads_dir

    CORS(
        app,
        resources={r"/*": {"origins": "*"}},
        supports_credentials=False,
        allow_headers="*",
        expose_headers="*",
    )

    app.register_blueprint(auth_bp)
    app.register_blueprint(photo_bp)

    @app.get("/health")
    def health():
        return jsonify({"ok": True})

    @app.get("/uploads/<path:filename>")
    def uploads(filename: str):
        return send_from_directory(str(uploads_dir), filename)

    app.teardown_appcontext(close_db)

    return app
