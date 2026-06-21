import logging
from typing import Optional

from models.price_record import PriceRecord
from .base_scraper import BaseScraper

logger = logging.getLogger(__name__)


class BazaarScraper(BaseScraper):
    """Bazaar supermarket scraper (bazaar.com.gr)."""

    supermarket_id = "bazaar"
    base_url = "https://www.bazaar.com.gr"

    def search_item(self, item_id: str, query_el: str) -> Optional[PriceRecord]:
        url = f"{self.base_url}/search?q={query_el}"
        soup = self.get_soup(url)
        if soup is None:
            return None

        card = soup.select_one(
            ".product-item, .product-card, .item, article.product, li.product"
        )
        if not card:
            return None

        price_el = card.select_one(
            "[class*='price'], .price, .product-price, ins .amount"
        )
        if not price_el:
            return None

        price = self.parse_price(price_el.get_text())
        if price is None:
            return None

        old_price_el = card.select_one(".old-price, del .amount, [class*='old-price']")
        original = self.parse_price(old_price_el.get_text()) if old_price_el else None

        name_el = card.select_one("h2, h3, .name, .title, .product-name")
        link_el = card.select_one("a[href]")

        return PriceRecord(
            item_id=item_id,
            supermarket_id=self.supermarket_id,
            price=price,
            is_on_sale=original is not None,
            original_price=original,
            product_name_found=name_el.get_text(strip=True) if name_el else "",
            product_url=(
                self.base_url + link_el["href"]
                if link_el and not link_el["href"].startswith("http")
                else (link_el["href"] if link_el else None)
            ),
        )
