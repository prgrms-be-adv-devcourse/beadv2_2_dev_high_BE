FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY user/build/libs/*.jar /app/user.jar

EXPOSE 8088

ENTRYPOINT ["java","-jar","/app/user.jar"]
