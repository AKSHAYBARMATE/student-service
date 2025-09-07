# Build stage
FROM maven:3.9.4-openjdk-17 AS builder

WORKDIR /app
COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# Runtime stage
FROM openjdk:17-jdk-slim

RUN addgroup --system spring && adduser --system spring --ingroup spring

USER spring:spring

WORKDIR /app

COPY --from=builder /app/target/student-service-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]