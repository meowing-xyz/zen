pluginManagement {
    repositories {
        maven("https://maven.deftu.dev/releases")
        maven("https://maven.fabricmc.net")
        maven("https://maven.architectury.dev/")
        maven("https://maven.minecraftforge.net")
        maven("https://repo.essential.gg/repository/maven-public")
        maven("https://server.bbkr.space/artifactory/libs-release/")
        maven("https://jitpack.io/")
        maven("https://maven.terraformersmc.com/")
        maven("https://maven.deftu.dev/snapshots")

        mavenLocal()
        mavenCentral()

        gradlePluginPortal()
    }

    plugins {
        kotlin("jvm") version("2.0.0")
        id("dev.deftu.gradle.gradle-toolkit") version("2.57.0")
    }
}

val projectName: String = extra["mod.name"].toString()

rootProject.name = projectName