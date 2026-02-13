"""Korea Tourism Organization Tour API (VisitKorea) client"""
import json
import logging
from urllib.parse import quote, urlencode
from urllib.request import Request, urlopen
from urllib.error import HTTPError

logger = logging.getLogger(__name__)

BASE_URL = "http://apis.data.go.kr/B551011/KorService2"

USER_AGENT = (
    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.132 Safari/537.36"
)

def fetch_tour_info(service_key: str, keyword: str) -> dict | None:
    """
    Retrieves tour information from the Tour API.
    - searchKeyword2: Search for a keyword.
    - detailCommon2: Common information (overview, etc.).
    - detailIntro2: Introduction information (opening hours, holidays, etc.).
    """
    if not service_key or not keyword or len(keyword.strip()) < 2:
        return None

    try:
        # 1. Search for a keyword.
        items = _search_keyword2(service_key, keyword.strip())
        if not items:
            return None

        item = items[0]
        content_id = item.get("contentid")
        title = item.get("title", keyword)
        if not content_id:
            return _item_to_info(item, title)

        # 2. Common information (overview, etc.).
        common = _detail_common2(service_key, content_id)
        # 3. Introduction information (opening hours, holidays, etc.).
        intro = _detail_intro2(service_key, content_id, item.get("contenttypeid", "12"))
        # 4. Image information (detailImage2).
        images = _detail_image2(service_key, content_id)

        return _merge_info(item, common, intro, title, images)
    except Exception as e:
        logger.warning("Tour API fetch failed for %r: %s", keyword, e)
        return None


def _build_url(path: str, params: dict) -> str:
    params.setdefault("MobileOS", "ETC")
    params.setdefault("MobileApp", "QuestOfSeoul")
    params.setdefault("_type", "json")
    # Encoding key (% included) is left as is, decoding key is encoded.
    service_key = params.pop("serviceKey", "")
    sk_encoded = service_key if "%" in service_key else quote(service_key, safe="")
    rest = urlencode(params, encoding="utf-8")
    return f"{BASE_URL}/{path}?serviceKey={sk_encoded}&{rest}"


def _request(url: str) -> dict | None:
    try:
        req = Request(url, headers={"User-Agent": USER_AGENT})
        with urlopen(req, timeout=10) as resp:
            data = json.loads(resp.read().decode("utf-8"))
        return data
    except HTTPError as e:
        try:
            body = e.read().decode("utf-8", errors="replace")
            logger.warning("Tour API HTTP %s: %s", e.code, body[:300])
        except Exception:
            logger.warning("Tour API HTTP %s", e.code)
        return None
    except Exception as e:
        logger.warning("Tour API request failed: %s", e)
        return None


def _search_keyword2(service_key: str, keyword: str) -> list:
    url = _build_url("searchKeyword2", {
        "serviceKey": service_key,
        "keyword": keyword,
        "numOfRows": 5,
    })
    data = _request(url)
    if not data:
        return []
    if _is_error_response(data):
        return []
    try:
        body = data.get("response", {}).get("body", {})
        items = body.get("items", {})
        if not items:
            return []
        item_list = items.get("item")
        if isinstance(item_list, dict):
            item_list = [item_list]
        return item_list or []
    except (KeyError, TypeError):
        return []


def _detail_common2(service_key: str, content_id: str) -> dict:
    url = _build_url("detailCommon2", {
        "serviceKey": service_key,
        "contentId": content_id,
        "defaultYN": "Y",
        "firstImageYN": "N",
        "areacodeYN": "N",
        "addrinfoYN": "Y",
        "overviewYN": "Y",
    })
    data = _request(url)
    if not data or _is_error_response(data):
        return {}
    try:
        items = data.get("response", {}).get("body", {}).get("items", {}).get("item")
        if isinstance(items, list):
            return items[0] if items else {}
        return items or {}
    except (KeyError, TypeError):
        return {}


def _is_error_response(data: dict) -> bool:
    """resultCode is '0000' if successful"""
    code = data.get("response", {}).get("header", {}).get("resultCode", "")
    return code != "0000"


def _detail_image2(service_key: str, content_id: str) -> list:
    """Retrieve image information (maximum 3 URLs)"""
    url = _build_url("detailImage2", {
        "serviceKey": service_key,
        "contentId": content_id,
        "numOfRows": 3,
        "imageYN": "Y",
    })
    data = _request(url)
    if not data or _is_error_response(data):
        return []
    try:
        items = data.get("response", {}).get("body", {}).get("items", {})
        if not items:
            return []
        item_list = items.get("item")
        if isinstance(item_list, dict):
            item_list = [item_list]
        urls = []
        for it in (item_list or []):
            u = it.get("originimgurl") or it.get("smallimageurl")
            if u:
                urls.append(u)
        return urls[:3]
    except (KeyError, TypeError):
        return []


def _detail_intro2(service_key: str, content_id: str, content_type_id: str) -> dict:
    url = _build_url("detailIntro2", {
        "serviceKey": service_key,
        "contentId": content_id,
        "contentTypeId": content_type_id,
    })
    data = _request(url)
    if not data or _is_error_response(data):
        return {}
    try:
        items = data.get("response", {}).get("body", {}).get("items", {}).get("item")
        if isinstance(items, list):
            return items[0] if items else {}
        return items or {}
    except (KeyError, TypeError):
        return {}


def _item_to_info(item: dict, title: str) -> dict:
    """Construct basic information from search results."""
    info = {"장소": title}
    if item.get("overview"):
        info["개요"] = item["overview"][:500]
    if item.get("addr1"):
        info["주소"] = item["addr1"]
    return info


def _merge_info(item: dict, common: dict, intro: dict, title: str, images: list | None = None) -> dict:
    """Merge search/common/introduction/image information into a single dict for RAG"""
    info = {"장소": title}

    overview = common.get("overview") or item.get("overview")
    if overview:
        info["개요"] = (overview[:600] + "…") if len(overview) > 600 else overview

    # Opening hours, holidays (detailIntro).
    usetime = intro.get("usetime")
    if usetime:
        info["이용시간"] = usetime
    restdate = intro.get("restdate")
    if restdate:
        info["휴무일"] = restdate
    useseason = intro.get("useseason")
    if useseason:
        info["이용시즌"] = useseason

    # Address
    addr = common.get("addr1") or item.get("addr1")
    if addr:
        info["주소"] = addr
    # Contact
    tel = common.get("tel") or item.get("tel")
    if tel:
        info["연락처"] = tel

    # Images (detailImage2)
    if images:
        info["이미지URL"] = ", ".join(images[:3])

    return info
