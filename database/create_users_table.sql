-- users 테이블 생성 스크립트
-- MySQL 데이터베이스: dbp_week3

USE leftout;

-- 기존 테이블이 있다면 삭제 (선택사항)
-- DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '사용자 ID (PK)',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '로그인 아이디',
    password VARCHAR(255) NOT NULL COMMENT '암호화된 비밀번호',
    name VARCHAR(100) NOT NULL COMMENT '사용자 이름',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '가입일시'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='사용자 정보 테이블';

-- 인덱스 생성 (로그인 성능 향상)
CREATE INDEX idx_username ON users(username);

-- 테이블 생성 확인
DESC users;
