-- ========================================
-- 통계 기능 테스트용 다양한 데이터 삽입 스크립트
-- ========================================

-- 테스트 사용자 생성 (기존에 없는 경우)
INSERT INTO users (username, password, name) VALUES
('testuser1', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cyhQQKJzDxv.xehgUnF0SYQYNkjce', '테스트유저1'),
('testuser2', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cyhQQKJzDxv.xehgUnF0SYQYNkjce', '테스트유저2')
ON DUPLICATE KEY UPDATE username=username;

-- ========================================
-- 1. 소비된 식재료 (다양한 카테고리, 날짜)
-- ========================================

-- 최근 1주일 내 소비
INSERT INTO ingredient (user_id, ingredient_name, category_id, purchase_date, expiry_date, consume_date, quantity, unit, storage_location, status) VALUES
-- 채소류
(1, '양파', 1, DATE_SUB(CURDATE(), INTERVAL 3 DAY), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 3 DAY), INTERVAL 30 DAY), CURDATE(), 2, 'kg', '실온', 'consumed'),
(1, '당근', 1, DATE_SUB(CURDATE(), INTERVAL 5 DAY), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 5 DAY), INTERVAL 14 DAY), DATE_SUB(CURDATE(), INTERVAL 1 DAY), 1.5, 'kg', '냉장', 'consumed'),
(1, '상추', 1, DATE_SUB(CURDATE(), INTERVAL 4 DAY), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 4 DAY), INTERVAL 7 DAY), DATE_SUB(CURDATE(), INTERVAL 2 DAY), 0.5, 'kg', '냉장', 'consumed'),
(1, '시금치', 1, DATE_SUB(CURDATE(), INTERVAL 6 DAY), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 6 DAY), INTERVAL 7 DAY), DATE_SUB(CURDATE(), INTERVAL 3 DAY), 0.8, 'kg', '냉장', 'consumed'),

-- 육류
(1, '소고기', 2, DATE_SUB(CURDATE(), INTERVAL 2 DAY), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 2 DAY), INTERVAL 3 DAY), CURDATE(), 1, 'kg', '냉장', 'consumed'),
(1, '닭고기', 2, DATE_SUB(CURDATE(), INTERVAL 1 DAY), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 1 DAY), INTERVAL 2 DAY), CURDATE(), 1.2, 'kg', '냉장', 'consumed'),
(1, '삼겹살', 2, DATE_SUB(CURDATE(), INTERVAL 4 DAY), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 4 DAY), INTERVAL 3 DAY), DATE_SUB(CURDATE(), INTERVAL 2 DAY), 0.8, 'kg', '냉동', 'consumed'),

-- 유제품
(1, '우유', 3, DATE_SUB(CURDATE(), INTERVAL 5 DAY), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 5 DAY), INTERVAL 7 DAY), DATE_SUB(CURDATE(), INTERVAL 1 DAY), 1, 'L', '냉장', 'consumed'),
(1, '요구르트', 3, DATE_SUB(CURDATE(), INTERVAL 7 DAY), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 7 DAY), INTERVAL 14 DAY), DATE_SUB(CURDATE(), INTERVAL 3 DAY), 4, '개', '냉장', 'consumed'),

-- 과일
(1, '사과', 4, DATE_SUB(CURDATE(), INTERVAL 6 DAY), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 6 DAY), INTERVAL 14 DAY), DATE_SUB(CURDATE(), INTERVAL 2 DAY), 3, '개', '냉장', 'consumed'),
(1, '바나나', 4, DATE_SUB(CURDATE(), INTERVAL 3 DAY), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 3 DAY), INTERVAL 7 DAY), CURDATE(), 5, '개', '실온', 'consumed');

