-- 이메일 알림 기능을 위한 스키마 변경
-- 실행 전 users 테이블이 존재하는지 확인하세요

-- users 테이블에 이메일 관련 컬럼 추가
ALTER TABLE users ADD COLUMN email VARCHAR(255) DEFAULT NULL;
ALTER TABLE users ADD COLUMN email_notification_enabled BOOLEAN DEFAULT FALSE;

-- 이메일 중복 체크를 위한 유니크 인덱스 (NULL은 중복 허용)
ALTER TABLE users ADD UNIQUE INDEX idx_users_email (email);
