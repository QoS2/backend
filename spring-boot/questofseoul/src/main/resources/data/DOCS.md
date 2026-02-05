## 데이터베이스 설치

```bash
# PostgreSQL 설치
brew install postgresql

# 서비스 실행
brew services start postgresql

# 접속
psql postgres
```

### 사용자 & DB 기본 세팅

```sql
-- 사용자 생성
CREATE USER myuser WITH PASSWORD 'mypassword';

-- DB 생성
CREATE DATABASE mydb OWNER myuser;

-- 권한 부여
GRANT ALL PRIVILEGES ON DATABASE mydb TO myuser;
```

## 데이터베이스 설정

### PostgreSQL + PostGIS 설치

```sql
-- PostgreSQL에 PostGIS 확장 설치
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 데이터베이스 생성
CREATE DATABASE questofseoul;
```

## 초기 데이터

초기 테스트 데이터는 SQL 스크립트로 제공됩니다.

### 데이터 로드 방법

```bash
# PostgreSQL에 연결
psql -U postgres -d questofseoul

# SQL 스크립트 실행
\i src/main/resources/data/init_quest_data.sql
```

또는 직접 실행:

```bash
psql -U postgres -d questofseoul -f src/main/resources/data/init_quest_data.sql
```
