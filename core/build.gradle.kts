plugins{
    `java-library`
}

dependencies{
    api(project(":nodes"))
    api(project(":api"))
    api(libs.bundles.eldoria.utilities)
}
