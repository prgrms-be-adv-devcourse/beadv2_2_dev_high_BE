FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY product/build/libs/*.jar /app/product.jar

EXPOSE 8085

ENTRYPOINT ["java","-jar","/app/product.jar"]
