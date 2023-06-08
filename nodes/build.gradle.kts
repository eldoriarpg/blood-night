plugins{
    `java-library`
}

dependencies {
    api(platform("com.fasterxml.jackson:jackson-bom:2.14.2"))
    api("com.fasterxml.jackson.core", "jackson-core")
    api("com.fasterxml.jackson.core","jackson-databind")
    api("com.fasterxml.jackson.core","jackson-annotations")

    testImplementation("com.fasterxml.jackson.core", "jackson-core")
    testImplementation("com.fasterxml.jackson.core","jackson-databind")
}
