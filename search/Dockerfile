FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY search/build/libs/*.jar /app/search.jar

EXPOSE 8086

ENTRYPOINT ["java","-jar","/app/search.jar"]
