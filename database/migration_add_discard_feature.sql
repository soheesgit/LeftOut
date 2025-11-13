-- =====================================================
-- 통계 기능 추가를 위한 데이터베이스 마이그레이션
-- 작성일: 2025-01-13
-- 설명: discard_date 컬럼 추가 및 status 제약조건 수정
-- =====================================================

-- 현재 데이터베이스 선택
USE leftout;

-- =====================================================
-- 1. 백업 권장 사항
-- =====================================================
-- 마이그레이션 실행 전 백업을 권장합니다:
-- mysqldump -u user_01 -p leftout > backup_before_migration.sql

-- =====================================================
-- 2. 현재 테이블 상태 확인 (실행 전 검증)
-- =====================================================
SELECT 'Before Migration - Checking current table structure...' AS status;

-- 현재 ingredient 테이블 구조 확인
DESCRIBE ingredient;

-- 현재 데이터 개수 확인
SELECT
    COUNT(*) as total_ingredients,
    COUNT(CASE WHEN status = 'active' THEN 1 END) as active_count,
    COUNT(CASE WHEN status = 'consumed' THEN 1 END) as consumed_count
FROM ingredient;

-- =====================================================
-- 3. 마이그레이션 실행
-- =====================================================

-- 3.1. discard_date 컬럼 추가
-- NULL 허용 (기존 데이터는 discard_date가 없으므로)
SELECT 'Adding discard_date column...' AS status;

ALTER TABLE ingredient
ADD COLUMN discard_date DATE NULL AFTER consume_date;

-- 3.2. status CHECK 제약조건 수정
-- MySQL에서 CHECK 제약조건 이름 확인 방법:
-- SHOW CREATE TABLE ingredient;

SELECT 'Updating status CHECK constraint...' AS status;

-- 기존 CHECK 제약조건 삭제
-- 주의: MySQL 8.0.16 이상에서만 CHECK 제약조건 지원
-- 제약조건 이름은 자동 생성되었을 수 있으므로 확인 필요
-- 일반적으로 'ingredient_chk_1' 또는 'ingredient_chk_2' 형식

-- CHECK 제약조건 이름 확인
SELECT
    CONSTRAINT_NAME,
    CHECK_CLAUSE
FROM information_schema.CHECK_CONSTRAINTS
WHERE TABLE_NAME = 'ingredient'
  AND TABLE_SCHEMA = 'leftout'
  AND CHECK_CLAUSE LIKE '%status%';

-- 기존 status CHECK 제약조건 삭제 (이름을 확인한 후 실행)
-- ALTER TABLE ingredient DROP CHECK ingredient_chk_1;

-- 새로운 CHECK 제약조건 추가 (discarded 포함)
ALTER TABLE ingredient
ADD CONSTRAINT chk_ingredient_status
CHECK (status IN ('active', 'consumed', 'discarded'));

-- storage_location CHECK 제약조건도 다시 추가 (기존 제약조건과 충돌 방지)
-- 기존 제약조건이 있다면 먼저 확인
ALTER TABLE ingredient
ADD CONSTRAINT chk_ingredient_storage
CHECK (storage_location IN ('냉장', '냉동', '실온'));

-- =====================================================
-- 4. 마이그레이션 검증
-- =====================================================
SELECT 'Verifying migration...' AS status;

-- 테이블 구조 확인 (discard_date 컬럼이 추가되었는지)
DESCRIBE ingredient;

-- CHECK 제약조건 확인
SELECT
    CONSTRAINT_NAME,
    CHECK_CLAUSE
FROM information_schema.CHECK_CONSTRAINTS
WHERE TABLE_NAME = 'ingredient'
  AND TABLE_SCHEMA = 'leftout';

-- 데이터 무결성 확인
SELECT
    COUNT(*) as total_ingredients,
    COUNT(CASE WHEN status = 'active' THEN 1 END) as active_count,
    COUNT(CASE WHEN status = 'consumed' THEN 1 END) as consumed_count,
    COUNT(CASE WHEN discard_date IS NOT NULL THEN 1 END) as has_discard_date
FROM ingredient;

-- =====================================================
-- 5. 테스트 데이터 (선택사항)
-- =====================================================
-- 폐기 기능 테스트를 위한 샘플 데이터
-- 실제 운영 환경에서는 주석 처리하고 사용하지 마세요

/*
-- 테스트용 폐기 데이터 추가 (기존 식재료가 있다면)
UPDATE ingredient
SET status = 'discarded',
    discard_date = CURDATE()
WHERE ingredient_id = 1  -- 테스트할 식재료 ID
  AND status = 'active';

-- 확인
SELECT * FROM ingredient WHERE status = 'discarded';
*/

-- =====================================================
-- 6. 롤백 스크립트 (문제 발생 시 사용)
-- =====================================================
/*
-- 마이그레이션을 되돌리려면 아래 SQL 실행

-- discard_date 컬럼 삭제
ALTER TABLE ingredient DROP COLUMN discard_date;

-- status CHECK 제약조건 원복
ALTER TABLE ingredient DROP CHECK chk_ingredient_status;
ALTER TABLE ingredient DROP CHECK chk_ingredient_storage;

ALTER TABLE ingredient
ADD CONSTRAINT ingredient_chk_1
CHECK (status IN ('active', 'consumed'));

ALTER TABLE ingredient
ADD CONSTRAINT ingredient_chk_2
CHECK (storage_location IN ('냉장', '냉동', '실온'));
*/

SELECT 'Migration completed successfully!' AS status;
SELECT 'Please verify the changes by checking the table structure and constraints.' AS next_step;
