package com.app.questofseoul.domain.enums;

/** 맵 마커 타입: Place(스텝), Sub Place, 포토스팟, 보물찾기 */
public enum MarkerType {
    STEP,       // Place - 퀘스트 핵심 장소
    WAYPOINT,   // Sub Place - 이동 경로 서브 장소
    PHOTO_SPOT, // 포토 스팟
    TREASURE    // 보물 찾기
}
