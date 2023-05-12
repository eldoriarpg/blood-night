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
        compileOnly("org.spigotmc", "spigot-api", "1.19.2-R0.1-SNAPSHOT")
        compileOnly("org.jetbrains", "annotations", "24.0.1")
        testImplementation(platform("org.junit:junit-bom:5.9.1"))
        testImplementation("org.junit.jupiter:junit-jupiter")
    }

    tasks {
        test {
            useJUnitPlatform()
        }
    }
}
