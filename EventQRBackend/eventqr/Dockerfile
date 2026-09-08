FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY pom.xml ./
COPY mvnw ./
COPY .mvn .mvn
RUN chmod +x mvnw

COPY src ./src
# Tests are NOT run here: the image build skips them to keep the package layer fast and
# hermetic. CI is responsible for `mvn test` (and dependency-check) before this build is
# invoked — see README "Development Guidelines". Never gate a deploy on the image build
# alone without a green test run upstream.
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy AS runtime

WORKDIR /app

RUN useradd --create-home --shell /bin/bash appuser

COPY --from=build /workspace/target/eventqr-0.0.1-SNAPSHOT.jar /app/app.jar

EXPOSE 10000

USER appuser

CMD ["sh", "-c", "java -Dserver.port=${PORT:-10000} -Dserver.address=0.0.0.0 -jar /app/app.jar"]