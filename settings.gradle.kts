plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "bloodnight"
include("graph-model")
include("graph-compiler")
include("graph-std")
include("graph-editor-payload")
include("bukkit-nodes")
include("catalog")
include("core")
include("api")
include("web-api")
include("web-backend")
project(":web-api").projectDir = file("web/api")
project(":web-backend").projectDir = file("web/backend")
include("json-schema")
project(":json-schema").projectDir = file("tools/json-schema")
