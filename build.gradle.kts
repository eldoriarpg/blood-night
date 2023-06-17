plugins {
    id("java")
}

group = "de.eldoria.bloodnight"
version = "1.0.0"

allprojects {
    apply {
        plugin<JavaPlugin>()
    }

    repositories {
        mavenCentral()
        maven("https://eldonexus.de/repository/maven-public/")
        maven("https://eldonexus.de/repository/maven-proxies/")
    }

    dependencies {
        val libs = rootProject.libs
        val testlibs = rootProject.testlibs
        compileOnly(libs.spigot.latest)
        compileOnly(libs.jetbrains.annotations)
        testImplementation(libs.spigot.latest)
        testImplementation(testlibs.mockbuckit)
        testImplementation(testlibs.bundles.junit)
    }

    tasks {
        test {
            useJUnitPlatform()
        }
    }
}