-- 최근 1개월 내 소비
INSERT INTO ingredient (user_id, ingredient_name, category_id, purchase_date, expiry_date, consume_date, quantity, unit, storage_location, status) VALUES
(1, '감자', 1, DATE_SUB(CURDATE(), INTERVAL 25 DAY), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 25 DAY), INTERVAL 30 DAY), DATE_SUB(CURDATE(), INTERVAL 10 DAY), 2, 'kg', '실온', 'consumed'),
(1, '배추', 1, DATE_SUB(CURDATE(), INTERVAL 20 DAY), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 20 DAY), INTERVAL 14 DAY), DATE_SUB(CURDATE(), INTERVAL 8 DAY), 1, '포기', '냉장', 'consumed'),
(1, '브로콜리', 1, DATE_SUB(CURDATE(), INTERVAL 15 DAY), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 15 DAY), INTERVAL 7 DAY), DATE_SUB(CURDATE(), INTERVAL 12 DAY), 2, '송이', '냉장', 'consumed'),
(1, '돼지고기', 2, DATE_SUB(CURDATE(), INTERVAL 18 DAY), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 18 DAY), INTERVAL 3 DAY), DATE_SUB(CURDATE(), INTERVAL 15 DAY), 1.5, 'kg', '냉동', 'consumed'),
(1, '치즈', 3, DATE_SUB(CURDATE(), INTERVAL 22 DAY), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 22 DAY), INTERVAL 30 DAY), DATE_SUB(CURDATE(), INTERVAL 9 DAY), 0.5, 'kg', '냉장', 'consumed'),
(1, '딸기', 4, DATE_SUB(CURDATE(), INTERVAL 28 DAY), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 28 DAY), INTERVAL 5 DAY), DATE_SUB(CURDATE(), INTERVAL 25 DAY), 1, '팩', '냉장', 'consumed'),
(1, '포도', 4, DATE_SUB(CURDATE(), INTERVAL 14 DAY), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 14 DAY), INTERVAL 7 DAY), DATE_SUB(CURDATE(), INTERVAL 11 DAY), 2, '송이', '냉장', 'consumed');

-- 2~3개월 전 소비
INSERT INTO ingredient (user_id, ingredient_name, category_id, purchase_date, expiry_date, consume_date, quantity, unit, storage_location, status) VALUES
(1, '소고기', 2, DATE_SUB(CURDATE(), INTERVAL 65 DAY), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 65 DAY), INTERVAL 3 DAY), DATE_SUB(CURDATE(), INTERVAL 62 DAY), 1, 'kg', '냉동', 'consumed'),
(1, '양파', 1, DATE_SUB(CURDATE(), INTERVAL 75 DAY), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 75 DAY), INTERVAL 30 DAY), DATE_SUB(CURDATE(), INTERVAL 50 DAY), 1.5, 'kg', '실온', 'consumed'),
(1, '계란', 3, DATE_SUB(CURDATE(), INTERVAL 80 DAY), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 80 DAY), INTERVAL 30 DAY), DATE_SUB(CURDATE(), INTERVAL 55 DAY), 10, '개', '냉장', 'consumed'),
(1, '우유', 3, DATE_SUB(CURDATE(), INTERVAL 70 DAY), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 70 DAY), INTERVAL 7 DAY), DATE_SUB(CURDATE(), INTERVAL 67 DAY), 1, 'L', '냉장', 'consumed');

-- ========================================
-- 2. 폐기된 식재료 (유통기한 지남)
-- ========================================

-- 최근 1주일 내 폐기
INSERT INTO ingredient (user_id, ingredient_name, category_id, purchase_date, expiry_date, discard_date, quantity, unit, storage_location, status, memo) VALUES
(1, '상추', 1, DATE_SUB(CURDATE(), INTERVAL 10 DAY), DATE_SUB(CURDATE(), INTERVAL 3 DAY), CURDATE(), 0.3, 'kg', '냉장', 'discarded', '시들어서 폐기'),
(1, '우유', 3, DATE_SUB(CURDATE(), INTERVAL 12 DAY), DATE_SUB(CURDATE(), INTERVAL 5 DAY), DATE_SUB(CURDATE(), INTERVAL 2 DAY), 0.5, 'L', '냉장', 'discarded', '유통기한 지남'),
(1, '딸기', 4, DATE_SUB(CURDATE(), INTERVAL 8 DAY), DATE_SUB(CURDATE(), INTERVAL 3 DAY), DATE_SUB(CURDATE(), INTERVAL 1 DAY), 0.5, '팩', '냉장', 'discarded', '곰팡이 발생');

