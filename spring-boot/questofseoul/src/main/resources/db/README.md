# DB Init (경복궁 투어)

SQL 기반 스키마 + 시드 데이터.

## 수동 실행

```bash
# 1. DB 생성 (없으면)
createdb questofseoul -U postgres

# 2. 스키마 (테이블 생성)
psql -U postgres -d questofseoul -f src/main/resources/db/schema.sql

# 3. 시드 데이터 (경복궁 투어)
psql -U postgres -d questofseoul -f src/main/resources/db/data.sql
```

## Docker Compose 사용 시

`docker-compose up postgres` 후:

```bash
docker exec -i questofseoul-db psql -U postgres -d questofseoul < spring-boot/questofseoul/src/main/resources/db/schema.sql
docker exec -i questofseoul-db psql -U postgres -d questofseoul < spring-boot/questofseoul/src/main/resources/db/data.sql
```

## Spring Boot 자동 실행 (선택)

`application-init.properties` 생성 후:

```properties
spring.sql.init.mode=always
spring.sql.init.schema-locations=classpath:db/schema.sql
spring.sql.init.data-locations=classpath:db/data.sql
spring.jpa.defer-datasource-initialization=true
spring.jpa.hibernate.ddl-auto=none
```

실행: `./gradlew bootRun -Dspring.profiles.active=init`

**주의:** `ddl-auto=none`이면 JPA가 테이블을 생성하지 않음. schema.sql에 필요한 테이블이 모두 있어야 함. 현재 schema.sql은 경복궁 시드용 핵심 4개 테이블만 포함. 전체 스키마가 필요하면 JPA `ddl-auto=update` 유지하고 **data.sql만** 사용하는 것을 권장.
