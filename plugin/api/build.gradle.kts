plugins {
    id("bloodnight.paper")
    `maven-publish`
}

dependencies {
    compileOnly(libs.jetbrains.annotations)
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishData {
    eldoNexus()
}

tasks.javadoc {
    (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:all,-missing", "-quiet")
}
