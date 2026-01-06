FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY discovery/build/libs/*.jar /app/discovery.jar

EXPOSE 8761

ENTRYPOINT ["java","-jar","/app/discovery.jar"]
