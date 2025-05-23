# build from project's root : sudo docker build -t repo-social:1.0.0 -f ./repo-social/Dockerfile .
# run from project's root : sudo docker run -it repo-social:1.0.0

# Maven image pour la base
FROM maven:3.8.5-openjdk-17-slim AS build

WORKDIR /app

COPY . .
# COPY target/repo-social-1.0.0-SNAPSHOT.jar .

RUN mvn clean package -Dquarkus.package.type=uber-jar -f repo-social -DskipTests

# Images Openjdk plus déployées -> remplacement avec eclipse-temurin
FROM eclipse-temurin:17.0.14_7-jre-alpine

WORKDIR /app

COPY --from=build /app/repo-social/target/repo-social-1.0.0-SNAPSHOT-runner.jar .

EXPOSE 8082

CMD [ "java", "-jar", "repo-social-1.0.0-SNAPSHOT-runner.jar"]
