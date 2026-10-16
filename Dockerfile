# ============================================================================
# sync-service (Feature 4) - imagen Docker en DOS ETAPAS (multi-stage build)
#
# Etapa 1 "build": usa una imagen con Maven + JDK 17 para COMPILAR el proyecto
#                  (genera el codigo desde sync-api.yaml y crea el .jar).
# Etapa 2 "run"  : usa una imagen pequena con SOLO el JRE 17 y copia el .jar.
#                  Maven, el JDK y el codigo fuente NO llegan a la imagen final.
#
# Se puede usar multi-stage porque todas las dependencias estan en Maven Central
# (a diferencia del proyecto de la Clase 11, que dependia de un .jar local).
# ============================================================================

# ---------- Etapa 1: compilar ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

# Primero solo el pom: Docker guarda esta capa en cache y no vuelve a descargar
# las dependencias mientras el pom.xml no cambie.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# Luego el codigo y el contrato OpenAPI
COPY src ./src
RUN mvn -B -q package -DskipTests

# ---------- Etapa 2: ejecutar ----------
FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /build/target/sync-service-0.0.1-SNAPSHOT.jar app.jar

# Documenta el puerto; el mapeo real lo hace docker compose (o ECS en AWS)
EXPOSE 8084

# Sin credenciales aqui: DB_URL, DB_PASSWORD, DYNAMODB_ENDPOINT... llegan como
# variables de entorno al arrancar (application.yml ya las lee).
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
