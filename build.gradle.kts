import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.0.20"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0"
    id("com.gradleup.shadow") version "8.3.0"
    application
}

group = "eu.iamgio.quarkdown"
version = "1.0-SNAPSHOT"

allprojects {
    repositories {
        mavenCentral()
    }
}

dependencies {
    subprojects.forEach {
        implementation(it)
    }
}

application {
    mainClass.set("eu.iamgio.quarkdown.cli.QuarkdownCliKt")
}

tasks.build {
    dependsOn("shadowJar")
}

tasks.jar {
    enabled = false // The jar is generated by shadowJar
}

tasks.test {
    useJUnitPlatform()
}

tasks.distZip {
    archiveVersion.set("")

    // The module 'libs' contains .qmd library files that are saved in the lib/qmd directory of the distribution zip.
    val librariesModule = project(":libs")

    into("${archiveBaseName.get()}/lib/qmd") {
        from(librariesModule.file("src/main/resources")) {
            include("*.qmd")
        }
    }
}

tasks.wrapper {
    gradleVersion = "8.3"
    distributionType = Wrapper.DistributionType.ALL
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

tasks.withType<ShadowJar> {
    archiveVersion.set("")
    archiveClassifier.set("")
}
