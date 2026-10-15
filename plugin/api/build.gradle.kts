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
    }
}
