# Multi-stage Docker build for Spring Boot application on Render
# Stage 1: Build the JAR package using Maven
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom.xml and pre-fetch dependencies for Docker cache optimization
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy project source code and build the production JAR
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Lightweight runtime container
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copy compiled JAR artifact
COPY --from=build /app/target/nutrigen-backend-1.0.0.jar app.jar

# Dynamic port assigned by Render ($PORT)
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT} -jar app.jar"]
