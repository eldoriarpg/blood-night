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
    val styling = layout.projectDirectory.dir("src/javadoc")
    inputs.dir(styling)
    with(options as StandardJavadocDocletOptions) {
        addStringOption("Xdoclint:all,-missing", "-quiet")
        windowTitle = "BloodNight API"
        docTitle = "BloodNight API"
        addFileOption("-add-stylesheet", styling.file("bloodnight.css").asFile)
        addFileOption("-add-script", styling.file("bloodnight.js").asFile)
        bottom = "<a class=\"bn-docs-link\" href=\"/\">BloodNight documentation</a>"
        links(
            "https://docs.oracle.com/en/java/javase/25/docs/api/",
            "https://jd.papermc.io/paper/${libs.versions.paper.get().substringBefore(".build")}/",
            "https://jspecify.dev/docs/api/",
            "https://javadoc.io/static/org.jetbrains/annotations/${libs.versions.jetbrains.annotations.get()}/",
        )
    }
}
