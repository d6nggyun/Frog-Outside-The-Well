FROM openjdk:17-jdk-slim

ARG JAR_NAME=ingle-0.0.1-SNAPSHOT.jar

COPY ./build/libs/${JAR_NAME} app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]