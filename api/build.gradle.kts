plugins {
    id("bloodnight.paper")
    `maven-publish`
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
