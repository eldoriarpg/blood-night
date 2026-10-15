import com.github.spotbugs.snom.Confidence
import com.github.spotbugs.snom.Effort
import com.github.spotbugs.snom.SpotBugsTask
import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    java
    id("com.diffplug.spotless")
    id("com.github.spotbugs")
    id("de.chojo.publishdata")
}

val libs = the<LibrariesForLibs>()

group = "de.eldoria.bloodnight"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://eldonexus.de/repository/maven-public/")
    maven("https://eldonexus.de/repository/maven-proxies/")
    maven("https://repo.extendedclip.com/releases/")
}

dependencies {
    implementation(platform(libs.jackson.bom))
    compileOnly(libs.spotbugs.annotations)
    testCompileOnly(libs.spotbugs.annotations)
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.junit)
    testRuntimeOnly(libs.junit.launcher)
}

tasks.test {
    useJUnitPlatform()
    afterSuite(KotlinClosure2<TestDescriptor, TestResult, Unit>({ suite, result ->
        if (suite.parent == null && result.skippedTestCount > 0) {
            throw GradleException(
                "${result.skippedTestCount} test(s) skipped in ${project.path}; a skip hides a test that does not run, " +
                    "e.g. MockBukkit throwing UnimplementedOperationException",
            )
        }
    }))
}

spotless {
    java {
        target("src/**/*.java")
        licenseHeaderFile(rootProject.file("HEADER.txt"))
        trimTrailingWhitespace()
        endWithNewline()
        palantirJavaFormat(libs.versions.palantir.get()).formatJavadoc(false)
        removeUnusedImports()
        importOrder("", "java", "javax", "\\#")
        encoding("UTF-8")
    }
}

spotbugs {
    ignoreFailures = false
    effort = Effort.MAX
    reportLevel = Confidence.LOW
    includeFilter = rootProject.layout.projectDirectory.file("config/spotbugs/null.xml")
}

tasks.named<SpotBugsTask>("spotbugsMain") {
    reports.create("html") { required = true }
    reports.create("xml") { required = true }
}

tasks.named("spotbugsTest") { enabled = false }
