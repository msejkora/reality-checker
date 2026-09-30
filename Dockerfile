FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace

COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B dependency:go-offline

COPY src src
RUN ./mvnw -B package -DskipTests

FROM eclipse-temurin:25-jre
WORKDIR /app

RUN groupadd --system --gid 10001 app && useradd --system --uid 10001 --gid 10001 app

COPY --from=build /workspace/target/*.jar /app/app.jar
COPY src/main/resources/filters /app/filters

ENV APP_FILTER_DIRECTORY=/app/filters
EXPOSE 8061

USER 10001:10001
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
