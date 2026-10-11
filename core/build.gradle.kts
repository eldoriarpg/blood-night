import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    id("bloodnight.paper")
    alias(libs.plugins.shadow)
    alias(libs.plugins.plugin.yml.paper)
}

dependencies {
    api(project(":api"))
    api(project(":bukkit-nodes"))
    api(project(":graph-editor-payload"))
    api(libs.bundles.eldoria.utilities)
    api(libs.bundles.jackson.full)
    api(libs.jackson.bukkit.paper)
    api(libs.ocular)
    compileOnly(libs.strokkcommands.annotations)
    annotationProcessor(libs.strokkcommands.processor)
}

paper {
    name = "BloodNight"
    main = "de.eldoria.bloodnight.BloodNight"
    apiVersion = "26.3"
    description = "Special mobs during blood nights, their behaviour built from node graphs"
    authors = listOf("RainbowDashLabs")

    val subcommands = mapOf(
        "reload" to "reads the configuration again and recompiles every mob",
        "spawn" to "spawns a custom mob",
        "problems" to "lists compile problems and runtime failures of mobs",
        "source" to "writes the generated code of a mob to the debug folder",
        "debug" to "fires the debug trigger of mobs",
        "export" to "writes the editor payload and catalog to the debug folder",
    )
    permissions {
        register("bloodnight.admin") {
            description = "Every blood night command"
            default = BukkitPluginDescription.Permission.Default.OP
            children = subcommands.keys.map { "bloodnight.command.$it" }
        }
        subcommands.forEach { (subcommand, does) ->
            register("bloodnight.command.$subcommand") {
                description = "/bloodnight $subcommand, $does"
                default = BukkitPluginDescription.Permission.Default.OP
            }
        }
    }
}

tasks {
    processResources {
        dependsOn(":catalog:generateCatalog")
        from(project(":catalog").layout.buildDirectory.file("catalog/catalog.json"))
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
