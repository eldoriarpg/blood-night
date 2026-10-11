plugins {
    `java-library`
}

dependencies {
    api(project(":graph-model"))
    api(libs.javapoet)
    implementation(libs.ecj)
}
