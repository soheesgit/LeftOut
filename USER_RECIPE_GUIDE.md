# 사용자 레시피 게시판 사용 가이드

## 구현 완료된 기능

### 1. 레시피 CRUD
- ✅ **레시피 작성**: 제목, 설명, 재료, 조리 단계, 이미지 업로드
- ✅ **레시피 조회**: 전체 레시피 목록, 내 레시피 목록, 상세보기
- ✅ **레시피 수정**: 작성자만 수정 가능
- ✅ **레시피 삭제**: 작성자만 삭제 가능

### 2. 댓글 기능
- ✅ **댓글 작성**: 로그인한 모든 사용자
- ✅ **댓글 삭제**: 작성자만 가능

### 3. 좋아요(찜) 기능
- ✅ **좋아요 추가/취소**: 토글 방식
- ✅ **좋아요한 레시피 목록**: 내가 좋아요한 레시피 모아보기

### 4. 검색 기능
- ✅ 레시피 제목, 설명, 재료로 검색

### 5. 이미지 업로드
- ✅ 레시피 대표 이미지 업로드
- ✅ 지원 형식: JPG, JPEG, PNG, GIF
- ✅ 최대 파일 크기: 10MB

---

## 데이터베이스 설정

### 1. 스키마 실행

MySQL Workbench에서 다음 파일을 순서대로 실행하세요:

```sql
-- 1. 사용자 레시피 테이블 생성
database/3_user_recipe_schema.sql
```

이 스크립트는 다음 테이블을 생성합니다:
- `user_recipe`: 사용자 작성 레시피
- `recipe_comment`: 댓글
- `recipe_like`: 좋아요

### 2. 테스트 데이터 삽입 (선택)

스키마 파일에 이미 샘플 데이터가 포함되어 있습니다:
- 3개의 샘플 레시피
- 댓글 및 좋아요 데이터

---

## 애플리케이션 실행

### 1. 빌드

```bash
./gradlew build
```

### 2. 실행

```bash
./gradlew bootRun
```

또는 IDE에서 `Test001Application.java` 실행

### 3. 접속

브라우저에서 `http://localhost:8080` 접속

---

## 사용 방법

### 1. 회원가입 및 로그인

1. `http://localhost:8080/signup` 접속
2. 회원가입 후 로그인

### 2. 레시피 작성

1. 로그인 후 홈 화면에서 "레시피 게시판" 메뉴 클릭
2. "새 레시피 작성" 버튼 클릭
3. 레시피 정보 입력:
   - 제목 (필수)
   - 설명
   - 재료 (필수): "재료 추가" 버튼으로 여러 개 추가 가능
   - 조리 단계 (필수): "단계 추가" 버튼으로 여러 단계 추가
   - 준비 시간, 조리 시간, 인분, 난이도
   - 대표 이미지
4. "레시피 등록" 버튼 클릭

### 3. 레시피 조회

- **전체 레시피**: `/recipe/all-recipes`
- **내 레시피**: `/recipe/my-recipes`
- **좋아요한 레시피**: `/recipe/liked-recipes`

### 4. 레시피 수정/삭제

1. 레시피 상세 페이지에서 "수정" 또는 "삭제" 버튼 클릭
2. 본인이 작성한 레시피만 수정/삭제 가능

### 5. 댓글 작성

1. 레시피 상세 페이지 하단 댓글 입력란에 내용 작성
2. "댓글 작성" 버튼 클릭
3. 본인이 작성한 댓글은 삭제 가능

### 6. 좋아요

1. 레시피 상세 페이지에서 "❤" 버튼 클릭
2. 다시 클릭하면 좋아요 취소
3. 실시간으로 좋아요 수 업데이트

---

## API 엔드포인트

### 레시피 관련

| Method | URL | 설명 |
|--------|-----|------|
| GET | `/recipe/my-recipes` | 내 레시피 목록 |
| GET | `/recipe/all-recipes` | 전체 레시피 목록 |
| GET | `/recipe/liked-recipes` | 좋아요한 레시피 목록 |
| GET | `/recipe/create` | 레시피 작성 폼 |
| POST | `/recipe/create` | 레시피 생성 |
| GET | `/recipe/user-recipe/{id}` | 레시피 상세보기 |
| GET | `/recipe/edit/{id}` | 레시피 수정 폼 |
| POST | `/recipe/edit/{id}` | 레시피 수정 |
| POST | `/recipe/delete/{id}` | 레시피 삭제 |

### 댓글 관련

| Method | URL | 설명 |
|--------|-----|------|
| POST | `/recipe/{recipeId}/comment` | 댓글 작성 |
| POST | `/recipe/comment/{commentId}/edit` | 댓글 수정 |
| POST | `/recipe/comment/{commentId}/delete` | 댓글 삭제 |

