#!/usr/bin/env python3
"""Tour API Connection Test Script"""
import json
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from dotenv import load_dotenv

load_dotenv()

from app.config import get_settings
from app.services.rag.tour_api_client import fetch_tour_info, _request, _build_url, _is_error_response


def main():
    s = get_settings()
    key = s.data_go_kr_service_key
    if not key:
        print("DATA_GO_KR_SERVICE_KEY is not set in .env")
        return 1

    print()

    # 1. Raw API call to searchKeyword2
    print("1. Direct call to searchKeyword2...")
    params = {"serviceKey": key, "keyword": "경복궁", "numOfRows": 2, "MobileOS": "ETC", "MobileApp": "Test", "_type": "json"}
    url = _build_url("searchKeyword2", params.copy())
    data = _request(url)
    if data:
        code = data.get("response", {}).get("header", {}).get("resultCode", "")
        msg = data.get("response", {}).get("header", {}).get("resultMsg", "")
        print(f"   resultCode: {code}, resultMsg: {msg}")
        if code == "0000":
            body = data.get("response", {}).get("body", {})
            total = body.get("totalCount", 0)
            print(f"   totalCount: {total}")
            items = body.get("items", {})
            if items:
                item = items.get("item")
                if isinstance(item, list) and item:
                    print(f"  First result: {item[0].get('title', '')}")
                elif isinstance(item, dict):
                    print(f"  First result: {item.get('title', '')}")
            print("  API is working")
        else:
            print("  API error")
    else:
        print("  Request failed")

    print()
    print("2. Call fetch_tour_info('경복궁')...")
    result = fetch_tour_info(key, "경복궁")
    if result:
        print("  Success:", list(result.keys()))
        for k, v in list(result.items())[:3]:
            print(f"      {k}: {str(v)[:60]}...")
    else:
        print("  None returned")

    return 0 if result else 1


if __name__ == "__main__":
    sys.exit(main())
