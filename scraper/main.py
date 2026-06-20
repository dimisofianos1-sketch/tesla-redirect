#!/usr/bin/env python3
"""
PosoKanei4Real — Daily price scraper agent.
Runs all supermarket scrapers concurrently and uploads results to Firebase.
"""
import json
import logging
import os
import sys
from concurrent.futures import ThreadPoolExecutor, as_completed
from pathlib import Path

from scrapers import ALL_SCRAPERS
from firebase_uploader import upload_results
from models import ScrapeResult

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s: %(message)s",
    datefmt="%Y-%m-%dT%H:%M:%S",
)
logger = logging.getLogger("main")

CATALOG_PATH = Path(__file__).parent.parent / "data" / "grocery_catalog.json"
MAX_WORKERS = int(os.environ.get("SCRAPER_WORKERS", "4"))


def load_all_items(catalog_path: Path) -> list[dict]:
    """Flatten catalog JSON into a list of items with category context."""
    with catalog_path.open(encoding="utf-8") as f:
        catalog = json.load(f)
    items = []
    for category in catalog["categories"]:
        for item in category["items"]:
            items.append({**item, "category_id": category["id"]})
    return items


def run_scraper(scraper_class, items: list[dict]) -> ScrapeResult:
    scraper = scraper_class()
    logger.info("▶ Starting %s", scraper.supermarket_id)
    result = scraper.scrape_catalog(items)
    logger.info(
        "■ Done %s: %d prices / %d errors",
        scraper.supermarket_id,
        len(result.records),
        len(result.errors),
    )
    return result


def main():
    logger.info("=== PosoKanei4Real scraper starting ===")

    items = load_all_items(CATALOG_PATH)
    logger.info("Loaded %d items from catalog", len(items))

    all_results: list[ScrapeResult] = []

    with ThreadPoolExecutor(max_workers=MAX_WORKERS) as pool:
        futures = {
            pool.submit(run_scraper, cls, items): cls for cls in ALL_SCRAPERS
        }
        for future in as_completed(futures):
            cls = futures[future]
            try:
                result = future.result()
                all_results.append(result)
            except Exception as exc:
                logger.error("Scraper %s crashed: %s", cls.__name__, exc)

    total_prices = sum(len(r.records) for r in all_results)
    total_errors = sum(len(r.errors) for r in all_results)
    logger.info(
        "Scraping complete: %d prices collected, %d errors across %d supermarkets",
        total_prices,
        total_errors,
        len(all_results),
    )

    if os.environ.get("DRY_RUN") == "1":
        logger.info("DRY_RUN=1 — skipping Firebase upload")
        _print_sample(all_results)
        return

    written = upload_results(all_results)
    logger.info("=== Done. %d records written to Firebase ===", written)


def _print_sample(all_results: list[ScrapeResult]):
    for result in all_results[:2]:
        logger.info("Sample from %s:", result.supermarket_id)
        for record in result.records[:3]:
            logger.info("  %s → %.2f€", record.item_id, record.price)


if __name__ == "__main__":
    main()
