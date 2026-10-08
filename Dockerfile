FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

COPY app.jar /app/app.jar

EXPOSE 8001

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
