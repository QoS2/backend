/**
 * Nominatim 역지오코딩 (위경도 → 주소/장소명)
 * https://operations.osmfoundation.org/policies/nominatim/
 * - User-Agent 필수, 1 request/sec 제한
 */

export type ReverseGeocodeResult = {
  /** 장소/건물 이름 (제목용) */
  name: string;
  /** 전체 표시 주소 */
  displayName: string;
  /** 상세 주소 조각 (road, suburb, city 등) */
  address: Record<string, string>;
  /** 추천 반경(m) - 관광지/광장 등 넓은 장소는 70, 건물은 50 */
  suggestedRadiusM?: number;
};

const NOMINATIM_REVERSE = 'https://nominatim.openstreetmap.org/reverse';
const USER_AGENT =
  "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.132 Safari/537.36";

function pickBestName(data: {
  name?: string;
  display_name?: string;
  address?: Record<string, string>;
}): string {
  const addr = data.address;
  if (addr) {
    const order = [
      'attraction',
      'tourism',
      'historic',
      'building',
      'place',
      'amenity',
      'name',
      'road',
      'quarter',
      'suburb',
      'neighbourhood',
      'city_district',
    ];
    for (const key of order) {
      const v = addr[key];
      if (v && typeof v === 'string' && v.length >= 2) return v;
    }
  }
  if (data.name && typeof data.name === 'string') return data.name;
  if (data.display_name) {
    const first = String(data.display_name).split(',')[0]?.trim();
    if (first) return first;
  }
  return '';
}

function suggestRadius(addr?: Record<string, string>): number | undefined {
  if (!addr) return undefined;
  if (addr.attraction || addr.tourism || addr.historic || addr.place) return 70;
  if (addr.building || addr.amenity) return 50;
  return undefined;
}

export async function reverseGeocode(
  lat: number,
  lng: number
): Promise<ReverseGeocodeResult | null> {
  try {
    const params = new URLSearchParams({
      lat: String(lat),
      lon: String(lng),
      format: 'json',
      zoom: '18',
      addressdetails: '1',
    });
    const res = await fetch(`${NOMINATIM_REVERSE}?${params}`, {
      method: 'GET',
      headers: {
        'User-Agent': USER_AGENT,
        Accept: 'application/json',
      },
    });
    if (!res.ok) return null;
    const data = (await res.json()) as {
      name?: string;
      display_name?: string;
      address?: Record<string, string>;
    };
    const name = pickBestName(data);
    if (!name && !data.display_name) return null;
    const addr = data.address ?? {};
    return {
      name: name || String(data.display_name || '').split(',')[0]?.trim() || '위치',
      displayName: data.display_name ?? '',
      address: addr,
      suggestedRadiusM: suggestRadius(addr),
    };
  } catch {
    return null;
  }
}
