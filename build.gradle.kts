plugins {
    id("java")
}

group = "de.eldoria.bloodnight"
version = "1.0.0"

allprojects {
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
        compileOnly(libs.paper.api)
        compileOnly(libs.jetbrains.annotations)
        testImplementation(libs.paper.api)
        testImplementation(libs.mockbukkit)
        testImplementation(platform(libs.junit.bom))
        testImplementation(libs.bundles.junit)
        testRuntimeOnly(libs.junit.launcher)
    }

    tasks {
        test {
            useJUnitPlatform()
        }
    }
}
