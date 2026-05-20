FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY minesweeper/pom.xml .
COPY minesweeper/src ./src
RUN apk add --no-cache maven
RUN mvn clean package -DskipTests
CMD ["java", "-jar", "target/*.jar"]
