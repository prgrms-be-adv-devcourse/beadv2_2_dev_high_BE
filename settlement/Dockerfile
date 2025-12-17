FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

COPY gradlew gradlew.bat settings.gradle build.gradle ./
COPY gradle ./gradle
COPY . .

RUN chmod +x gradlew
RUN ./gradlew :settlement:bootJar --no-daemon

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=builder /app/settlement/build/libs/settlement-*.jar /app/settlement.jar

EXPOSE 8087

ENTRYPOINT ["java","-jar","/app/settlement.jar"]
