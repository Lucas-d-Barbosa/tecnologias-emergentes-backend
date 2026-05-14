FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# copy only what is needed for dependency download
COPY pom.xml mvnw .
COPY .mvn .mvn
RUN mvn -B -DskipTests dependency:go-offline

# copy sources and build
COPY src src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]