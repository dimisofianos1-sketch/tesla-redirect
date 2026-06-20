import logging
from abc import ABC, abstractmethod
from typing import Optional
import requests
from bs4 import BeautifulSoup

from models.price_record import PriceRecord, ScrapeResult
from utils.http_client import build_session, polite_get

logger = logging.getLogger(__name__)


class BaseScraper(ABC):
    supermarket_id: str = ""
    base_url: str = ""

    def __init__(self):
        self.session = build_session()

    @abstractmethod
    def search_item(self, item_id: str, query_el: str) -> Optional[PriceRecord]:
        """Search for an item by its Greek name and return a PriceRecord."""

    def scrape_catalog(self, catalog: list[dict]) -> ScrapeResult:
        result = ScrapeResult(supermarket_id=self.supermarket_id)
        for item in catalog:
            try:
                record = self.search_item(item["id"], item["name_el"])
                if record:
                    result.records.append(record)
                    logger.info("[%s] ✓ %s → %.2f€", self.supermarket_id, item["id"], record.price)
                else:
                    result.errors.append(f"not_found:{item['id']}")
                    logger.debug("[%s] ✗ %s not found", self.supermarket_id, item["id"])
            except Exception as exc:
                msg = f"error:{item['id']}:{exc}"
                result.errors.append(msg)
                logger.error("[%s] exception on %s: %s", self.supermarket_id, item["id"], exc)
        result.success = len(result.records) > 0
        return result

    def get_soup(self, url: str, **kwargs) -> Optional[BeautifulSoup]:
        resp = polite_get(self.session, url, **kwargs)
        if resp is None:
            return None
        return BeautifulSoup(resp.text, "lxml")

    @staticmethod
    def parse_price(text: str) -> Optional[float]:
        """Extract a float price from a string like '2,49 €' or '1.99€'."""
        import re
        cleaned = re.sub(r"[^\d,\.]", "", text.strip())
        cleaned = cleaned.replace(",", ".")
        # Handle cases like '2.49.99' → take last valid float
        parts = cleaned.split(".")
        if len(parts) > 2:
            cleaned = parts[0] + "." + "".join(parts[1:])
        try:
            return float(cleaned)
        except ValueError:
            return None
