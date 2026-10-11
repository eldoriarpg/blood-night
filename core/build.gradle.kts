plugins {
    `java-library`
    alias(libs.plugins.shadow)
}

dependencies {
    api(project(":nodes"))
    api(project(":api"))
    api(project(":bukkit-nodes"))
    api(project(":graph-editor-payload"))
    api(libs.bundles.eldoria.utilities)
    api(libs.bundles.jackson.full)
    api(libs.jackson.bukkit.paper)
    api(libs.ocular)
}

tasks {
    processResources {
        dependsOn(":catalog:generateCatalog")
        from(project(":catalog").layout.buildDirectory.file("catalog/catalog.json"))
        val props = mapOf("version" to project.version)
        inputs.properties(props)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }

    shadowJar {
        archiveBaseName = "BloodNight"
        archiveClassifier = ""
        exclude("module-info.class", "META-INF/versions/*/module-info.class")
        val shade = "de.eldoria.bloodnight.libs"
        relocate("tools.jackson", "$shade.jackson")
        relocate("com.fasterxml.jackson", "$shade.jackson.annotation")
        relocate("org.snakeyaml", "$shade.snakeyaml")
        relocate("dev.chojo.ocular", "$shade.ocular")
        relocate("de.eldoria.jacksonbukkit", "$shade.jacksonbukkit")
        relocate("de.eldoria.eldoutilities", "$shade.eldoutilities")
        relocate("com.palantir.javapoet", "$shade.javapoet")
    }

    build {
        dependsOn(shadowJar)
    }
}
