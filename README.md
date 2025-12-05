# 냉털이 (LeftOut)

냉장고 식재료 관리 및 레시피 추천 웹 애플리케이션

## 프로젝트 소개

**냉털이**는 냉장고에 있는 식재료를 관리하고, 보유한 식재료를 기반으로 만들 수 있는 레시피를 추천해주는 서비스입니다.

"오늘 뭐 먹지?" 고민을 해결해드립니다!

---

## 데모 사이트

**[https://leftout-koi6.onrender.com/](https://leftout-koi6.onrender.com/)**

> **참고**: 이 서비스는 Render 무료 플랜으로 배포되어 있습니다.
> - 일정 시간(약 15분) 동안 요청이 없으면 서버가 **휴면 상태(Sleep)**로 전환됩니다.
> - 첫 접속 시 서버가 깨어나는 데 **3분~5분 정도** 소요될 수 있습니다.
> - 잠시 기다리시면 정상적으로 이용 가능합니다!

| 배포 환경 | 서비스 |
|-----------|--------|
| 호스팅 | [Render](https://render.com/) |
| 데이터베이스 | [HelioHost](https://heliohost.org/) MySQL |

---

## 주요 기능

### 1. 식재료 관리
- 내 냉장고 식재료 조회/추가/수정/삭제
- 빠른 추가 (간편 등록)
- 상세 추가 (유통기한, 수량 등)
- **AI 음식 인식** - 사진으로 식재료 자동 인식 및 추가
- 카테고리별 분류 (채소, 육류, 유제품, 과일, 조미료, 해산물 등)

### 2. 레시피 검색
- **보유 식재료 기반 레시피 매칭**
- 매칭률 표시 (보유 재료 몇 % 일치)
- 조리방법별 필터 (굽기, 끓이기, 볶기, 찌기 등)
- 요리종류별 필터 (반찬, 국&찌개, 밥, 일품, 후식 등)
- 레시피명/재료명 검색

### 3. 레시피 게시판
- 사용자 레시피 작성/수정/삭제
- 댓글 기능
- 좋아요(찜) 기능
- 이미지 업로드

### 4. 랜덤 레시피 추천
- "오늘 뭐 먹지?" 랜덤 추천
- 로그인 시 보유 식재료 가중치 적용

### 5. 통계
- 카테고리별 식재료 현황
- 유통기한 임박 식재료 알림

---

## 기술 스택

| 구분 | 기술 |
|------|------|
| Backend | Spring Boot 3.5.6, Java 17 |
| Database | MySQL 8.0 |
| ORM | MyBatis |
| Frontend | Thymeleaf, HTML/CSS/JavaScript |
| Build | Gradle |
| External API | 식품안전나라 공공 API, Hugging Face API |

---

## 설치 및 실행

### 요구사항
- Java 17 이상
- MySQL 8.0 이상
- Gradle 7.0 이상

### 1. 환경변수 설정

```bash
# 데이터베이스
DB_HOST=localhost
DB_PORT=3306
DB_NAME=leftout
DB_USERNAME=your_username
DB_PASSWORD=your_password

# 식품안전나라 API (선택)
API_KEY=your_api_key

# Hugging Face API (선택 - AI 인식 기능)
HF_API_KEY=your_hf_api_key
```

### 2. 데이터베이스 설정

MySQL에서 다음 SQL 파일을 순서대로 실행:

```bash
# 1. 기본 스키마 (사용자, 식재료, 카테고리)
database/1_schema.sql

# 2. 매칭 테이블
database/2_user_recipe_match_schema.sql

# 3. 레시피 테이블
database/3_user_recipe_schema.sql


# 3. 레시피 테이블
database/4_user_recipe_match.sql
```

### 3. 빌드 및 실행

```bash
# 빌드
./gradlew build

# 실행
./gradlew bootRun
```

### 4. 접속

브라우저에서 `http://localhost:8080` 접속

---

## 프로젝트 구조

```
src/main/java/com/example/demo/TEST_001/
├── controller/
│   ├── IndexController.java          # 메인 페이지
│   ├── AuthController.java           # 로그인/회원가입
│   ├── IngredientController.java     # 식재료 관리
│   ├── RecipeController.java         # 레시피 검색/상세
│   ├── UserRecipeController.java     # 사용자 레시피 게시판
│   ├── FoodRecognitionController.java # AI 음식 인식
│   └── StatisticsController.java     # 통계
├── service/
│   ├── IngredientService.java
│   ├── RecipeService.java
│   ├── UserRecipeService.java
│   ├── FoodRecognitionService.java
│   └── ...
├── repository/
│   └── ...
└── dto/
    └── ...

src/main/resources/
├── mapper/                           # MyBatis XML
├── templates/                        # Thymeleaf 템플릿
└── application.yml                   # 설정 파일

database/
├── 1_schema.sql                      # 기본 스키마
├── 2_user_recipe_match_schema.sql    # 매칭 테이블
└── 3_user_recipe_schema.sql          # 레시피 테이블
```

---

## 주요 API 엔드포인트

### 식재료
| Method | URL | 설명 |
|--------|-----|------|
| GET | `/ingredient/list` | 내 식재료 목록 |
| GET | `/ingredient/quick-add` | 빠른 추가 폼 |
| GET | `/ingredient/add` | 상세 추가 폼 |
| GET | `/ingredient/ai-recognition` | AI 인식 페이지 |

### 레시피
| Method | URL | 설명 |
|--------|-----|------|
| GET | `/recipe/list` | 레시피 목록 (매칭 포함) |
| GET | `/recipe/detail/{id}` | 레시피 상세 |
| GET | `/recipe/random` | 랜덤 추천 |

### 사용자 레시피
| Method | URL | 설명 |
|--------|-----|------|
| GET | `/recipe/my-recipes` | 내 레시피 목록 |
| GET | `/recipe/create` | 레시피 작성 폼 |
| POST | `/recipe/create` | 레시피 등록 |
| GET | `/recipe/edit/{id}` | 레시피 수정 폼 |
| POST | `/recipe/delete/{id}` | 레시피 삭제 |

---