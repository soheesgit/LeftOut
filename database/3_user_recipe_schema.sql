-- ========================================
-- 사용자 레시피 게시판 시스템 스키마
-- ========================================

-- 기존 테이블 삭제 (외래키 제약조건 고려)
DROP TABLE IF EXISTS recipe_like;
DROP TABLE IF EXISTS recipe_comment;
DROP TABLE IF EXISTS user_recipe;

-- ========================================
-- 1. 사용자 레시피 테이블
-- ========================================
CREATE TABLE user_recipe (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,                          -- API 레시피는 NULL
    title VARCHAR(255) NOT NULL,
    description TEXT,
    ingredients TEXT,                        -- JSON 형식으로 재료 목록 저장 (사용자 레시피용)
    cooking_steps TEXT,                      -- JSON 형식으로 조리 단계 저장 (사용자 레시피용)
    preparation_time INT,                   -- 준비 시간 (분)
    cooking_time INT,                       -- 조리 시간 (분)
    servings INT,                           -- 몇 인분
    difficulty_level VARCHAR(20),           -- 난이도: 'easy', 'medium', 'hard'
    main_image_path VARCHAR(500),           -- 대표 이미지 경로
    view_count INT DEFAULT 0,               -- 조회수
    like_count INT DEFAULT 0,               -- 좋아요 수 (캐시)
    comment_count INT DEFAULT 0,            -- 댓글 수 (캐시)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- API 레시피 통합용 필드
    source VARCHAR(20) DEFAULT 'user',      -- 'user' 또는 'api'
    rcp_seq VARCHAR(50),                    -- API 레시피 고유번호
    rcp_way2 VARCHAR(50),                   -- 조리방법 (굽기, 찌기 등)
    rcp_pat2 VARCHAR(50),                   -- 요리종류 (반찬, 국 등)
    rcp_parts_dtls TEXT,                    -- 재료정보 (API 레시피용)
    info_wgt VARCHAR(50),                   -- 중량
    info_eng VARCHAR(50),                   -- 열량
    info_car VARCHAR(50),                   -- 탄수화물
    info_pro VARCHAR(50),                   -- 단백질
    info_fat VARCHAR(50),                   -- 지방
    info_na VARCHAR(50),                    -- 나트륨
    rcp_na_tip TEXT,                        -- 저감 조리법 TIP
    hash_tag VARCHAR(255),                  -- 해시태그
    att_file_no_main VARCHAR(500),          -- 이미지경로(소)
    att_file_no_mk VARCHAR(500),            -- 이미지경로(대)
    manual01 TEXT, manual02 TEXT, manual03 TEXT, manual04 TEXT, manual05 TEXT,
    manual06 TEXT, manual07 TEXT, manual08 TEXT, manual09 TEXT, manual10 TEXT,
    manual11 TEXT, manual12 TEXT, manual13 TEXT, manual14 TEXT, manual15 TEXT,
    manual16 TEXT, manual17 TEXT, manual18 TEXT, manual19 TEXT, manual20 TEXT,
    manual_img01 VARCHAR(500), manual_img02 VARCHAR(500), manual_img03 VARCHAR(500),
    manual_img04 VARCHAR(500), manual_img05 VARCHAR(500), manual_img06 VARCHAR(500),
    manual_img07 VARCHAR(500), manual_img08 VARCHAR(500), manual_img09 VARCHAR(500),
    manual_img10 VARCHAR(500), manual_img11 VARCHAR(500), manual_img12 VARCHAR(500),
    manual_img13 VARCHAR(500), manual_img14 VARCHAR(500), manual_img15 VARCHAR(500),
    manual_img16 VARCHAR(500), manual_img17 VARCHAR(500), manual_img18 VARCHAR(500),
    manual_img19 VARCHAR(500), manual_img20 VARCHAR(500),

    -- 성능 최적화용 (미리 파싱된 재료)
    parsed_ingredients TEXT,                 -- JSON 배열: ["소금", "설탕", ...]
    ingredient_count INT DEFAULT 0,          -- 재료 개수

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at DESC),
    INDEX idx_like_count (like_count DESC),
    INDEX idx_source (source),
    UNIQUE INDEX idx_rcp_seq (rcp_seq)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- 2. 레시피 댓글 테이블
