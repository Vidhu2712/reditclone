FROM maven:3-eclipse-temurin-23-noble AS build
COPY . .
RUN mvn clean package -DskipTests
FROM openjdk:23-jdk-slim
COPY --from=build /target/reddit-0.0.1-SNAPSHOT.jar reddit.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar","reddit.jar"]