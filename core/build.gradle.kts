import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import net.minecrell.pluginyml.paper.PaperPluginDescription

plugins {
    id("bloodnight.paper")
    alias(libs.plugins.shadow)
    alias(libs.plugins.plugin.yml.paper)
    alias(libs.plugins.run.paper)
    `maven-publish`
}

publishData {
    eldoNexus(internal = true)
}

publishing {
    publications {
        create<MavenPublication>("plugin") {
            artifactId = "bloodnight"
            artifact(tasks.shadowJar)
        }
    }
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
    implementation(libs.bstats)
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
    annotationProcessor(libs.ocular)
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
        "trigger" to "fires the debug trigger of mobs",
        "debug" to "uploads a debug report to Lyna",
        "export" to "writes the editor payload and catalog to the debug folder",
        "night" to "shows the night selection and the next blood night of a world",
        "editor" to "opens, applies or closes a web editor session",
        "item" to "registers the held item or removes a registered item",
        "settings" to "shows or changes one setting without the web editor",
        "store" to "lists, installs and updates mobs from the store",
        "pack" to "writes the resource pack out to merge it into another pack",
        "convert" to "takes over the settings of the old BloodNight",
        "setup" to "walks through the first settings and mobs in the web editor",
    )
    serverDependencies {
        listOf("PlaceholderAPI", "MiniPlaceholders", "WorldGuard").forEach { name ->
            register(name) {
                required = false
                joinClasspath = true
                load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            }
        }
        listOf("Nexo", "Oraxen", "ItemsAdder").forEach { name ->
            register(name) {
                required = false
                joinClasspath = false
                load = PaperPluginDescription.RelativeLoadOrder.AFTER
            }
        }
    }
    permissions {
        register("bloodnight.admin") {
            description = "Every blood night command"
            default = BukkitPluginDescription.Permission.Default.OP
            children = subcommands.keys.map { "bloodnight.command.$it" } + "bloodnight.command.night.manage"
        }
        val areas = mapOf(
            "mobs" to "the mobs",
            "macros" to "the macros",
            "items" to "the registered items",
            "worlds" to "the settings of the worlds",
            "server" to "the server-wide settings",
            "drops" to "the global drops",
        )
        areas.forEach { (area, what) ->
            register("bloodnight.editor.$area") {
                description = "Change $what in the web editor; without it they are read only"
                default = BukkitPluginDescription.Permission.Default.OP
            }
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
                if (subcommand == "editor") children = areas.keys.map { "bloodnight.editor.$it" }
            }
        }
    }
}

val devEditor = providers.environmentVariable("BLOODNIGHT_EDITOR_URL").orElse("http://localhost:8080").get()
val devPack = providers.environmentVariable("BLOODNIGHT_RESOURCEPACK_URL")
    .orElse("http://localhost:8765/bloodnight.zip")
    .get()

tasks {
    processResources {
        dependsOn(":catalog:generateCatalog")
        from(project(":catalog").layout.buildDirectory.file("catalog/catalog.json"))
        from(rootProject.layout.projectDirectory.dir("resourcepack")) {
            into("resourcepack")
        }
    }

    register<JavaExec>("generateStore") {
        group = "build"
        description = "Writes src/main/resources/store/bloodnight from the default mobs and src/main/store descriptions"
        dependsOn("compileTestJava")
        mainClass = "de.eldoria.bloodnight.store.StoreGeneratorCli"
        classpath = sourceSets.test.get().runtimeClasspath
        workingDir = rootDir
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
        relocate("de.eldoria.EldoUtilities", "$shade.EldoUtilities")
        relocate("com.palantir.javapoet", "$shade.javapoet")
        relocate("org.bstats", "$shade.bstats")
    }

    build {
        dependsOn(shadowJar)
    }

    runServer {
        minecraftVersion("26.3")
        jvmArgs(
            "-Dcom.mojang.eula.agree=true",
            "-Dbloodnight.editor.url=$devEditor",
            "-Dbloodnight.resourcepack.url=$devPack")
        downloadPlugins {
            modrinth("essentialsx", "2.22.0")
            modrinth("fastasyncworldedit", "2.16.1")
            modrinth("worldguard", "7.0.19")
        }
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
    jvmArgs(
        "-Dcom.mojang.eula.agree=true",
        "-Dbloodnight.editor.url=$devEditor",
        "-Dbloodnight.resourcepack.url=$devPack")
}