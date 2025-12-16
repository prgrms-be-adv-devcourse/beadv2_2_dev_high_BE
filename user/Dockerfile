FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

COPY gradlew gradlew.bat settings.gradle build.gradle ./
COPY gradle ./gradle
COPY . .

RUN chmod +x gradlew
RUN ./gradlew :user:bootJar --no-daemon

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=builder /app/user/build/libs/user-*.jar /app/user.jar

EXPOSE 8088

ENTRYPOINT ["java","-jar","/app/user.jar"]
