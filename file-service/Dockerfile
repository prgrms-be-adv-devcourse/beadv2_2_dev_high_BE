FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

COPY gradlew gradlew.bat settings.gradle build.gradle ./
COPY gradle ./gradle
COPY . .

RUN chmod +x gradlew
RUN ./gradlew :file-service:bootJar --no-daemon

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=builder /app/file-service/build/libs/file-service-*.jar /app/file-service.jar

EXPOSE 9027

ENTRYPOINT ["java","-jar","/app/file-service.jar"]
