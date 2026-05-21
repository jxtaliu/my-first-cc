FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN apk add --no-cache maven && \
    mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080 5005

ENTRYPOINT ["java", "-jar", "-Xms512m", "-Xmx1024m", \
    "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", \
    "app.jar"]
