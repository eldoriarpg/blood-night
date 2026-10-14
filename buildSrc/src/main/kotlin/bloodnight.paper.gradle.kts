import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("bloodnight.library")
}

val libs = the<LibrariesForLibs>()

dependencies {
    compileOnly(libs.paper.api)
    testImplementation(libs.paper.api)
    testImplementation(libs.mockbukkit)
}
