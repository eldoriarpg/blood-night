rootProject.name = "bloodnight"
include("nodes")
include("core")
include("api")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("jackson", "2.16.1")
            library("jackson-core", "com.fasterxml.jackson.core", "jackson-core").versionRef("jackson")
            library("jackson-databind", "com.fasterxml.jackson.core", "jackson-databind").versionRef("jackson")
            library("jackson-annotations", "com.fasterxml.jackson.core", "jackson-annotations").versionRef("jackson")
            library("jackson-yaml", "com.fasterxml.jackson.dataformat", "jackson-dataformat-yaml").versionRef("jackson")
            bundle("jackson-full", listOf("jackson-core", "jackson-databind", "jackson-annotations", "jackson-yaml"))
            bundle("jackson-lite", listOf("jackson-core", "jackson-databind", "jackson-annotations"))

            version("eldoutil", "2.0.1")
            library("eldoutil-items","de.eldoria.util", "items").versionRef("eldoutil")
            library("eldoutil-entities","de.eldoria.util", "entities").versionRef("eldoutil")
            library("eldoutil-jackson","de.eldoria.util", "jackson-configuration").versionRef("eldoutil")
            bundle("eldoria-utilities", listOf("eldoutil-items", "eldoutil-jackson","eldoutil-entities"))

            version("minecraft-latest", "1.19.4-R0.1-SNAPSHOT")
            library("paper-latest", "io.papermc.paper", "paper-api").versionRef("minecraft-latest")
            library("spigot-latest", "org.spigotmc", "spigot-api").versionRef("minecraft-latest")

            library("jetbrains-annotations", "org.jetbrains:annotations:24.1.0")
        }

        create("testlibs") {
            library("mockbuckit", "com.github.seeseemelk:MockBukkit-v1.19:2.29.0")
            version("junit", "5.10.1")
            library("junit-jupiter", "org.junit.jupiter","junit-jupiter").versionRef("junit")
            library("junit-params", "org.junit.jupiter","junit-jupiter-params").versionRef("junit")
            bundle("junit", listOf("junit-jupiter", "junit-params"))
        }
    }
}
