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
        //compileOnly("io.papermc.paper:paper-api:1.19.4-R0.1-SNAPSHOT")
        compileOnly("org.spigotmc", "spigot-api", "1.19.4-R0.1-SNAPSHOT")
        compileOnly("org.jetbrains", "annotations", "24.0.1")
        testImplementation("org.spigotmc", "spigot-api", "1.19.4-R0.1-SNAPSHOT")
        testImplementation("com.github.seeseemelk", "MockBukkit-v1.19", "2.29.0")
        testImplementation(platform("org.junit:junit-bom:5.9.2"))
        testImplementation("org.junit.jupiter:junit-jupiter")
        testImplementation("org.junit.jupiter:junit-jupiter-params")
    }

    tasks {
        test {
            useJUnitPlatform()
        }
    }
}