-- 최근 1개월 내 폐기
INSERT INTO ingredient (user_id, ingredient_name, category_id, purchase_date, expiry_date, discard_date, quantity, unit, storage_location, status, memo) VALUES
(1, '시금치', 1, DATE_SUB(CURDATE(), INTERVAL 25 DAY), DATE_SUB(CURDATE(), INTERVAL 18 DAY), DATE_SUB(CURDATE(), INTERVAL 15 DAY), 0.5, 'kg', '냉장', 'discarded', '변색됨'),
(1, '닭고기', 2, DATE_SUB(CURDATE(), INTERVAL 20 DAY), DATE_SUB(CURDATE(), INTERVAL 18 DAY), DATE_SUB(CURDATE(), INTERVAL 17 DAY), 0.8, 'kg', '냉장', 'discarded', '유통기한 지남'),
(1, '바나나', 4, DATE_SUB(CURDATE(), INTERVAL 22 DAY), DATE_SUB(CURDATE(), INTERVAL 15 DAY), DATE_SUB(CURDATE(), INTERVAL 14 DAY), 3, '개', '실온', 'discarded', '너무 익음'),
(1, '요구르트', 3, DATE_SUB(CURDATE(), INTERVAL 28 DAY), DATE_SUB(CURDATE(), INTERVAL 14 DAY), DATE_SUB(CURDATE(), INTERVAL 10 DAY), 2, '개', '냉장', 'discarded', '유통기한 지남');

-- 2~3개월 전 폐기
INSERT INTO ingredient (user_id, ingredient_name, category_id, purchase_date, expiry_date, discard_date, quantity, unit, storage_location, status, memo) VALUES
(1, '브로콜리', 1, DATE_SUB(CURDATE(), INTERVAL 70 DAY), DATE_SUB(CURDATE(), INTERVAL 63 DAY), DATE_SUB(CURDATE(), INTERVAL 60 DAY), 1, '송이', '냉장', 'discarded', '변색 및 물러짐'),
(1, '돼지고기', 2, DATE_SUB(CURDATE(), INTERVAL 85 DAY), DATE_SUB(CURDATE(), INTERVAL 82 DAY), DATE_SUB(CURDATE(), INTERVAL 80 DAY), 1, 'kg', '냉장', 'discarded', '유통기한 지남');

-- ========================================
-- 3. 현재 보관 중인 식재료 (active)
-- ========================================

-- 유통기한 임박 (3일 이내)
INSERT INTO ingredient (user_id, ingredient_name, category_id, purchase_date, expiry_date, quantity, unit, storage_location, status, memo) VALUES
(1, '우유', 3, DATE_SUB(CURDATE(), INTERVAL 5 DAY), DATE_ADD(CURDATE(), INTERVAL 2 DAY), 1, 'L', '냉장', 'active', '빨리 소비 필요'),
(1, '닭고기', 2, DATE_SUB(CURDATE(), INTERVAL 1 DAY), DATE_ADD(CURDATE(), INTERVAL 1 DAY), 1, 'kg', '냉장', 'active', '오늘 요리 예정'),
(1, '바나나', 4, DATE_SUB(CURDATE(), INTERVAL 4 DAY), DATE_ADD(CURDATE(), INTERVAL 3 DAY), 6, '개', '실온', 'active', '익어가는 중');

-- 유통기한 여유 있음 (1주일 이상)
INSERT INTO ingredient (user_id, ingredient_name, category_id, purchase_date, expiry_date, quantity, unit, storage_location, status, memo) VALUES
(1, '소고기', 2, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 3 DAY), 1.5, 'kg', '냉동', 'active', '스테이크용'),
(1, '당근', 1, DATE_SUB(CURDATE(), INTERVAL 2 DAY), DATE_ADD(CURDATE(), INTERVAL 12 DAY), 2, 'kg', '냉장', 'active', NULL),
(1, '양파', 1, DATE_SUB(CURDATE(), INTERVAL 1 DAY), DATE_ADD(CURDATE(), INTERVAL 29 DAY), 3, 'kg', '실온', 'active', NULL),
(1, '감자', 1, DATE_SUB(CURDATE(), INTERVAL 3 DAY), DATE_ADD(CURDATE(), INTERVAL 27 DAY), 2.5, 'kg', '실온', 'active', NULL),
(1, '계란', 3, DATE_SUB(CURDATE(), INTERVAL 5 DAY), DATE_ADD(CURDATE(), INTERVAL 25 DAY), 20, '개', '냉장', 'active', '신선한 계란'),
(1, '치즈', 3, DATE_SUB(CURDATE(), INTERVAL 7 DAY), DATE_ADD(CURDATE(), INTERVAL 23 DAY), 0.5, 'kg', '냉장', 'active', '체다치즈'),
(1, '사과', 4, DATE_SUB(CURDATE(), INTERVAL 3 DAY), DATE_ADD(CURDATE(), INTERVAL 11 DAY), 5, '개', '냉장', 'active', '홍로'),
(1, '배추', 1, DATE_SUB(CURDATE(), INTERVAL 2 DAY), DATE_ADD(CURDATE(), INTERVAL 12 DAY), 1, '포기', '냉장', 'active', '김장용'),
(1, '파', 1, DATE_SUB(CURDATE(), INTERVAL 1 DAY), DATE_ADD(CURDATE(), INTERVAL 13 DAY), 1, '단', '냉장', 'active', NULL);

