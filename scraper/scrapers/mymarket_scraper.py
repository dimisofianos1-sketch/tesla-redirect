import logging
from typing import Optional

from models.price_record import PriceRecord
from .base_scraper import BaseScraper

logger = logging.getLogger(__name__)

MYMARKET_SEARCH_API = "https://www.mymarket.gr/api/search"


class MyMarketScraper(BaseScraper):
    """My Market scraper (mymarket.gr)."""

    supermarket_id = "mymarket"
    base_url = "https://www.mymarket.gr"

    def search_item(self, item_id: str, query_el: str) -> Optional[PriceRecord]:
        params = {"q": query_el, "size": 5}
        resp = self.session.get(MYMARKET_SEARCH_API, params=params, timeout=20)
        if resp.ok:
            try:
                data = resp.json()
                products = (
                    data.get("products")
                    or data.get("results")
                    or data.get("items")
                    or []
                )
                if products:
                    p = products[0]
                    price = p.get("price") or p.get("finalPrice") or p.get("salePrice")
                    if price is not None:
                        orig = p.get("originalPrice") or p.get("regularPrice")
                        return PriceRecord(
                            item_id=item_id,
                            supermarket_id=self.supermarket_id,
                            price=float(price),
                            is_on_sale=bool(orig and float(orig) > float(price)),
                            original_price=float(orig) if orig else None,
                            product_name_found=p.get("name") or p.get("title", ""),
                            product_url=self.base_url + (p.get("url") or ""),
                        )
            except (ValueError, KeyError, AttributeError) as exc:
                logger.debug("MyMarket parse error %s: %s", item_id, exc)

        # HTML fallback
        url = f"{self.base_url}/search/?q={query_el}"
        soup = self.get_soup(url)
        if soup is None:
            return None
        card = soup.select_one(".product-item, .product-card, .product")
        if not card:
            return None
        price_el = card.select_one("[class*='price'], .price")
        if not price_el:
            return None
        price = self.parse_price(price_el.get_text())
        return PriceRecord(
            item_id=item_id,
            supermarket_id=self.supermarket_id,
            price=price,
        ) if price else None
