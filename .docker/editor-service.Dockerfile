FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /build
COPY . .
RUN ./gradlew :editor-service:installDist --no-daemon -x test -x spotbugsMain

FROM eclipse-temurin:25-jre-alpine
COPY --from=build /build/editor-service/build/install/editor-service /app
EXPOSE 8080
ENTRYPOINT ["/app/bin/editor-service"]