-- 조미료 (장기 보관)
INSERT INTO ingredient (user_id, ingredient_name, category_id, purchase_date, expiry_date, quantity, unit, storage_location, status, memo) VALUES
(1, '간장', 5, DATE_SUB(CURDATE(), INTERVAL 30 DAY), DATE_ADD(CURDATE(), INTERVAL 335 DAY), 1, 'L', '실온', 'active', '양조간장'),
(1, '고추장', 5, DATE_SUB(CURDATE(), INTERVAL 45 DAY), DATE_ADD(CURDATE(), INTERVAL 135 DAY), 0.5, 'kg', '냉장', 'active', '태양초'),
(1, '된장', 5, DATE_SUB(CURDATE(), INTERVAL 60 DAY), DATE_ADD(CURDATE(), INTERVAL 120 DAY), 1, 'kg', '냉장', 'active', '재래식');

-- ========================================
-- 4. 두 번째 사용자용 데이터
-- ========================================

INSERT INTO ingredient (user_id, ingredient_name, category_id, purchase_date, expiry_date, consume_date, quantity, unit, storage_location, status) VALUES
(2, '토마토', 1, DATE_SUB(CURDATE(), INTERVAL 4 DAY), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 4 DAY), INTERVAL 7 DAY), DATE_SUB(CURDATE(), INTERVAL 1 DAY), 1, 'kg', '냉장', 'consumed'),
(2, '삼겹살', 2, DATE_SUB(CURDATE(), INTERVAL 2 DAY), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 2 DAY), INTERVAL 3 DAY), CURDATE(), 1, 'kg', '냉장', 'consumed'),
(2, '귤', 4, DATE_SUB(CURDATE(), INTERVAL 10 DAY), DATE_SUB(CURDATE(), INTERVAL 3 DAY), DATE_SUB(CURDATE(), INTERVAL 2 DAY), 2, 'kg', '실온', 'discarded');

INSERT INTO ingredient (user_id, ingredient_name, category_id, purchase_date, expiry_date, quantity, unit, storage_location, status) VALUES
(2, '오이', 1, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 7 DAY), 5, '개', '냉장', 'active'),
(2, '버터', 3, DATE_SUB(CURDATE(), INTERVAL 10 DAY), DATE_ADD(CURDATE(), INTERVAL 80 DAY), 0.5, 'kg', '냉장', 'active'),
(2, '수박', 4, DATE_SUB(CURDATE(), INTERVAL 1 DAY), DATE_ADD(CURDATE(), INTERVAL 6 DAY), 1, '통', '냉장', 'active');

-- ========================================
-- 데이터 삽입 완료 메시지
-- ========================================
--
-- -- 삽입된 데이터 확인 쿼리
-- SELECT
--     u.username,
--     i.status,
--     COUNT(*) as count
-- FROM ingredient i
-- JOIN users u ON i.user_id = u.id
-- GROUP BY u.username, i.status
-- ORDER BY u.username, i.status;
--
-- SELECT
--     c.category_name,
--     i.status,
--     COUNT(*) as count
-- FROM ingredient i
-- JOIN category c ON i.category_id = c.category_id
-- GROUP BY c.category_name, i.status
-- ORDER BY c.category_name, i.status;