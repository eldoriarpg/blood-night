import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import net.minecrell.pluginyml.paper.PaperPluginDescription

plugins {
    id("bloodnight.paper")
    alias(libs.plugins.shadow)
    alias(libs.plugins.plugin.yml.paper)
    alias(libs.plugins.run.paper)
}

dependencies {
    api(project(":api"))
    api(project(":web-api"))
    api(project(":bukkit-nodes"))
    api(project(":graph-editor-payload"))
    api(libs.bundles.eldoria.utilities)
    api(libs.bundles.jackson.full)
    api(libs.jackson.bukkit.paper)
    api(libs.ocular)
    compileOnly(project(":json-schema"))
    testImplementation(project(":json-schema"))
    compileOnly(libs.strokkcommands.annotations)
    compileOnly(libs.placeholderapi)
    compileOnly(libs.miniplaceholders)
    compileOnly(libs.worldguard) {
        exclude(group = "org.bukkit")
        exclude(group = "org.spigotmc")
        exclude(group = "io.papermc.paper")
    }
    testImplementation(libs.placeholderapi)
    testImplementation(libs.miniplaceholders)
    annotationProcessor(libs.strokkcommands.processor)
}

paper {
    name = "BloodNight"
    main = "de.eldoria.bloodnight.BloodNight"
    bootstrapper = "de.eldoria.bloodnight.BloodNightBootstrap"
    apiVersion = "26.2"
    foliaSupported = true
    description = "Special mobs during blood nights, their behaviour built from node graphs"
    authors = listOf("RainbowDashLabs")

    val subcommands = mapOf(
        "reload" to "reads the configuration again and recompiles every mob",
        "spawn" to "spawns a custom mob",
        "problems" to "lists compile problems and runtime failures of mobs",
        "source" to "writes the generated code of a mob to the debug folder",
        "debug" to "fires the debug trigger of mobs",
        "export" to "writes the editor payload and catalog to the debug folder",
        "night" to "shows the night selection and the next blood night of a world",
        "editor" to "opens, applies or closes a web editor session",
        "item" to "registers the held item or removes a registered item",
    )
    serverDependencies {
        listOf("PlaceholderAPI", "MiniPlaceholders", "WorldGuard").forEach { name ->
            register(name) {
                required = false
                joinClasspath = true
                load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            }
        }
    }
    permissions {
        register("bloodnight.admin") {
            description = "Every blood night command"
            default = BukkitPluginDescription.Permission.Default.OP
            children = subcommands.keys.map { "bloodnight.command.$it" } + "bloodnight.command.night.manage"
        }
        register("bloodnight.bypass.commands") {
            description = "Use commands blocked during blood nights"
            default = BukkitPluginDescription.Permission.Default.OP
        }
        register("bloodnight.updates") {
            description = "Hear about new BloodNight versions on join"
            default = BukkitPluginDescription.Permission.Default.OP
        }
        register("bloodnight.command.night.manage") {
            description = "/bloodnight night force and cancel, decides the coming night of a world"
            default = BukkitPluginDescription.Permission.Default.OP
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

    runServer {
        minecraftVersion("26.3")
        jvmArgs("-Dcom.mojang.eula.agree=true")
    }

    register<JavaExec>("generatePayloadSchema") {
        group = "build"
        description = "Writes web/frontend/src/api/generated/payload.schema.json from the editor payload's classes"
        dependsOn("compileTestJava")
        mainClass = "de.eldoria.bloodnight.editor.PayloadSchemaCli"
        classpath = sourceSets.test.get().runtimeClasspath
        workingDir = rootDir
    }
}

runPaper.folia.registerTask {
    minecraftVersion("26.2")
    runDirectory = layout.projectDirectory.dir("run-folia")
    jvmArgs("-Dcom.mojang.eula.agree=true")
}