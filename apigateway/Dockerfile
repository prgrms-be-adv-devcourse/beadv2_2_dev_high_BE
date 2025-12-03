FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

# Copy Gradle wrapper and settings first for caching
COPY gradlew gradlew.bat settings.gradle build.gradle ./
COPY gradle ./gradle

# Copy source
COPY . .

RUN chmod +x gradlew
RUN ./gradlew :apigateway:bootJar --no-daemon

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=builder /app/apigateway/build/libs/apigateway-*.jar /app/apigateway.jar

EXPOSE 8089

ENTRYPOINT ["java","-jar","/app/apigateway.jar"]
