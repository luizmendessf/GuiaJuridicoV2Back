# Estágio 1: Build da aplicação com Maven
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml ./pom.xml
RUN mvn -q -f pom.xml dependency:go-offline
COPY src ./src
RUN mvn -q -f pom.xml clean package -DskipTests

# Estágio 2: Criação da imagem final de execução
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENV JAVA_OPTS="-XX:+UseStringDeduplication -XX:+AlwaysPreTouch -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-prod} -Dserver.port=${PORT:-8080}"
HEALTHCHECK --interval=30s --timeout=5s --start-period=20s --retries=5 CMD curl -f http://localhost:${PORT:-8080}/actuator/health || exit 1
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]