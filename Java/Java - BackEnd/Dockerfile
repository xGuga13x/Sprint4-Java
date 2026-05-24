FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn package -DskipTests -Dquarkus.package.type=uber-jar -q

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*-runner.jar app.jar
EXPOSE 8080
CMD ["java", "-Dquarkus.http.port=${PORT:-8080}", "-jar", "app.jar"]
