# syntax=docker/dockerfile:1.7

# Build stage
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY oa-common/pom.xml oa-common/
COPY oa-system/pom.xml oa-system/
COPY oa-approval/pom.xml oa-approval/
COPY oa-leave/pom.xml oa-leave/
COPY oa-app/pom.xml oa-app/
COPY oa-generator/pom.xml oa-generator/
COPY . .
RUN --mount=type=cache,target=/root/.m2 mvn -pl oa-app -am package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=builder /app/oa-app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
