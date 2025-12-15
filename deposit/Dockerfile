FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

COPY gradlew gradlew.bat settings.gradle build.gradle ./
COPY gradle ./gradle
COPY . .

RUN chmod +x gradlew
RUN ./gradlew :deposit:bootJar --no-daemon

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=builder /app/deposit/build/libs/deposit-*.jar /app/deposit.jar

EXPOSE 8083

ENTRYPOINT ["java","-jar","/app/deposit.jar"]
