plugins{
    `java-library`
}

dependencies {
    api(libs.bundles.jackson.lite)

    testImplementation(libs.bundles.jackson.lite)
    testImplementation(libs.bundles.eldoria.utilities)
}
