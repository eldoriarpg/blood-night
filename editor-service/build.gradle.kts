plugins {
    id("bloodnight.java")
    application
}

dependencies {
    implementation(project(":editor-api"))
    implementation(project(":graph-editor-payload"))
    implementation(libs.jspecify)
    implementation(libs.javalin)
    implementation(libs.bundles.sadu)
    implementation(libs.postgresql)
    implementation(libs.jackson.databind)
    runtimeOnly(libs.logback.classic)
    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.testcontainers.junit)
}

application {
    mainClass = "de.eldoria.bloodnight.editor.service.EditorService"
}
