FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY deposit/build/libs/*.jar /app/deposit.jar

EXPOSE 8083

ENTRYPOINT ["java","-jar","/app/deposit.jar"]
