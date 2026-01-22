FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY config/build/libs/*.jar /app/config.jar

EXPOSE 8888

ENTRYPOINT ["java","-jar","/app/config.jar"]
