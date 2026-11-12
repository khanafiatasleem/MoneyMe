# Stage 1: Build application with Eclipse Temurin JDK 17
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /workspace/app

# Copy build descriptor and wrapper
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Normalize potential Windows line endings and ensure execution permission
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw

# Copy application source code
COPY src src

# Package production executable JAR without running tests
RUN ./mvnw clean package -DskipTests -B

# Stage 2: Minimal, secure JRE 17 runtime
FROM eclipse-temurin:17-jre-alpine
VOLUME /tmp
WORKDIR /app

# Copy binary from build stage
COPY --from=build /workspace/app/target/moneyme-backend-1.0.0.jar app.jar

# Render dynamic port assignment (defaults to 8080)
EXPOSE 8080

# Production memory tuning for cloud free tiers (e.g. Render 512MB RAM)
ENV JAVA_OPTS="-Xms256m -Xmx384m -XX:+UseG1GC"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT:-8080} -jar app.jar"]
