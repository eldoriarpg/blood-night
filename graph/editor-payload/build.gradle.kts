plugins {
    id("bloodnight.library")
}

dependencies {
    api(project(":graph-compiler"))
    testImplementation(project(":graph-std"))
}

tasks.test {
    systemProperty("snapshot.update", System.getProperty("snapshot.update", "false"))
    systemProperty("snapshot.dir", layout.projectDirectory.dir("src/test/resources").asFile.absolutePath)
}
