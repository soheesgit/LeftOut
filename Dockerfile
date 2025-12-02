# ===== 빌드 스테이지 =====
FROM gradle:8-jdk17 AS builder
WORKDIR /app

# Gradle 캐시 활용을 위해 의존성 파일 먼저 복사
COPY build.gradle settings.gradle ./
COPY gradle ./gradle

# 의존성 다운로드 (캐시 레이어)
RUN gradle dependencies --no-daemon || true

# 소스 코드 복사 및 빌드
COPY src ./src
RUN gradle build -x test --no-daemon

# ===== 실행 스테이지 =====
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 보안: 비루트 사용자로 실행
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# 업로드 디렉토리 생성
RUN mkdir -p /app/uploads/recipes && \
    chown -R appuser:appgroup /app

# JAR 파일 복사
COPY --from=builder /app/build/libs/*.jar app.jar
RUN chown appuser:appgroup app.jar

USER appuser

# 환경변수 (외부 DB 연결용)
ENV SPRING_PROFILES_ACTIVE=prod
ENV DB_HOST=host.docker.internal
ENV DB_PORT=3306
ENV DB_NAME=leftout
ENV DB_USERNAME=user_01
ENV DB_PASSWORD=1234
ENV RECIPE_API_KEY=""

# 포트 노출
EXPOSE 8080

# 헬스체크
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget -q --spider http://localhost:8080/actuator/health || exit 1

# 애플리케이션 실행
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
