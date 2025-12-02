-- 기존 book 테이블 삭제
DROP TABLE IF EXISTS book;

-- 테이블 삭제 순서 (외래키 제약조건 고려)
DROP TABLE IF EXISTS ingredient;
DROP TABLE IF EXISTS ingredient_default_expiry;
DROP TABLE IF EXISTS category;
DROP TABLE IF EXISTS users;

DROP DATABASE IF EXISTS leftout;
CREATE DATABASE leftout;
USE leftout;


-- user_01 권한 주기
CREATE USER 'user_01'@'localhost' IDENTIFIED BY '1234';
ALTER USER 'user_01'@'localhost' IDENTIFIED BY '1234';

GRANT ALL PRIVILEGES ON leftout.* TO 'user_01'@'localhost';

FLUSH PRIVILEGES;

-- 사용자 관리 시스템

-- 0. 사용자 테이블
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 식재료 관리 시스템 테이블 생성

-- 1. 카테고리 테이블
CREATE TABLE category (
    category_id INT PRIMARY KEY AUTO_INCREMENT,
    category_name VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. 식재료 기본 유통기한 테이블
CREATE TABLE ingredient_default_expiry (
    ingredient_name VARCHAR(100) PRIMARY KEY,
    default_expiry_days INT NOT NULL,
    category_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES category(category_id)
);

-- 3. 식재료 테이블 (사용자별 냉장고 관리)
CREATE TABLE ingredient (
    ingredient_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    ingredient_name VARCHAR(100) NOT NULL,
    category_id INT,
    purchase_date DATE NOT NULL,
    expiry_date DATE NOT NULL,
    consume_date DATE,
    discard_date DATE,
    quantity DECIMAL(10,2),
    unit VARCHAR(20),
    memo TEXT,
    storage_location VARCHAR(20) DEFAULT '냉장',
    status VARCHAR(20) DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES category(category_id),
    CHECK (status IN ('active', 'consumed', 'discarded')),
    CHECK (storage_location IN ('냉장', '냉동', '실온'))
);

-- 기본 카테고리 데이터 삽입
INSERT INTO category (category_name) VALUES
('채소'),
('육류'),
('유제품'),
('과일'),
('조미료'),
('기타');

-- 기본 식재료 유통기한 데이터 삽입 (일 단위)
INSERT INTO ingredient_default_expiry (ingredient_name, default_expiry_days, category_id) VALUES
-- 채소
('계란', 30, 3),
('양파', 30, 1),
('감자', 30, 1),
('당근', 14, 1),
('배추', 14, 1),
('상추', 7, 1),
('시금치', 7, 1),
('브로콜리', 7, 1),
('파', 14, 1),
('마늘', 90, 1),

-- 육류
('소고기', 3, 2),
('돼지고기', 3, 2),
('닭고기', 2, 2),
('삼겹살', 3, 2),

-- 유제품
('우유', 7, 3),
('요구르트', 14, 3),
('치즈', 30, 3),
('버터', 90, 3),

-- 과일
('사과', 14, 4),
('바나나', 7, 4),
('딸기', 5, 4),
('포도', 7, 4),
('귤', 14, 4),
('수박', 7, 4),

-- 조미료
('간장', 365, 5),
('된장', 180, 5),
('고추장', 180, 5),
('식용유', 365, 5),
('소금', 1000, 5),
('설탕', 365, 5);
