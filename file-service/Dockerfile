FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY file-service/build/libs/*.jar /app/file-service.jar

EXPOSE 9027

ENTRYPOINT ["java","-jar","/app/file-service.jar"]
