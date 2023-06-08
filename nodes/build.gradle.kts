dependencies {
    implementation(platform("com.fasterxml.jackson:jackson-bom:2.14.2"))
    implementation("com.fasterxml.jackson.core", "jackson-core")
    implementation("com.fasterxml.jackson.core","jackson-databind")
    implementation("com.fasterxml.jackson.core","jackson-annotations")

    testImplementation("com.fasterxml.jackson.core", "jackson-core")
    testImplementation("com.fasterxml.jackson.core","jackson-databind")
}
