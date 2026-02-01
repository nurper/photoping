from __future__ import annotations

import datetime as dt
import uuid
from pathlib import Path
from typing import Any, Dict, List

from flask import Blueprint, current_app, jsonify, request

from photoping.deps import get_db, json_error, require_current_user
from photoping.models import Photo


bp = Blueprint("photo", __name__, url_prefix="/photo")


def _external_base_url() -> str:
    proto = request.headers.get("X-Forwarded-Proto") or request.scheme
    host = request.headers.get("X-Forwarded-Host") or request.host
    return f"{proto}://{host}".rstrip("/")


def _parse_float(value: Any, name: str) -> float:
    try:
        return float(value)
    except (TypeError, ValueError):
        raise ValueError(f"Invalid {name}")


@bp.get("/by-location")
def by_location():
    db = get_db()
    _, err = require_current_user(db)
    if err is not None:
        return err

    try:
        lat = _parse_float(request.args.get("lat"), "lat")
        lon = _parse_float(request.args.get("lon"), "lon")
    except ValueError as e:
        return json_error(400, str(e))

    photos = (
        db.query(Photo)
        .filter(Photo.lat == lat)
        .filter(Photo.lon == lon)
        .order_by(Photo.created_at.desc())
        .all()
    )

    out: List[Dict[str, Any]] = []
    for p in photos:
        out.append(
            {
                "id": p.id,
                "image_url": p.image_path,
                "message": p.message,
                "created_at": p.created_at.isoformat(),
                "lat": p.lat,
                "lon": p.lon,
            }
        )
    return jsonify(out)


@bp.post("")
def upload_photo():
    db = get_db()
    user, err = require_current_user(db)
    if err is not None:
        return err

    photo = request.files.get("photo")
    if photo is None:
        return json_error(400, "Missing photo")

    if not photo.mimetype or not photo.mimetype.startswith("image/"):
        return json_error(400, "Photo must be an image")

    message = (request.form.get("message") or "").strip()
    if not message:
        return json_error(400, "Missing message")

    try:
        lat = _parse_float(request.form.get("lat"), "lat")
        lon = _parse_float(request.form.get("lon"), "lon")
    except ValueError as e:
        return json_error(400, str(e))

    uploads_dir: Path = current_app.config["UPLOADS_DIR"]
    file_id = str(uuid.uuid4())
    filename = f"{file_id}.jpg"
    target_path = uploads_dir / filename
    photo.save(target_path)

    base = _external_base_url()
    image_url = f"{base}/uploads/{filename}"

    entity = Photo(
        id=file_id,
        user_id=user.id,
        message=message,
        image_path=image_url,
        lat=lat,
        lon=lon,
        created_at=dt.datetime.now(dt.timezone.utc),
    )
    db.add(entity)
    db.commit()

    return jsonify({"id": entity.id, "image_url": image_url, "message": entity.message})


@bp.post("/url")
def create_photo_by_url():
    db = get_db()
    user, err = require_current_user(db)
    if err is not None:
        return err

    data = request.get_json(silent=True)
    data = data if isinstance(data, dict) else {}

    image_url = (data.get("image_url") or "").strip()
    message = (data.get("message") or "").strip()
    if not image_url.startswith("http"):
        return json_error(400, "image_url must be a URL")
    if not message:
        return json_error(400, "Missing message")

    try:
        lat = _parse_float(data.get("lat"), "lat")
        lon = _parse_float(data.get("lon"), "lon")
    except ValueError as e:
        return json_error(400, str(e))

    file_id = str(uuid.uuid4())
    entity = Photo(
        id=file_id,
        user_id=user.id,
        message=message,
        image_path=image_url,
        lat=lat,
        lon=lon,
        created_at=dt.datetime.now(dt.timezone.utc),
    )
    db.add(entity)
    db.commit()

    return jsonify({"id": entity.id, "image_url": entity.image_path, "message": entity.message})
