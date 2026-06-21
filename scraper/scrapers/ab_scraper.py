import logging
from typing import Optional

from models.price_record import PriceRecord
from .base_scraper import BaseScraper

logger = logging.getLogger(__name__)

AB_SEARCH_API = "https://www.ab.gr/api/2.0/rest/activeCarrouselRetrieval/search"


class ABScraper(BaseScraper):
    """AB Βασιλόπουλος scraper using their public search API."""

    supermarket_id = "ab"
    base_url = "https://www.ab.gr"

    def search_item(self, item_id: str, query_el: str) -> Optional[PriceRecord]:
        # AB exposes a JSON search endpoint used by their website
        params = {
            "query": query_el,
            "lang": "el",
            "currentPage": 0,
            "pageSize": 5,
            "sort": "relevance",
        }
        resp = self.session.get(AB_SEARCH_API, params=params, timeout=20)
        if not resp.ok:
            return None
        try:
            data = resp.json()
            products = data.get("products", [])
            if not products:
                return None
            p = products[0]
            price_info = p.get("price", {})
            price_val = price_info.get("value") or price_info.get("formattedValue", "")
            if isinstance(price_val, str):
                price_val = self.parse_price(price_val)
            if price_val is None:
                return None
            original = price_info.get("originalValue")
            return PriceRecord(
                item_id=item_id,
                supermarket_id=self.supermarket_id,
                price=float(price_val),
                is_on_sale=bool(original and original != price_val),
                original_price=float(original) if original else None,
                product_url=f"{self.base_url}{p.get('url', '')}",
                product_name_found=p.get("name", ""),
            )
        except (KeyError, ValueError, AttributeError) as exc:
            logger.debug("AB parse error for %s: %s", item_id, exc)
            return None
