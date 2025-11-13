# 데이터베이스 마이그레이션 가이드

## 📋 개요
통계 기능 추가를 위해 `ingredient` 테이블에 다음 변경사항을 적용합니다:
- `discard_date` 컬럼 추가
- `status` 필드에 'discarded' 값 추가

## ⚠️ 중요 사항
- **기존 데이터는 유지됩니다**
- 마이그레이션 전 **반드시 백업**을 수행하세요
- MySQL 8.0.16 이상 버전 필요 (CHECK 제약조건 지원)

## 📝 마이그레이션 실행 방법

### 1단계: 데이터베이스 백업 (필수)

```bash
# Windows 명령 프롬프트 또는 PowerShell에서 실행
mysqldump -u user_01 -p leftout > backup_before_migration.sql
```

비밀번호 입력: `1234`

### 2단계: MySQL 접속

```bash
mysql -u user_01 -p leftout
```

비밀번호 입력: `1234`

### 3단계: 마이그레이션 실행

#### 방법 A: SQL 파일 직접 실행 (권장)

MySQL에 접속한 상태에서:

```sql
SOURCE E:/3-2/데베프/TEST_001/TEST_001/database/migration_add_discard_feature.sql;
```

또는 명령줄에서 직접:

```bash
mysql -u user_01 -p leftout < database/migration_add_discard_feature.sql
```

#### 방법 B: 수동으로 단계별 실행

MySQL에 접속한 후 아래 SQL을 순서대로 실행:

```sql
-- 1. 데이터베이스 선택
USE leftout;

-- 2. discard_date 컬럼 추가
ALTER TABLE ingredient
ADD COLUMN discard_date DATE NULL AFTER consume_date;

-- 3. 기존 CHECK 제약조건 확인
SELECT CONSTRAINT_NAME, CHECK_CLAUSE
FROM information_schema.CHECK_CONSTRAINTS
WHERE TABLE_NAME = 'ingredient'
  AND TABLE_SCHEMA = 'leftout';

-- 4. 새로운 CHECK 제약조건 추가
ALTER TABLE ingredient
ADD CONSTRAINT chk_ingredient_status
CHECK (status IN ('active', 'consumed', 'discarded'));

ALTER TABLE ingredient
ADD CONSTRAINT chk_ingredient_storage
CHECK (storage_location IN ('냉장', '냉동', '실온'));
```

### 4단계: 검증

마이그레이션 후 테이블 구조 확인:

```sql
-- 테이블 구조 확인
DESCRIBE ingredient;

-- CHECK 제약조건 확인
SHOW CREATE TABLE ingredient;

-- 데이터 확인
SELECT COUNT(*) FROM ingredient;
```

예상 결과:
- `discard_date` 컬럼이 `consume_date` 다음에 추가되어 있어야 함
- `status` 필드의 CHECK 제약조건에 'discarded'가 포함되어 있어야 함

## 🔄 롤백 방법 (문제 발생 시)

마이그레이션을 되돌려야 하는 경우:

```sql
-- discard_date 컬럼 삭제
ALTER TABLE ingredient DROP COLUMN discard_date;

-- 새로운 CHECK 제약조건 삭제
ALTER TABLE ingredient DROP CHECK chk_ingredient_status;
ALTER TABLE ingredient DROP CHECK chk_ingredient_storage;

-- 기존 CHECK 제약조건 복원
ALTER TABLE ingredient
ADD CONSTRAINT ingredient_chk_1
CHECK (status IN ('active', 'consumed'));

ALTER TABLE ingredient
ADD CONSTRAINT ingredient_chk_2
CHECK (storage_location IN ('냉장', '냉동', '실온'));
```

또는 백업 파일에서 복원:

```bash
mysql -u user_01 -p leftout < backup_before_migration.sql
```

## ✅ 마이그레이션 체크리스트

- [ ] 데이터베이스 백업 완료
- [ ] MySQL 버전 확인 (8.0.16 이상)
- [ ] 마이그레이션 SQL 실행
- [ ] 테이블 구조 확인 (`DESCRIBE ingredient`)
- [ ] CHECK 제약조건 확인
- [ ] 기존 데이터 손실 없는지 확인 (`SELECT COUNT(*) FROM ingredient`)
- [ ] Spring Boot 애플리케이션 재시작
- [ ] 통계 페이지 접속 테스트 (`http://localhost:8080/statistics`)
- [ ] 폐기 기능 테스트

## 🚀 마이그레이션 후 다음 단계

1. **Spring Boot 애플리케이션 재시작**
   ```bash
   # Gradle을 사용하는 경우
   ./gradlew bootRun
   ```

2. **통계 기능 테스트**
   - http://localhost:8080/ingredient/list 접속
   - 식재료 하나를 선택하여 "🗑️ 폐기" 버튼 클릭
   - http://localhost:8080/statistics 접속하여 통계 확인

## 🐛 문제 해결

### CHECK 제약조건 충돌 오류
```
ERROR 3819 (HY000): Check constraint 'ingredient_chk_1' is violated.
```

**해결방법:** 기존 CHECK 제약조건과 이름이 충돌하는 경우
```sql
-- 기존 제약조건 확인
SELECT CONSTRAINT_NAME FROM information_schema.CHECK_CONSTRAINTS
WHERE TABLE_NAME = 'ingredient';

-- 충돌하는 제약조건 삭제
ALTER TABLE ingredient DROP CHECK ingredient_chk_1;
ALTER TABLE ingredient DROP CHECK ingredient_chk_2;

-- 그 후 새로운 제약조건 추가
```

### 컬럼이 이미 존재하는 경우
```
ERROR 1060 (42S21): Duplicate column name 'discard_date'
```

**해결방법:** 컬럼이 이미 추가되어 있는 경우 - 건너뛰고 다음 단계 진행

## 📞 지원

문제가 발생하면 다음을 확인하세요:
1. MySQL 버전: `SELECT VERSION();`
2. 현재 테이블 구조: `DESCRIBE ingredient;`
3. 에러 로그 확인
