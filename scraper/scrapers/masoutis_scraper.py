import logging
from typing import Optional

from models.price_record import PriceRecord
from .base_scraper import BaseScraper

logger = logging.getLogger(__name__)

MASOUTIS_SEARCH_API = "https://www.masoutis.gr/api/products/search"


class MasoutisScraper(BaseScraper):
    """Μασούτης scraper (masoutis.gr)."""

    supermarket_id = "masoutis"
    base_url = "https://www.masoutis.gr"

    def search_item(self, item_id: str, query_el: str) -> Optional[PriceRecord]:
        # Try API endpoint first
        params = {"term": query_el, "page": 1, "pageSize": 5}
        resp = self.session.get(MASOUTIS_SEARCH_API, params=params, timeout=20)
        if resp.ok:
            try:
                data = resp.json()
                products = data.get("products") or data.get("items") or []
                if products:
                    p = products[0]
                    price = p.get("price") or p.get("salePrice")
                    if price:
                        return PriceRecord(
                            item_id=item_id,
                            supermarket_id=self.supermarket_id,
                            price=float(price),
                            product_name_found=p.get("name", ""),
                        )
            except (ValueError, KeyError):
                pass

        # Fallback to HTML search
        url = f"{self.base_url}/search?q={query_el}"
        soup = self.get_soup(url)
        if soup is None:
            return None
        card = soup.select_one(".product-item, .product, [class*='product']")
        if not card:
            return None
        price_el = card.select_one("[class*='price'], .price, .product-price")
        if not price_el:
            return None
        price = self.parse_price(price_el.get_text())
        if price is None:
            return None
        name_el = card.select_one(".name, .title, h2, h3")
        return PriceRecord(
            item_id=item_id,
            supermarket_id=self.supermarket_id,
            price=price,
            product_name_found=name_el.get_text(strip=True) if name_el else "",
        )
