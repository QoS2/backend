import { MapContainer, TileLayer, Marker, useMapEvents } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

const DEFAULT_CENTER: [number, number] = [37.5665, 126.978]; // 서울 시청
const DEFAULT_ZOOM = 15;

const icon = L.divIcon({
  className: 'custom-marker',
  html: '<div style="background:var(--color-accent);width:20px;height:20px;border-radius:999px;border:3px solid #fff;box-shadow:0 2px 8px rgba(0,0,0,0.25)"></div>',
  iconSize: [20, 20],
  iconAnchor: [10, 10],
});

function MapClickHandler({
  onSelect,
}: {
  onSelect: (lat: number, lng: number) => void;
}) {
  useMapEvents({
    click(e) {
      const { lat, lng } = e.latlng;
      onSelect(lat, lng);
    },
  });
  return null;
}

export function MapPicker({
  lat,
  lng,
  onSelect,
  height = 200,
}: {
  lat?: number | null;
  lng?: number | null;
  onSelect: (lat: number, lng: number) => void;
  height?: number;
}) {
  const validLat = lat != null && !Number.isNaN(lat);
  const validLng = lng != null && !Number.isNaN(lng);
  const center: [number, number] = validLat && validLng ? [lat, lng] : DEFAULT_CENTER;
  const hasPosition = validLat && validLng;

  return (
    <div
      className="map-picker"
      style={{ display: 'flex', flexDirection: 'column', gap: 10 }}
    >
      <div style={{ height }}>
        <MapContainer
          center={center}
          zoom={DEFAULT_ZOOM}
          style={{
            height: '100%',
            width: '100%',
            borderRadius: 12,
            border: '1px solid var(--color-border)',
            boxShadow: 'var(--shadow-sm)',
          }}
          scrollWheelZoom
        >
          <TileLayer
            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          />
          <MapClickHandler onSelect={onSelect} />
          {hasPosition && lat != null && lng != null && <Marker position={[lat, lng]} icon={icon} />}
        </MapContainer>
      </div>
      <p
        style={{
          margin: 0,
          fontSize: 12,
          color: 'var(--color-text-muted)',
          lineHeight: 1.45,
        }}
      >
        지도를 클릭하면 위도·경도가 자동으로 반영됩니다.
      </p>
    </div>
  );
}
