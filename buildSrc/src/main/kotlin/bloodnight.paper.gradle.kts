import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("bloodnight.library")
}

val libs = the<LibrariesForLibs>()

dependencies {
    compileOnly(libs.paper.api)
    testImplementation(libs.paper.api.test)
    testImplementation(libs.mockbukkit)
}

// TODO: Test against the shipped Paper version once MockBukkit releases it; MockBukkit 26.2 fails on paper-api 26.3.
listOf(configurations.testCompileClasspath, configurations.testRuntimeClasspath).forEach { classpath ->
    classpath { resolutionStrategy.force(libs.paper.api.test.get()) }
}
