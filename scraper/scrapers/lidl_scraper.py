import logging
from typing import Optional

from models.price_record import PriceRecord
from .base_scraper import BaseScraper

logger = logging.getLogger(__name__)


class LidlScraper(BaseScraper):
    """Lidl Greece scraper (lidl.gr)."""

    supermarket_id = "lidl"
    base_url = "https://www.lidl.gr"

    def search_item(self, item_id: str, query_el: str) -> Optional[PriceRecord]:
        url = f"{self.base_url}/q/{query_el}"
        soup = self.get_soup(url)
        if soup is None:
            return None
        # Lidl renders pricing in structured JSON-LD or in data attributes
        import json
        for script in soup.find_all("script", type="application/ld+json"):
            try:
                obj = json.loads(script.string or "")
                if isinstance(obj, list):
                    obj = obj[0]
                if obj.get("@type") in ("Product", "ItemList"):
                    items = obj.get("itemListElement", [obj])
                    for item in items:
                        product = item.get("item", item)
                        offers = product.get("offers", {})
                        price = offers.get("price") or offers.get("lowPrice")
                        if price:
                            return PriceRecord(
                                item_id=item_id,
                                supermarket_id=self.supermarket_id,
                                price=float(price),
                                product_name_found=product.get("name", ""),
                                product_url=product.get("url", ""),
                            )
            except (json.JSONDecodeError, KeyError, TypeError):
                continue

        # Fallback: parse HTML
        card = soup.select_one(".product, [class*='product-card'], article")
        if card is None:
            return None
        price_el = card.select_one("[class*='price'], .price")
        if price_el is None:
            return None
        price = self.parse_price(price_el.get_text())
        return PriceRecord(
            item_id=item_id,
            supermarket_id=self.supermarket_id,
            price=price,
        ) if price else None
