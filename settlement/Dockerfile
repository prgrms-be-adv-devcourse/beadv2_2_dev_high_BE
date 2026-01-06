FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY settlement/build/libs/*.jar /app/settlement.jar

EXPOSE 8087

ENTRYPOINT ["java","-jar","/app/settlement.jar"]
