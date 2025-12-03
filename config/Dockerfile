FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

COPY gradlew gradlew.bat settings.gradle build.gradle ./
COPY gradle ./gradle
COPY . .

RUN chmod +x gradlew
RUN ./gradlew :config:bootJar --no-daemon

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=builder /app/config/build/libs/config-*.jar /app/config.jar

EXPOSE 8888

ENTRYPOINT ["java","-jar","/app/config.jar"]