-- ========================================
CREATE TABLE recipe_comment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    recipe_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (recipe_id) REFERENCES user_recipe(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_recipe_id (recipe_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- 3. 레시피 좋아요 테이블
-- ========================================
CREATE TABLE recipe_like (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    recipe_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (recipe_id) REFERENCES user_recipe(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_recipe_like (user_id, recipe_id),
    INDEX idx_recipe_id (recipe_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ========================================
-- 테스트 데이터 삽입
-- ========================================

-- 사용자 레시피 샘플 데이터 (user_id=1 기준)
INSERT INTO user_recipe (user_id, title, description, ingredients, cooking_steps, preparation_time, cooking_time, servings, difficulty_level, main_image_path) VALUES
(1, '김치볶음밥', '간단하고 맛있는 김치볶음밥 레시피입니다.',
'[{"name":"김치","amount":"200g"},{"name":"밥","amount":"2공기"},{"name":"식용유","amount":"2큰술"},{"name":"참기름","amount":"1큰술"},{"name":"김가루","amount":"약간"}]',
'[{"step":1,"description":"김치를 잘게 썰어주세요.","imagePath":null},{"step":2,"description":"달군 팬에 식용유를 두르고 김치를 볶습니다.","imagePath":null},{"step":3,"description":"밥을 넣고 잘 섞어가며 볶아주세요.","imagePath":null},{"step":4,"description":"참기름을 넣고 마무리합니다.","imagePath":null}]',
10, 10, 2, 'easy', null),

(1, '된장찌개', '구수한 된장찌개 만드는 법',
'[{"name":"된장","amount":"2큰술"},{"name":"두부","amount":"1/2모"},{"name":"감자","amount":"1개"},{"name":"양파","amount":"1/2개"},{"name":"애호박","amount":"1/2개"},{"name":"대파","amount":"1대"},{"name":"마늘","amount":"1큰술"}]',
'[{"step":1,"description":"감자, 양파, 호박을 먹기 좋은 크기로 썰어주세요.","imagePath":null},{"step":2,"description":"냄비에 물을 붓고 된장을 풀어줍니다.","imagePath":null},{"step":3,"description":"감자와 양파를 먼저 넣고 끓입니다.","imagePath":null},{"step":4,"description":"두부와 호박, 대파를 넣고 한소끔 끓여 완성합니다.","imagePath":null}]',
15, 20, 4, 'easy', null),

(1, '스테이크 구이', '완벽한 미디엄 스테이크 굽는 법',
'[{"name":"소고기 등심","amount":"300g"},{"name":"소금","amount":"적당량"},{"name":"후추","amount":"적당량"},{"name":"올리브유","amount":"2큰술"},{"name":"마늘","amount":"3쪽"},{"name":"버터","amount":"30g"}]',
'[{"step":1,"description":"고기를 실온에 30분 정도 꺼내두세요.","imagePath":null},{"step":2,"description":"소금과 후추로 간을 합니다.","imagePath":null},{"step":3,"description":"팬을 강불로 달구고 올리브유를 두릅니다.","imagePath":null},{"step":4,"description":"고기를 앞뒤로 3-4분씩 구워주세요.","imagePath":null},{"step":5,"description":"마늘과 버터를 넣고 베이스팅합니다.","imagePath":null},{"step":6,"description":"불을 끄고 5분간 휴지시킨 후 서빙합니다.","imagePath":null}]',
35, 15, 2, 'medium', null);

-- 두 번째 사용자 레시피 (user_id=2 기준, 존재하는 경우)
INSERT INTO user_recipe (user_id, title, description, ingredients, cooking_steps, preparation_time, cooking_time, servings, difficulty_level, main_image_path)
SELECT 2, '토마토 파스타', '상큼한 토마토 파스타',
'[{"name":"스파게티 면","amount":"200g"},{"name":"토마토","amount":"4개"},{"name":"마늘","amount":"4쪽"},{"name":"올리브유","amount":"3큰술"},{"name":"바질","amount":"약간"}]',
'[{"step":1,"description":"물을 끓여 파스타 면을 삶아주세요.","imagePath":null},{"step":2,"description":"토마토를 다져주세요.","imagePath":null},{"step":3,"description":"팬에 올리브유와 마늘을 볶습니다.","imagePath":null},{"step":4,"description":"토마토를 넣고 소스를 만듭니다.","imagePath":null},{"step":5,"description":"삶은 면과 소스를 버무려 완성합니다.","imagePath":null}]',
10, 20, 2, 'easy', null
FROM users WHERE id = 2 LIMIT 1;

-- 댓글 샘플 데이터
INSERT INTO recipe_comment (recipe_id, user_id, content) VALUES
(1, 1, '제가 만든 레시피인데 정말 맛있어요!'),
(1, 2, '김치볶음밥 진짜 맛있게 먹었습니다. 감사합니다!'),
(2, 2, '된장찌개 레시피 따라했는데 대박이에요!'),
(3, 2, '스테이크 완전 성공했어요. 가족들이 엄청 좋아했습니다.');

-- 좋아요 샘플 데이터
INSERT INTO recipe_like (recipe_id, user_id) VALUES
(1, 2),
(2, 2),
(3, 2);

SET SQL_SAFE_UPDATES = 0;
-- 캐시 데이터 업데이트 (좋아요 수, 댓글 수)
UPDATE user_recipe ur
SET like_count = (SELECT COUNT(*) FROM recipe_like WHERE recipe_id = ur.id),
    comment_count = (SELECT COUNT(*) FROM recipe_comment WHERE recipe_id = ur.id);
SET SQL_SAFE_UPDATES = 1;
-- ========================================
-- 확인 쿼리
-- ========================================
--
-- -- 데이터 확인
-- SELECT
--     ur.id,
--     ur.title,
--     u.username AS author,
--     ur.like_count,
--     ur.comment_count,
--     ur.created_at
-- FROM user_recipe ur
-- JOIN users u ON ur.user_id = u.id
-- ORDER BY ur.created_at DESC;
--
-- SELECT
--     rc.id,
--     ur.title AS recipe_title,
--     u.username AS commenter,
--     rc.content,
--     rc.created_at
-- FROM recipe_comment rc
-- JOIN user_recipe ur ON rc.recipe_id = ur.id
-- JOIN users u ON rc.user_id = u.id
-- ORDER BY rc.created_at DESC;
--
-- SELECT
--     rl.id,
--     ur.title AS recipe_title,
--     u.username AS liked_by
-- FROM recipe_like rl
-- JOIN user_recipe ur ON rl.recipe_id = ur.id
-- JOIN users u ON rl.user_id = u.id;
