# syntax=docker/dockerfile:1

# ---- Etapa de build
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /build

# Maven wrapper + cache
COPY --chmod=0755 mvnw mvnw
COPY .mvn/ .mvn/
COPY pom.xml pom.xml

# Precalienta dependencias
RUN --mount=type=cache,target=/root/.m2 ./mvnw -DskipTests dependency:go-offline

# Código y empaquetado (con repackage para que traiga Start-Class)
COPY src src
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -DskipTests clean package spring-boot:repackage && \
    sh -lc 'ls -1 target/*.jar | grep -v "\.original$" | head -n1 | xargs -I{} cp {} target/app.jar'

# ---- Runtime mínimo
FROM eclipse-temurin:21-jre-jammy AS final
ARG UID=10001
RUN adduser --disabled-password --gecos "" --home "/nonexistent" --shell "/sbin/nologin" --no-create-home --uid "${UID}" appuser
USER appuser

WORKDIR /app
COPY --from=build /build/target/app.jar /app/app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
