plugins {
    id("java")
    alias(libs.plugins.shadow) apply false
}

val frameworkProjects = setOf("graph-model", "graph-compiler", "graph-std")

allprojects {
    group = "de.eldoria.bloodnight"
    version = "1.0.0"

    apply {
        plugin<JavaPlugin>()
    }

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(25)
        }
    }

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://eldonexus.de/repository/maven-public/")
        maven("https://eldonexus.de/repository/maven-proxies/")
    }

    dependencies {
        val libs = rootProject.libs
        implementation(platform(libs.jackson.bom))
        compileOnly(libs.jetbrains.annotations)
        testImplementation(platform(libs.junit.bom))
        testImplementation(libs.bundles.junit)
        testRuntimeOnly(libs.junit.launcher)
        if (project.name !in frameworkProjects) {
            compileOnly(libs.paper.api)
            testImplementation(libs.paper.api)
            testImplementation(libs.mockbukkit)
        }
    }

    tasks {
        test {
            useJUnitPlatform()
        }
    }
}
