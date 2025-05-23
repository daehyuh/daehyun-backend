# 🔨 빌드 단계
FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /app

COPY gradlew .
RUN chmod +x gradlew
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY src src

COPY application.properties application.properties
COPY application-jwt.properties application-jwt.properties
COPY application-db.properties application-db.properties
COPY application-security.properties application-security.properties
COPY application-file.properties application-file.properties

RUN ./gradlew bootJar --no-daemon

# 🚀 런타임 단계
FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar
COPY application.properties application.properties
COPY application-jwt.properties application-jwt.properties
COPY application-db.properties application-db.properties
COPY application-security.properties application-security.properties
COPY application-file.properties application-file.properties

EXPOSE 8080

CMD ["java", "-Dspring.config.location=classpath:/,file:/app/" ,"-jar", "app.jar"]