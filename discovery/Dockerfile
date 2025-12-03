FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

# Copy Gradle wrapper and settings first for better layer caching
COPY gradlew gradlew.bat settings.gradle build.gradle ./
COPY gradle ./gradle

# Copy source
COPY . .

RUN chmod +x gradlew
RUN ./gradlew :discovery:bootJar --no-daemon

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=builder /app/discovery/build/libs/discovery-*.jar /app/discovery.jar

EXPOSE 8761

ENTRYPOINT ["java","-jar","/app/discovery.jar"]
