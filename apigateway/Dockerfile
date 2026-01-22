FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY apigateway/build/libs/*.jar /app/apigateway.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/apigateway.jar"]
