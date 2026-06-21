import logging
from typing import Optional

from models.price_record import PriceRecord
from .base_scraper import BaseScraper

logger = logging.getLogger(__name__)

SKLAVENITIS_SEARCH = "https://www.sklavenitis.gr/api/search"


class SklavenitissScraper(BaseScraper):
    """Σκλαβενίτης scraper using their search endpoint."""

    supermarket_id = "sklavenitis"
    base_url = "https://www.sklavenitis.gr"

    def search_item(self, item_id: str, query_el: str) -> Optional[PriceRecord]:
        params = {
            "q": query_el,
            "size": 5,
            "page": 1,
        }
        resp = self.session.get(SKLAVENITIS_SEARCH, params=params, timeout=20)
        if not resp.ok:
            return self._fallback_html_search(item_id, query_el)
        try:
            data = resp.json()
            items = data.get("items") or data.get("products") or data.get("results") or []
            if not items:
                return self._fallback_html_search(item_id, query_el)
            p = items[0]
            price = (
                p.get("price")
                or p.get("salePrice")
                or p.get("priceValue")
            )
            if price is None:
                return None
            return PriceRecord(
                item_id=item_id,
                supermarket_id=self.supermarket_id,
                price=float(price),
                product_name_found=p.get("name") or p.get("title", ""),
                product_url=self.base_url + (p.get("url") or p.get("link", "")),
            )
        except (ValueError, KeyError, AttributeError) as exc:
            logger.debug("Sklavenitis parse error %s: %s", item_id, exc)
            return None

    def _fallback_html_search(self, item_id: str, query_el: str) -> Optional[PriceRecord]:
        url = f"{self.base_url}/search/?q={query_el}"
        soup = self.get_soup(url)
        if soup is None:
            return None
        card = soup.select_one(".product-item, .product-card, article.product")
        if card is None:
            return None
        price_el = card.select_one(".price, [class*='price'], .product-price")
        name_el = card.select_one(".name, h2, h3, .product-name")
        if price_el is None:
            return None
        price = self.parse_price(price_el.get_text())
        if price is None:
            return None
        return PriceRecord(
            item_id=item_id,
            supermarket_id=self.supermarket_id,
            price=price,
            product_name_found=name_el.get_text(strip=True) if name_el else "",
        )
