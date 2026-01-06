FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY notification/build/libs/*.jar /app/notification.jar

EXPOSE 8089

ENTRYPOINT ["java","-jar","/app/notification.jar"]
