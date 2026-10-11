plugins {
    `java-library`
}

dependencies {
    api(project(":nodes"))
    api(project(":api"))
    api(libs.bundles.eldoria.utilities)
    api(libs.bundles.jackson.full)
    api(libs.jackson.bukkit.paper)
    api(libs.ocular)
}
