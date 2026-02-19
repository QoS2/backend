# DB Init (경복궁 투어)

SQL 기반 시드 데이터.

## 수동 실행

```bash
cd spring-boot/questofseoul

# 1. DB 전체 초기화 (모든 테이블 데이터 삭제)
psql -h localhost -U postgres -d questofseoul -f scripts/reset-db.sql

# 2. 시드 데이터 삽입 (경복궁 투어)
psql -h localhost -U postgres -d questofseoul -f src/main/resources/db/data.sql
```

## 기존 DB 마이그레이션 (권장)

기존 로컬 DB를 유지하는 경우 아래 마이그레이션을 순서대로 적용하세요.

```bash
cd spring-boot/questofseoul
psql -h localhost -U postgres -d questofseoul -f scripts/migrations/V001__add_spot_content_steps_next_action.sql
psql -h localhost -U postgres -d questofseoul -f scripts/migrations/V002__remove_lock_state_add_tour_assets.sql
psql -h localhost -U postgres -d questofseoul -f scripts/migrations/V003__normalize_mission_type_and_drop_lock_state.sql
```

## 스키마

테이블은 Hibernate `ddl-auto=update`로 생성됨.

## Docker Compose 사용 시

```bash
# 1. 초기화
docker exec -i questofseoul-db psql -U postgres -d questofseoul < spring-boot/questofseoul/scripts/reset-db.sql

# 2. 시드 데이터
docker exec -i questofseoul-db psql -U postgres -d questofseoul < spring-boot/questofseoul/src/main/resources/db/data.sql
```
