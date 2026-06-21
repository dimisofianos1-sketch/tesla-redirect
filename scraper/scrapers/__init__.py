from .ab_scraper import ABScraper
from .sklavenitis_scraper import SklavenitissScraper
from .lidl_scraper import LidlScraper
from .masoutis_scraper import MasoutisScraper
from .mymarket_scraper import MyMarketScraper
from .kritikos_scraper import KritikosScraper
from .bazaar_scraper import BazaarScraper
from .chalkiadakis_scraper import ChalkiadakisScraper

ALL_SCRAPERS = [
    ABScraper,
    SklavenitissScraper,
    LidlScraper,
    MasoutisScraper,
    MyMarketScraper,
    KritikosScraper,
    BazaarScraper,
    ChalkiadakisScraper,
]

__all__ = [
    "ABScraper",
    "SklavenitissScraper",
    "LidlScraper",
    "MasoutisScraper",
    "MyMarketScraper",
    "KritikosScraper",
    "BazaarScraper",
    "ChalkiadakisScraper",
    "ALL_SCRAPERS",
]
