import logging
from typing import Optional

from models.price_record import PriceRecord
from .base_scraper import BaseScraper

logger = logging.getLogger(__name__)


class ChalkiadakisScraper(BaseScraper):
    """Χαλκιαδάκης supermarket scraper — Crete-focused chain."""

    supermarket_id = "chalkiadakis"
    base_url = "https://www.chalkiadakis.gr"

    def search_item(self, item_id: str, query_el: str) -> Optional[PriceRecord]:
        url = f"{self.base_url}/search?q={query_el}"
        soup = self.get_soup(url)
        if soup is None:
            return None

        import json
        for script in soup.find_all("script", type="application/ld+json"):
            try:
                obj = json.loads(script.string or "")
                if isinstance(obj, dict) and obj.get("@type") == "Product":
                    offers = obj.get("offers", {})
                    price = offers.get("price")
                    if price:
                        return PriceRecord(
                            item_id=item_id,
                            supermarket_id=self.supermarket_id,
                            price=float(price),
                            product_name_found=obj.get("name", ""),
                        )
            except (json.JSONDecodeError, KeyError, TypeError):
                continue

        card = soup.select_one(".product, .product-item, article")
        if not card:
            return None
        price_el = card.select_one("[class*='price'], .price")
        if not price_el:
            return None
        price = self.parse_price(price_el.get_text())
        if price is None:
            return None
        name_el = card.select_one("h2, h3, .name")
        return PriceRecord(
            item_id=item_id,
            supermarket_id=self.supermarket_id,
            price=price,
            product_name_found=name_el.get_text(strip=True) if name_el else "",
        )
