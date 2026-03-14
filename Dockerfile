# Stage 1: Build with Maven + Java 21
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn clean package -DskipTests -q

# Stage 2: Run with slim Java 21 JRE
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8080} -jar app.jar"]
```

→ Click **Commit changes**

**Step 3 — Click on `src/main/resources/application.properties`** → click **pencil ✏️** → find the line:
```
server.port=${PORT:8080}
```
Add `server.address=0.0.0.0` directly below it:
```
server.port=${PORT:8080}
server.address=0.0.0.0
