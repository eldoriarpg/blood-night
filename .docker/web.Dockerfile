FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /build
COPY . .
RUN ./gradlew :web-backend:installDist --no-daemon -x test -x spotbugsMain

FROM eclipse-temurin:25-jre-alpine
COPY --from=build /build/web/backend/build/install/web-backend /app
EXPOSE 8080
ENTRYPOINT ["/app/bin/web-backend"]
