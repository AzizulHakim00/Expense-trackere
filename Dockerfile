FROM eclipse-temurin:26-jdk AS build
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -ntp dependency:go-offline
COPY src/ src/
RUN ./mvnw -B -ntp verify

FROM eclipse-temurin:26-jre
WORKDIR /app
RUN groupadd --system app && useradd --system --gid app app
COPY --from=build --chown=app:app /app/target/expense-tracker-1.0.0.jar /app/app.jar
USER app
EXPOSE 10000
ENTRYPOINT ["java","-jar","/app/app.jar"]
