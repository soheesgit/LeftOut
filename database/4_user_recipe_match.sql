-- ========================================
-- 사용자-레시피 매칭 점수 테이블
-- 성능 최적화: 매칭 점수를 미리 계산하여 저장
-- ========================================

DROP TABLE IF EXISTS user_recipe_match;

CREATE TABLE user_recipe_match (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    recipe_id BIGINT NOT NULL,
    matched_count INT DEFAULT 0,           -- 매칭된 재료 수
    total_count INT DEFAULT 0,             -- 레시피 전체 재료 수
    match_percent DECIMAL(5,2) DEFAULT 0,  -- 매칭 퍼센트 (0~100)
    matched_ingredients TEXT,              -- 매칭된 재료 목록 (콤마 구분)
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE KEY unique_user_recipe (user_id, recipe_id),
    INDEX idx_user_match_percent (user_id, match_percent DESC),
    INDEX idx_recipe_id (recipe_id),

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (recipe_id) REFERENCES user_recipe(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

