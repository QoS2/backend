import { useState, useEffect } from 'react';
import { getJson } from '../utils/api';
import { previewChat } from '../api/tour';
import styles from './MobilePreviewPage.module.css';

type TourDetail = {
  id: number;
  externalKey: string;
  titleEn: string;
  descriptionEn: string;
  infoJson: Record<string, unknown> | null;
  goodToKnowJson: Record<string, unknown> | null;
  tags: string[];
  stepsCount: number;
  waypointsCount: number;
  photoSpotsCount: number;
  treasuresCount: number;
  quizzesCount: number;
  startLatitude: number | null;
  startLongitude: number | null;
  unlocked: boolean;
};

type Marker = {
  id: number;
  type: 'STEP' | 'WAYPOINT' | 'PHOTO_SPOT' | 'TREASURE';
  title: string;
  latitude: number;
  longitude: number;
  radiusM: number;
  refId: number | null;
  stepOrder: number | null;
};

type GuideSegment = {
  stepId: number;
  stepTitle: string;
  segments: Array<{
    id: number;
    segIdx: number;
    textEn: string;
    triggerKey: string | null;
    media: Array<{ id: number; url: string; meta: unknown }>;
  }>;
};

type Quiz = {
  id: number;
  externalKey: string | null;
  type: 'PHOTO_CHOOSE' | 'FILL_BLANK' | 'MULTIPLE_CHOICE';
  promptEn: string;
  specJson: Record<string, unknown> | null;
  hintEn: string | null;
  mintReward: number;
  hasHint: boolean;
};

type TabType = 'map' | 'place' | 'treasure' | 'photo';
type ViewType = 'moving' | 'step' | 'quest';

const TAB_LABELS: Record<TabType, string> = {
  map: 'AI Tour Guide',
  place: 'Place',
  treasure: 'Treasure',
  photo: 'Photo',
};

function getMarkerTypeClass(type: Marker['type']): string {
  switch (type) {
    case 'STEP':
      return styles.markerTypeStep;
    case 'WAYPOINT':
      return styles.markerTypeWaypoint;
    case 'PHOTO_SPOT':
      return styles.markerTypePhoto;
    case 'TREASURE':
      return styles.markerTypeTreasure;
    default:
      return '';
  }
}

