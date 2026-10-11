import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("bloodnight.java")
    `java-library`
}

val libs = the<LibrariesForLibs>()

dependencies {
    api(libs.jspecify)
}
