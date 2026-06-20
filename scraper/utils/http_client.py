import time
import random
import logging
from typing import Optional
import requests
from requests.adapters import HTTPAdapter
from urllib3.util.retry import Retry

logger = logging.getLogger(__name__)

HEADERS = {
    "User-Agent": (
        "Mozilla/5.0 (Linux; Android 14; Pixel 8) "
        "AppleWebKit/537.36 (KHTML, like Gecko) "
        "Chrome/124.0.0.0 Mobile Safari/537.36"
    ),
    "Accept-Language": "el-GR,el;q=0.9,en;q=0.8",
    "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
    "Accept-Encoding": "gzip, deflate, br",
    "Connection": "keep-alive",
}


def build_session(retries: int = 3, backoff: float = 1.0) -> requests.Session:
    session = requests.Session()
    retry = Retry(
        total=retries,
        backoff_factor=backoff,
        status_forcelist=[429, 500, 502, 503, 504],
    )
    adapter = HTTPAdapter(max_retries=retry)
    session.mount("http://", adapter)
    session.mount("https://", adapter)
    session.headers.update(HEADERS)
    return session


def polite_get(
    session: requests.Session,
    url: str,
    min_delay: float = 1.5,
    max_delay: float = 4.0,
    timeout: int = 20,
    params: Optional[dict] = None,
) -> Optional[requests.Response]:
    time.sleep(random.uniform(min_delay, max_delay))
    try:
        resp = session.get(url, timeout=timeout, params=params)
        resp.raise_for_status()
        return resp
    except requests.RequestException as exc:
        logger.warning("GET %s failed: %s", url, exc)
        return None
