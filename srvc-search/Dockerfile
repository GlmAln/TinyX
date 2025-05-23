# build from project's root : sudo docker build -t srvc-search:1.0.0 -f ./srvc-search/Dockerfile .
# run from project's root : sudo docker run -it srvc-search:1.0.0

# Maven image pour la base
FROM maven:3.8.5-openjdk-17-slim AS build

WORKDIR /app

COPY . .

RUN mvn clean install -DskipTests && mvn package -Dquarkus.package.type=uber-jar -f srvc-search -DskipTests

# Images Openjdk plus déployées -> remplacement avec eclipse-temurin
FROM eclipse-temurin:17.0.14_7-jre-alpine

WORKDIR /app

COPY --from=build /app/srvc-search/target/srvc-search-1.0.0-SNAPSHOT-runner.jar .

EXPOSE 8083

CMD [ "java", "-jar", "srvc-search-1.0.0-SNAPSHOT-runner.jar"]
