plugins {
    id("bloodnight.paper")
}

val apiSources = configurations.create("apiSources") {
    isTransitive = false
}

dependencies {
    implementation(project(":bukkit-nodes"))
    implementation(project(":graph-editor-payload"))
    implementation(libs.javaparser)
    implementation(libs.paper.api)
    apiSources(variantOf(libs.paper.api) { classifier("sources") })
}

val catalogFile = layout.buildDirectory.file("catalog/catalog.json")

tasks.register<JavaExec>("generateCatalog") {
    description = "Writes the editor catalog of the blood-night node platform, with javadoc from the Paper API sources."
    classpath = sourceSets["main"].runtimeClasspath
    mainClass = "de.eldoria.bloodnight.catalog.CatalogMain"
    inputs.files(apiSources)
    inputs.files(sourceSets["main"].runtimeClasspath)
    outputs.file(catalogFile)
    argumentProviders.add(CommandLineArgumentProvider {
        listOf(catalogFile.get().asFile.absolutePath) + apiSources.files.map { it.absolutePath }
    })
}

tasks.test {
    systemProperty("catalog.sources", apiSources.asPath)
}
