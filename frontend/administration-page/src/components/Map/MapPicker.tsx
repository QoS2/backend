import { MapContainer, TileLayer, Marker, useMapEvents } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

const DEFAULT_CENTER: [number, number] = [37.5665, 126.978]; // 서울 시청
const DEFAULT_ZOOM = 15;

const icon = L.divIcon({
  className: 'custom-marker',
  html: '<div style="background:#6366f1;width:20px;height:20px;border-radius:50%;border:3px solid white;box-shadow:0 1px 3px rgba(0,0,0,0.3)"></div>',
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
  const center: [number, number] =
    validLat && validLng ? [lat, lng] : DEFAULT_CENTER;
  const hasPosition = validLat && validLng;

  return (
    <div className="map-picker" style={{ height }}>
      <MapContainer
        center={center}
        zoom={DEFAULT_ZOOM}
        style={{ height: '100%', width: '100%', borderRadius: 8 }}
        scrollWheelZoom={true}
      >
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />
        <MapClickHandler onSelect={onSelect} />
        {hasPosition && lat != null && lng != null && (
          <Marker position={[lat, lng]} icon={icon} />
        )}
      </MapContainer>
      <p
        style={{
          margin: 'var(--space-sm) 0 var(--space-xl) 0',
          fontSize: 12,
          color: 'var(--color-text-muted)',
          lineHeight: 1.5,
        }}
      >
        지도에서 위치를 클릭하면 위도·경도가 자동으로 입력됩니다.
      </p>
    </div>
  );
}
