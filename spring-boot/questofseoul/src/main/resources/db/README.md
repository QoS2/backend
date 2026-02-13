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

## 스키마

테이블은 Hibernate `ddl-auto=update`로 생성됨.

## Docker Compose 사용 시

```bash
# 1. 초기화
docker exec -i questofseoul-db psql -U postgres -d questofseoul < spring-boot/questofseoul/scripts/reset-db.sql

# 2. 시드 데이터
docker exec -i questofseoul-db psql -U postgres -d questofseoul < spring-boot/questofseoul/src/main/resources/db/data.sql
```
