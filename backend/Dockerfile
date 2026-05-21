FROM eclipse-temurin:21-jdk

RUN apt-get update && apt-get install -y maven

WORKDIR /app

CMD ["mvn", "spring-boot:run", "-DskipTests"]
