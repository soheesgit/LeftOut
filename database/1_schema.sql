-- -- 기존 book 테이블 삭제
-- DROP TABLE IF EXISTS book;
--
-- -- 테이블 삭제 순서 (외래키 제약조건 고려)
-- DROP TABLE IF EXISTS ingredient;
-- DROP TABLE IF EXISTS ingredient_default_expiry;
-- DROP TABLE IF EXISTS category;
-- DROP TABLE IF EXISTS users;
--
-- DROP DATABASE IF EXISTS leftout;
-- CREATE DATABASE leftout;
-- USE leftout;


-- user_01 권한 주기
-- CREATE USER 'user_01'@'localhost' IDENTIFIED BY '1234';
-- ALTER USER 'user_01'@'localhost' IDENTIFIED BY '1234';
--
-- GRANT ALL PRIVILEGES ON leftout.* TO 'user_01'@'localhost';
--
-- FLUSH PRIVILEGES;

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
('기타'),
('해산물');

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
('설탕', 365, 5),

-- ========== AI 이미지 인식용 식재료 (ImageNet-1k) ==========

-- 과일류 (category_id: 4)
('오렌지', 14, 4),
('레몬', 21, 4),
('무화과', 5, 4),
('파인애플', 5, 4),
('석류', 14, 4),
('슈가애플', 5, 4),
('잭프루트', 7, 4),

-- 채소류 (category_id: 1)
('콜리플라워', 7, 1),
('오이', 7, 1),
('주키니호박', 7, 1),
('아티초크', 7, 1),
('피망', 7, 1),
('버섯', 5, 1),
('주름버섯', 5, 1),
('잎새버섯', 7, 1),
('그물버섯', 5, 1),
('목이버섯', 14, 1),
('싸리버섯', 5, 1),
('스파게티호박', 14, 1),
('버터넛호박', 30, 1),
('도토리호박', 30, 1),
('옥수수', 5, 1),

-- 빵/곡류 (category_id: 6 - 기타)
('베이글', 5, 6),
('프레첼', 7, 6),
('치즈버거', 1, 6),
('핫도그', 1, 6),
('피자', 3, 6),
('부리토', 2, 6),
('미트로프', 3, 6),
('바게트', 3, 6),
('과카몰리', 3, 6),
('콩소메', 3, 6),
('팟파이', 3, 6),
('카르보나라', 2, 6),

-- 디저트류 (category_id: 6 - 기타)
('아이스크림', 30, 6),
('아이스바', 180, 6),
('초콜릿소스', 180, 6),
('트라이플', 3, 6),

-- 음료 (category_id: 6 - 기타)
('에스프레소', 1, 6),
('에그노그', 5, 6),
('레드와인', 365, 6),

-- 해산물 (category_id: 7 - 해산물, 신선도 중요)
('랍스터', 2, 7),
('가재', 2, 7),
('킹크랩', 2, 7),
('소라', 3, 7),
('해삼', 3, 7),
('성게', 2, 7),
('복어', 2, 7),
('철갑상어', 2, 7),
('장어', 2, 7),
('은연어', 2, 7),

-- 가금류 (category_id: 2 - 육류)
('닭', 2, 2),
('칠면조', 3, 2),
('거위', 3, 2),
('오리', 3, 2),

-- ========== AI 매핑 누락 식재료 추가 ==========

-- 채소류 (category_id: 1)
('양배추', 14, 1),
('카르돈', 7, 1),

-- 기타 (category_id: 6)
('도토리', 30, 6),
('반죽', 3, 6),

-- 해산물 추가 (category_id: 7)
('아메리칸랍스터', 2, 7),
('가시랍스터', 2, 7),
('던지니스크랩', 2, 7),
('돌게', 2, 7),
('농게', 2, 7),
('소라게', 2, 7),
('등각류', 2, 7),
('달팽이', 2, 7),
('민달팽이', 2, 7),
('군부', 2, 7),
('갯민숭달팽이', 2, 7),
('불가사리', 2, 7),

-- 생선류 추가 (category_id: 7)
('물고기', 2, 7),
('잉어', 2, 7),
('가아', 2, 7),
('쏠배감펭', 2, 7),
('꼬치고기', 2, 7),

-- 가금류 추가 (category_id: 2 - 육류)
('수탉', 2, 2),
('수오리', 3, 2),
('바다비오리', 3, 2);