### 좋아요 관련

| Method | URL | 설명 |
|--------|-----|------|
| POST | `/recipe/{recipeId}/like` | 좋아요 추가/취소 (토글) |

---

## 파일 구조

### 백엔드

```
src/main/java/com/example/demo/TEST_001/
├── controller/
│   └── UserRecipeController.java        # 레시피 게시판 컨트롤러
├── service/
│   ├── UserRecipeService.java           # 레시피 비즈니스 로직
│   ├── RecipeCommentService.java        # 댓글 비즈니스 로직
│   ├── RecipeLikeService.java           # 좋아요 비즈니스 로직
│   └── FileUploadService.java           # 파일 업로드 처리
├── repository/
│   ├── UserRecipeRepository.java        # 레시피 데이터 접근
│   ├── RecipeCommentRepository.java     # 댓글 데이터 접근
│   └── RecipeLikeRepository.java        # 좋아요 데이터 접근
└── dto/
    ├── UserRecipeDTO.java               # 레시피 DTO
    ├── RecipeCommentDTO.java            # 댓글 DTO
    ├── RecipeLikeDTO.java               # 좋아요 DTO
    ├── RecipeStepDTO.java               # 조리 단계 DTO
    └── IngredientItemDTO.java           # 재료 항목 DTO
```

### MyBatis Mapper

```
src/main/resources/mapper/
├── user-recipe-mapper.xml               # 레시피 SQL
├── recipe-comment-mapper.xml            # 댓글 SQL
└── recipe-like-mapper.xml               # 좋아요 SQL
```

### 프론트엔드

```
src/main/resources/templates/
├── myRecipes.html                       # 내 레시피 목록
├── allRecipes.html                      # 전체 레시피 목록
├── createRecipe.html                    # 레시피 작성 폼
├── editRecipe.html                      # 레시피 수정 폼
└── userRecipeDetail.html                # 레시피 상세보기
```

### 데이터베이스

```
database/
└── 3_user_recipe_schema.sql               # 레시피 테이블 스키마 및 샘플 데이터
```

---

## 주요 기능 설명

### 1. 권한 검증

- 레시피 수정/삭제: 작성자만 가능
- 댓글 수정/삭제: 작성자만 가능
- 모든 작성 기능: 로그인 필수

Service 계층에서 `SecurityException`을 던져 권한 오류 처리

### 2. JSON 데이터 처리

재료와 조리 단계는 JSON 형식으로 DB에 저장:

```json
// 재료
[
  {"name": "김치", "amount": "200g"},
  {"name": "밥", "amount": "2공기"}
]

// 조리 단계
[
  {"step": 1, "description": "김치를 잘게 썰어주세요.", "imagePath": null},
  {"step": 2, "description": "달군 팬에 식용유를 두르고 김치를 볶습니다.", "imagePath": null}
]
```

### 3. 파일 업로드

- 업로드 경로: `src/main/resources/static/uploads/recipes/`
- 파일명: UUID로 생성하여 중복 방지
- 지원 형식: JPG, JPEG, PNG, GIF
- 최대 크기: 10MB

### 4. 캐시 데이터

성능 향상을 위해 `user_recipe` 테이블에 캐시 필드 사용:
- `like_count`: 좋아요 수
- `comment_count`: 댓글 수
- `view_count`: 조회수

좋아요/댓글 추가/삭제 시 자동으로 업데이트됩니다.

---

## 트러블슈팅

### 1. 이미지 업로드 실패

- 업로드 디렉토리가 존재하는지 확인
- 파일 크기가 10MB 이하인지 확인
- 지원하는 형식(JPG, JPEG, PNG, GIF)인지 확인

### 2. 빌드 오류

```bash
./gradlew clean build
```

### 3. 데이터베이스 연결 오류

- MySQL이 실행 중인지 확인
- `application.yml`의 DB 설정 확인:
  - URL: `jdbc:mysql://127.0.0.1:3306/leftout`
  - Username: `user_01`
  - Password: `1234`

### 4. 권한 오류

- 로그인 상태 확인
- 세션이 유지되고 있는지 확인

---

## 개선 가능한 부분

1. **페이징 개선**: 현재는 기본 페이징, 무한 스크롤로 개선 가능
2. **이미지 다중 업로드**: 조리 단계별 이미지 업로드
3. **레시피 카테고리**: 한식, 중식, 양식 등 카테고리 분류
4. **레시피 평점**: 댓글에 평점 추가
5. **레시피 공유**: SNS 공유 기능
6. **재료 자동완성**: 기존 재료 DB와 연동하여 자동완성
7. **영양 정보**: 칼로리, 영양소 정보 추가

---

## 라이센스 및 문의

이 프로젝트는 교육 목적으로 작성되었습니다.

문의사항이 있으시면 이슈를 등록해주세요.
