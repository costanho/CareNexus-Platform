# ---------- Build stage ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Leverage Docker layer caching
COPY pom.xml .
RUN mvn -q -e -U -DskipTests dependency:go-offline

# Copy sources and build
COPY src ./src
RUN mvn -q -e -DskipTests clean package

# ---------- Run stage ----------
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy built jar from the build stage
COPY --from=build /app/target/*-SNAPSHOT.jar app.jar

# Set a profile for Docker
ENV SPRING_PROFILES_ACTIVE=docker
EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
