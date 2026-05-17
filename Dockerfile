# Build stage
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY oa-common/pom.xml oa-common/
COPY oa-system/pom.xml oa-system/
COPY oa-approval/pom.xml oa-approval/
COPY oa-app/pom.xml oa-app/
RUN mvn dependency:go-offline -B
COPY . .
RUN mvn package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/oa-app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
