from dataclasses import dataclass, field
from datetime import datetime
from typing import Optional


@dataclass
class PriceRecord:
    item_id: str
    supermarket_id: str
    price: float
    currency: str = "EUR"
    unit: str = ""
    is_on_sale: bool = False
    original_price: Optional[float] = None
    scraped_at: str = field(default_factory=lambda: datetime.utcnow().isoformat())
    product_url: Optional[str] = None
    product_name_found: Optional[str] = None

    def to_dict(self) -> dict:
        return {
            "item_id": self.item_id,
            "supermarket_id": self.supermarket_id,
            "price": self.price,
            "currency": self.currency,
            "unit": self.unit,
            "is_on_sale": self.is_on_sale,
            "original_price": self.original_price,
            "scraped_at": self.scraped_at,
            "product_url": self.product_url,
            "product_name_found": self.product_name_found,
        }


@dataclass
class ScrapeResult:
    supermarket_id: str
    records: list[PriceRecord] = field(default_factory=list)
    errors: list[str] = field(default_factory=list)
    scraped_at: str = field(default_factory=lambda: datetime.utcnow().isoformat())
    success: bool = True
