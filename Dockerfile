FROM eclipse-temurin:17-jdk-jammy

ARG JAR_NAME=seasonthon-0.0.1-SNAPSHOT.jar

COPY ./build/libs/${JAR_NAME} app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]
