FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY auction/build/libs/*.jar /app/auction.jar

EXPOSE 8081

ENTRYPOINT ["java","-jar","/app/auction.jar"]
