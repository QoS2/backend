import { useMemo } from 'react';
import { MapContainer, TileLayer, Marker, Popup, Circle } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import type { SpotAdminResponse } from '../../api/tour';

const DEFAULT_CENTER: [number, number] = [37.5665, 126.978];
const DEFAULT_ZOOM = 14;

const icon = L.divIcon({
  className: 'custom-marker',
  html: '<div style="background:#6366f1;width:20px;height:20px;border-radius:50%;border:3px solid white;box-shadow:0 1px 3px rgba(0,0,0,0.3)"></div>',
  iconSize: [20, 20],
  iconAnchor: [10, 10],
});

export function SpotsMap({ spots }: { spots: SpotAdminResponse[] }) {
  const withCoords = useMemo(
    () => spots.filter((s) => s.latitude != null && s.longitude != null),
    [spots]
  );

  const bounds = useMemo(() => {
    if (withCoords.length === 0) return null;
    const lats = withCoords.map((s) => s.latitude!);
    const lngs = withCoords.map((s) => s.longitude!);
    return [
      [Math.min(...lats), Math.min(...lngs)],
      [Math.max(...lats), Math.max(...lngs)],
    ] as [[number, number], [number, number]];
  }, [withCoords]);

  const center: [number, number] =
    withCoords.length > 0
      ? [
          withCoords.reduce((a, s) => a + (s.latitude ?? 0), 0) / withCoords.length,
          withCoords.reduce((a, s) => a + (s.longitude ?? 0), 0) / withCoords.length,
        ]
      : DEFAULT_CENTER;

  if (withCoords.length === 0) {
    return (
      <div style={{ height: 200, display: 'flex', alignItems: 'center', justifyContent: 'center', background: 'var(--color-bg-hover)', borderRadius: 8, color: 'var(--color-text-muted)' }}>
        위치가 설정된 Spot이 없습니다.
      </div>
    );
  }

  return (
    <div style={{ height: '100%', minHeight: 280, borderRadius: 8, overflow: 'hidden' }}>
      <MapContainer
        center={center}
        zoom={bounds ? 14 : DEFAULT_ZOOM}
        style={{ height: '100%', width: '100%' }}
        scrollWheelZoom={true}
      >
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />
        {withCoords.map((spot) => (
          <Marker
            key={spot.id}
            position={[spot.latitude!, spot.longitude!]}
            icon={icon}
          >
            <Popup>
              <strong>[{spot.type}] {spot.orderIndex}. {spot.title}</strong>
              {spot.radiusM != null && (
                <div style={{ fontSize: 12, color: '#666' }}>
                  반경 {spot.radiusM}m
                </div>
              )}
            </Popup>
            {spot.radiusM != null && spot.radiusM > 0 && (
              <Circle
                center={[spot.latitude!, spot.longitude!]}
                radius={spot.radiusM}
                pathOptions={{
                  color: 'var(--color-accent)',
                  fillColor: 'var(--color-accent)',
                  fillOpacity: 0.1,
                  weight: 1,
                }}
              />
            )}
          </Marker>
        ))}
      </MapContainer>
    </div>
  );
}
