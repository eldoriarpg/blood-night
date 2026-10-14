plugins {
    id("bloodnight.paper")
    `maven-publish`
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("api") {
            from(components["java"])
        }
    }
    repositories {
        maven {
            name = "EldoNexus"
            val kind = if (version.toString().endsWith("SNAPSHOT")) "snapshots" else "releases"
            url = uri("https://eldonexus.de/repository/maven-$kind/")
            credentials {
                username = System.getenv("NEXUS_USERNAME")
                password = System.getenv("NEXUS_PASSWORD")
            }
        }
    }
}

tasks.javadoc {
    (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:all,-missing", "-quiet")
}
