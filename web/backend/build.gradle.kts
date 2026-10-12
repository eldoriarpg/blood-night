plugins {
    id("bloodnight.java")
    application
}

dependencies {
    implementation(project(":web-api"))
    implementation(project(":graph-editor-payload"))
    implementation(libs.jspecify)
    implementation(libs.javalin)
    implementation(libs.bundles.sadu)
    implementation(libs.postgresql)
    implementation(libs.jackson.databind)
    runtimeOnly(libs.logback.classic)
    testImplementation(project(":json-schema"))
    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.testcontainers.junit)
}

application {
    mainClass = "de.eldoria.bloodnight.web.WebService"
}

tasks.register<JavaExec>("generateApiSpec") {
    group = "build"
    description = "Writes web/backend/src/main/resources/api/openapi.json from the routes and the records they carry"
    dependsOn("compileTestJava")
    mainClass = "de.eldoria.bloodnight.web.ApiSpecCli"
    classpath = sourceSets.test.get().runtimeClasspath
    workingDir = rootDir
}
