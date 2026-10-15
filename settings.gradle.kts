plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "bloodnight"
include("graph-model")
include("graph-compiler")
include("graph-std")
include("graph-editor-payload")
project(":graph-model").projectDir = file("graph/model")
project(":graph-compiler").projectDir = file("graph/compiler")
project(":graph-std").projectDir = file("graph/std")
project(":graph-editor-payload").projectDir = file("graph/editor-payload")
include("bukkit-nodes")
include("catalog")
include("core")
include("api")
project(":bukkit-nodes").projectDir = file("plugin/bukkit-nodes")
project(":catalog").projectDir = file("plugin/catalog")
project(":core").projectDir = file("plugin/core")
project(":api").projectDir = file("plugin/api")
include("web-api")
include("web-backend")
project(":web-api").projectDir = file("web/api")
project(":web-backend").projectDir = file("web/backend")
include("json-schema")
project(":json-schema").projectDir = file("tools/json-schema")
