-- questofseoul DB 전체 데이터 초기화
-- 실행: psql -h localhost -U postgres -d questofseoul -f scripts/reset-db.sql
-- 또는: source .env && psql "$DB_URL" -f scripts/reset-db.sql

-- public 스키마의 모든 테이블 truncate (CASCADE로 FK 의존성 해결)
DO $$
DECLARE
  r RECORD;
  tbls TEXT := '';
BEGIN
  FOR r IN (
    SELECT tablename FROM pg_tables
    WHERE schemaname = 'public'
    ORDER BY tablename
  )
  LOOP
    IF tbls != '' THEN tbls := tbls || ', '; END IF;
    tbls := tbls || quote_ident(r.tablename);
  END LOOP;
  IF tbls != '' THEN
    EXECUTE 'TRUNCATE TABLE ' || tbls || ' RESTART IDENTITY CASCADE';
    RAISE NOTICE 'Truncated all tables in public schema';
  END IF;
END $$;
