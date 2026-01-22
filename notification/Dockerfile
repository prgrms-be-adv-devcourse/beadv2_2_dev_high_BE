FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

COPY gradlew gradlew.bat settings.gradle build.gradle ./
COPY gradle ./gradle
COPY . .

RUN chmod +x gradlew
RUN ./gradlew :notification:bootJar --no-daemon

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=builder /app/notification/build/libs/notification-*.jar /app/notification.jar

EXPOSE 8089

ENTRYPOINT ["java","-jar","/app/notification.jar"]
