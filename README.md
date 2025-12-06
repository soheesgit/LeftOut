# 냉털이 (LeftOut)

냉장고 식재료 관리 및 레시피 추천 웹 애플리케이션

## 프로젝트 소개

**냉털이**는 냉장고에 있는 식재료를 관리하고, 보유한 식재료를 기반으로 만들 수 있는 레시피를 추천해주는 서비스입니다.

"오늘 뭐 먹지?" 고민을 해결해드립니다!

---

## 데모 사이트

**[https://leftout-koi6.onrender.com/](https://leftout-koi6.onrender.com/)**

> **참고**: 이 서비스는 Render 무료 플랜으로 배포되어 있습니다.
> - 일정 시간(약 15분) 동안 요청이 없으면 서버가 휴면 상태(Sleep)로 전환됩니다.
> - 첫 접속 시 서버가 깨어나는 데 **3분~5분 정도** 소요될 수 있습니다.
> - 잠시 기다리시면 정상적으로 이용 가능합니다!

| 배포 환경 | 서비스 |
|-----------|--------|
| 호스팅 | [Render](https://render.com/) |
| 데이터베이스 | [HelioHost](https://heliohost.org/) MySQL |

---

## 주요 기능
<img width="800" alt="image" src="https://github.com/user-attachments/assets/ad7c721e-4fa5-42a8-8ad6-e86a78e5aac2" />

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