export function MobilePreviewPage() {
  const [tour, setTour] = useState<TourDetail | null>(null);
  const [markers, setMarkers] = useState<Marker[]>([]);
  const [activeTab, setActiveTab] = useState<TabType>('map');
  const [view, setView] = useState<ViewType>('moving');
  const [selectedStepId, setSelectedStepId] = useState<number | null>(null);
  const [guide, setGuide] = useState<GuideSegment | null>(null);
  const [quizzes, setQuizzes] = useState<Quiz[]>([]);
  const [chatMessages, setChatMessages] = useState<Array<{ role: string; text: string; source?: string }>>([]);
  const [chatInput, setChatInput] = useState('');
  const [chatSending, setChatSending] = useState(false);

  async function handleSendChat() {
    const text = chatInput.trim();
    if (!text || !tour?.id || chatSending) return;
    setChatInput('');
    setChatMessages((prev) => [...prev, { role: 'USER', text }]);
    setChatSending(true);
    try {
      const history = chatMessages.map((m) => ({
        role: m.role === 'USER' ? 'user' : 'assistant',
        content: m.text,
      }));
      const { aiText } = await previewChat(tour.id, text, history);
      setChatMessages((prev) => [...prev, { role: 'GUIDE', text: aiText, source: 'LLM' }]);
    } catch (e) {
      setChatMessages((prev) => [
        ...prev,
        { role: 'GUIDE', text: '죄송합니다. 응답을 생성하지 못했습니다. 로그인 후 다시 시도해 주세요.', source: 'LLM' },
      ]);
    } finally {
      setChatSending(false);
    }
  }
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    async function load() {
      try {
        setLoading(true);
        const tourList = await getJson<Array<{ id: number; externalKey: string; titleEn: string }>>('/tours', { base: 'api' });
        const tid = tourList?.[0]?.id;
        if (!tid) {
          setError('등록된 투어가 없습니다.');
          return;
        }
        const [tourRes, markersRes] = await Promise.all([
          getJson<TourDetail>(`/tours/${tid}`, { base: 'api' }),
          getJson<Marker[]>(`/tours/${tid}/markers`, { base: 'api' }),
        ]);
        setTour(tourRes);
        setMarkers(markersRes);
        setChatMessages([
          { role: 'GUIDE', text: '경복궁에 오신 것을 환영합니다! AI 투어 가이드가 안내해 드릴게요.', source: 'SCRIPT' },
        ]);
      } catch (e) {
        setError(e instanceof Error ? e.message : 'Failed to load');
      } finally {
        setLoading(false);
      }
    }
    load();
  }, []);

  async function openStep(stepId: number) {
    setSelectedStepId(stepId);
    setView('step');
    try {
      const [guideRes, quizzesRes] = await Promise.all([
        getJson<GuideSegment>(`/steps/${stepId}/guide`, { base: 'api' }),
        getJson<Quiz[]>(`/steps/${stepId}/quizzes`, { base: 'api' }),
      ]);
      setGuide(guideRes);
      setQuizzes(quizzesRes);
    } catch {
      setGuide(null);
      setQuizzes([]);
    }
  }

  function goToQuest() {
    setView('quest');
  }

  function goBack() {
    if (view === 'quest') setView('step');
    else if (view === 'step') setView('moving');
  }

  if (loading || error) {
    return (
      <div className={styles.page}>
        <div className={styles.phoneFrame}>
          <div className={styles.screen}>
            {loading ? (
              <div className={styles.loading}>Loading...</div>
            ) : (
              <div className={styles.error}>{error}</div>
            )}
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className={styles.page}>
      <div className={styles.toolbar}>
        <span className={styles.toolbarTitle}>모바일 미리보기 · Quest-of-Seoul 재영</span>
      </div>
      <div className={styles.phoneFrame}>
        <div className={styles.phoneNotch} />
        <div className={styles.screen}>
          <header className={styles.header}>
            {view !== 'moving' && (
              <button type="button" className={styles.backBtn} onClick={goBack}>
                ←
              </button>
            )}
            <h1 className={styles.headerTitle}>
              {view === 'moving' ? tour?.titleEn ?? 'Quest of Seoul' : selectedStepId ? '스텝 상세' : '퀘스트'}
            </h1>
          </header>

          <main className={styles.main}>
            {view === 'moving' && (
              <>
                <div className={styles.tabBar}>
                  {(['map', 'place', 'treasure', 'photo'] as const).map((t) => (
                    <button
                      key={t}
                      type="button"
                      className={[styles.tab, activeTab === t ? styles.tabActive : ''].join(' ')}
                      onClick={() => setActiveTab(t)}
                    >
                      {TAB_LABELS[t]}
                    </button>
                  ))}
                </div>

                <div className={styles.contentArea}>
                  {activeTab === 'map' && (
                    <div className={styles.mapPlaceholder}>
                      <span className={styles.mapLabel}>지도 (Place / Sub Place / Photo Spot / Treasure 마커)</span>
                      <div className={styles.proximityBadge}>
                        <span className={styles.proximityCircle} />
                        50m 이내 · You are close
                      </div>
                      <ul className={styles.markerList}>
                        {markers.slice(0, 8).map((m) => (
                          <li key={`${m.type}-${m.id}`}>
                            <span className={[styles.markerType, getMarkerTypeClass(m.type)].join(' ')}>
                              {m.type}
                            </span>
                            {m.title}
                            {m.type === 'STEP' && (
                              <button
                                type="button"
                                className={styles.stepBtn}
                                onClick={() => openStep(m.refId ?? m.id)}
                              >
                                스텝 페이지
                              </button>
                            )}
                          </li>
                        ))}
                      </ul>
                    </div>
                  )}
                  {activeTab === 'place' && (
                    <div className={styles.placeList}>
                      {markers.filter((m) => m.type === 'STEP').map((m) => (
                        <div key={m.id} className={styles.placeItem}>
                          <span>{m.title}</span>
                          <span className={styles.notVisited}>방문 전</span>
                          <button type="button" className={styles.stepBtn} onClick={() => openStep(m.refId ?? m.id)}>
                            스텝 페이지
                          </button>
                        </div>
                      ))}
                    </div>
                  )}
                  {activeTab === 'treasure' && (
                    <div className={styles.treasureArea}>
                      <p>내가 수집한 보물 / 아직 수집되지 않은 보물</p>
                      <p className={styles.hint}>보물 마커: {markers.filter((m) => m.type === 'TREASURE').length}개</p>
                    </div>
                  )}
                  {activeTab === 'photo' && (
                    <div className={styles.photoArea}>
                      <p>포토스팟 / 내가 찍은 사진</p>
                      <p className={styles.hint}>포토스팟: {markers.filter((m) => m.type === 'PHOTO_SPOT').length}개</p>
                    </div>
                  )}
                </div>
              </>
            )}

            {view === 'step' && guide && (
              <div className={styles.stepContent}>
                <div className={styles.arrivalBanner}>도착했다! 🎉</div>
                <h2>{guide.stepTitle}</h2>
                {guide.segments.map((seg) => (
                  <div key={seg.id} className={styles.guideSegment}>
                    <p>{seg.textEn || '(가이드 설명 텍스트)'}</p>
                    {seg.media.length > 0 && (
                      <div className={styles.mediaPlaceholder}>이미지</div>
                    )}
                  </div>
                ))}
                <button type="button" className={styles.primaryBtn} onClick={goToQuest}>
                  History of Gate (퀘스트 하러 가기)
                </button>
              </div>
            )}

            {view === 'quest' && (
              <div className={styles.questContent}>
                <h2>퀘스트</h2>
                <p>사진 고르기 / 빈칸 넣기 / 텍스트 객관식</p>
                {quizzes.map((q) => (
                  <div key={q.id} className={styles.quizItem}>
                    <span>{q.type}</span> {q.promptEn || '퀴즈 프롬프트'}
                  </div>
                ))}
              </div>
            )}
          </main>

          <div className={styles.chatPanel}>
            <div className={styles.chatMessages}>
              {chatMessages.map((msg, i) => (
                <div key={i} className={msg.role === 'GUIDE' ? styles.chatGuide : styles.chatUser}>
                  {msg.source === 'SCRIPT' && <span className={styles.badge}>Official</span>}
                  {msg.source === 'LLM' && <span className={styles.badge}>AI</span>}
                  {msg.text}
                </div>
              ))}
            </div>
            <div className={styles.chatInputRow}>
              <input
                type="text"
                placeholder="궁금한 점을 물어보세요"
                value={chatInput}
                onChange={(e) => setChatInput(e.target.value)}
                onKeyDown={(e) => e.key === 'Enter' && handleSendChat()}
              />
              <button type="button" onClick={handleSendChat} disabled={chatSending}>
                {chatSending ? '...' : '전송'}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
