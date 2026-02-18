# Build stage
FROM gradle:8.12-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle :server:buildFatJar --no-daemon

# Run stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/server/build/libs/server-all.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
