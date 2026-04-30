FROM node:22-alpine AS frontend-build

WORKDIR /app/recipe-client

COPY recipe-client/package*.json ./
RUN npm ci --ignore-scripts

COPY recipe-client/ ./
RUN npm run build

FROM maven:3.9.6-eclipse-temurin-17-alpine AS backend-build

WORKDIR /app

COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B

COPY src ./src

RUN mkdir -p src/main/resources/static

COPY --from=frontend-build /app/recipe-client/dist ./src/main/resources/static

RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:17-alpine

WORKDIR /app

RUN apk add --no-cache wget

RUN addgroup -S spring && adduser -S spring -G spring && \
    mkdir -p /app/logs && chown -R spring:spring /app

COPY --from=backend-build /app/target/springrecipe-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/ || exit 1

USER spring

ENTRYPOINT ["java", "-jar", "app.jar"]