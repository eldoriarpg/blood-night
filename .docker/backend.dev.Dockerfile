FROM gradle:jdk25-alpine

WORKDIR /home/gradle/project

ENV GRADLE_USER_HOME=/home/gradle/.gradle

EXPOSE 8080

CMD ["./gradlew", ":web-backend:run", "--no-daemon", "-x", "test"]
