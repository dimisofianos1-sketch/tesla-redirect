"""Uploads scrape results to Firebase Firestore."""
import logging
import os
from datetime import datetime, timezone
from typing import Optional

logger = logging.getLogger(__name__)


def _get_db():
    import firebase_admin
    from firebase_admin import credentials, firestore

    if not firebase_admin._apps:
        cred_path = os.environ.get("GOOGLE_APPLICATION_CREDENTIALS")
        if cred_path:
            cred = credentials.Certificate(cred_path)
        else:
            cred = credentials.ApplicationDefault()
        firebase_admin.initialize_app(cred)
    return firestore.client()


def upload_results(all_results: list) -> int:
    """Write price records to Firestore. Returns total records written."""
    try:
        db = _get_db()
    except Exception as exc:
        logger.error("Firebase init failed: %s", exc)
        return 0

    from firebase_admin import firestore as fs

    today = datetime.now(timezone.utc).strftime("%Y-%m-%d")
    batch = db.batch()
    count = 0
    BATCH_LIMIT = 450  # Firestore limit is 500 ops per batch

    for result in all_results:
        for record in result.records:
            doc_id = f"{record.item_id}__{record.supermarket_id}"
            # Current prices collection (latest snapshot, easy to read)
            current_ref = db.collection("prices_current").document(doc_id)
            batch.set(current_ref, record.to_dict())

            # Historical prices — one document per item/market/day
            hist_ref = (
                db.collection("prices_history")
                .document(record.item_id)
                .collection(record.supermarket_id)
                .document(today)
            )
            batch.set(hist_ref, record.to_dict())

            count += 1
            if count % BATCH_LIMIT == 0:
                batch.commit()
                batch = db.batch()

    if count % BATCH_LIMIT != 0:
        batch.commit()

    # Write a metadata document so the app knows when the last update was
    db.collection("metadata").document("last_update").set(
        {
            "updated_at": datetime.now(timezone.utc).isoformat(),
            "date": today,
            "records_written": count,
        }
    )

    logger.info("Uploaded %d price records to Firestore (date=%s)", count, today)
    return count
