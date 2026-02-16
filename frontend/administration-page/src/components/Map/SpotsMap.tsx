import { useMemo } from 'react';
import { MapContainer, TileLayer, Marker, Popup, Circle } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import type { SpotAdminResponse } from '../../api/tour';

const DEFAULT_CENTER: [number, number] = [37.5665, 126.978];
const DEFAULT_ZOOM = 14;

const icon = L.divIcon({
  className: 'custom-marker',
  html: '<div style="background:var(--color-accent);width:20px;height:20px;border-radius:999px;border:3px solid #fff;box-shadow:0 2px 8px rgba(0,0,0,0.25)"></div>',
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
      <div
        style={{
          height: 220,
          borderRadius: 12,
          border: '1px dashed var(--color-border-strong)',
          background: 'var(--color-bg-subtle)',
          color: 'var(--color-text-muted)',
          display: 'grid',
          placeItems: 'center',
          fontSize: 13,
        }}
      >
        위치가 설정된 Spot이 없습니다.
      </div>
    );
  }

  return (
    <div
      style={{
        height: '100%',
        minHeight: 280,
        borderRadius: 12,
        border: '1px solid var(--color-border)',
        overflow: 'hidden',
        boxShadow: 'var(--shadow-sm)',
      }}
    >
      <MapContainer
        center={center}
        zoom={bounds ? 14 : DEFAULT_ZOOM}
        style={{ height: '100%', width: '100%' }}
        scrollWheelZoom
      >
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />

        {withCoords.map((spot) => (
          <Marker key={spot.id} position={[spot.latitude!, spot.longitude!]} icon={icon}>
            <Popup>
              <strong>
                [{spot.type}] {spot.orderIndex}. {spot.title}
              </strong>
              {spot.radiusM != null && (
                <div style={{ fontSize: 12, color: '#55695f' }}>반경 {spot.radiusM}m</div>
              )}
            </Popup>

            {spot.radiusM != null && spot.radiusM > 0 && (
              <Circle
                center={[spot.latitude!, spot.longitude!]}
                radius={spot.radiusM}
                pathOptions={{
                  color: 'var(--color-accent)',
                  fillColor: 'var(--color-accent)',
                  fillOpacity: 0.12,
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
