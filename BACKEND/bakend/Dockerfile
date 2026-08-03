# ============================================================
#  Dockerfile — bakend (Auditorías Industriales SAS)
#  Conteneriza únicamente el servicio Spring Boot. La base de
#  datos vive en Supabase (Postgres gestionado externo) y nunca
#  se empaqueta aquí — ver docker-compose.yml.
# ============================================================

# ---- Etapa de build ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -q dependency:go-offline

COPY src ./src
RUN ./mvnw -q package -DskipTests && \
    cp target/bakend-*.jar app.jar

# ---- Etapa de runtime ----
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

RUN groupadd --system bakend && useradd --system --gid bakend bakend
COPY --from=build /app/app.jar app.jar
USER bakend

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
