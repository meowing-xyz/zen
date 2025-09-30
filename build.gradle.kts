import org.apache.commons.lang3.SystemUtils

plugins {
    idea
    java
    kotlin("jvm") version "2.0.0"
    id("gg.essential.loom") version "0.10.0.+"
    id("dev.architectury.architectury-pack200") version "0.1.3"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

val baseGroup: String by project
val mcVersion: String by project
val modid: String by project
val transformerFile = file("src/main/resources/accesstransformer.cfg")
val elementaVersion = 710
val ucVersion = 415

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(8))
}

loom {
    launchConfigs {
        "client" {
            property("mixin.debug", "true")
            arg("--tweakClass", "org.spongepowered.asm.launch.MixinTweaker")
        }
    }
    runConfigs {
        "client" {
            // property("fml.coreMods.load", "meowing.zen.lwjgl.plugin.LWJGLLoadingPlugin")
            if (SystemUtils.IS_OS_MAC_OSX) vmArgs.remove("-XstartOnFirstThread")
        }
        remove(getByName("server"))
    }
    forge {
        pack200Provider.set(dev.architectury.pack200.java.Pack200Adapter())
        mixinConfig("mixins.$modid.json")
        if (transformerFile.exists()) {
            println("Installing access transformer")
            accessTransformer(transformerFile)
        }
    }
    mixin {
        defaultRefmapName.set("mixins.$modid.refmap.json")
    }
}

tasks.compileJava {
    dependsOn(tasks.processResources)
}

sourceSets.main {
    output.setResourcesDir(sourceSets.main.flatMap { it.java.classesDirectory })
    java.srcDir(layout.projectDirectory.dir("src/main/kotlin"))
    kotlin.destinationDirectory.set(java.destinationDirectory)
}

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://repo.spongepowered.org/maven/")
    maven("https://repo.essential.gg/repository/maven-public")
    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
    maven("https://repo.polyfrost.org/releases")
}

val shadowImpl: Configuration by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

dependencies {
    minecraft("com.mojang:minecraft:1.8.9")
    mappings("de.oceanlabs.mcp:mcp_stable:22-1.8.9")
    forge("net.minecraftforge:forge:1.8.9-11.15.1.2318-1.8.9")

    shadowImpl("org.spongepowered:mixin:0.7.11-SNAPSHOT") {
        isTransitive = false
    }
    annotationProcessor("org.spongepowered:mixin:0.8.5-SNAPSHOT")

    shadowImpl("org.reflections:reflections:0.10.2")
    shadowImpl("gg.essential:elementa:$elementaVersion")
    shadowImpl("gg.essential:universalcraft-1.8.9-forge:$ucVersion")
    shadowImpl("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.10.2")

    // shadowImpl("com.github.odtheking:odin-lwjgl:68de0d3e0b")
    shadowImpl("xyz.meowing:vexel-1.8.9-forge:1.0.2")

    runtimeOnly("me.djtheredstoner:DevAuth-forge-legacy:1.2.1")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Jar> {
    archiveBaseName.set("zen-1.8.9-forge")
    manifest.attributes.run {
        // this["FMLCorePlugin"] = "meowing.zen.lwjgl.plugin.LWJGLLoadingPlugin"
        this["Main-Class"] = "meowing.zen.Installer"
        this["FMLCorePluginContainsFMLMod"] = "true"
        this["ForceLoadAsMod"] = "true"
        this["TweakClass"] = "org.spongepowered.asm.launch.MixinTweaker"
        this["MixinConfigs"] = "mixins.$modid.json"
        if (transformerFile.exists())
            this["FMLAT"] = "${modid}_at.cfg"
    }
}

tasks.processResources {
    inputs.property("version", project.version)
    inputs.property("mcversion", mcVersion)
    inputs.property("modid", modid)
    inputs.property("basePackage", baseGroup)

    filesMatching(listOf("mcmod.info", "mixins.$modid.json")) {
        expand(inputs.properties)
    }

    rename("accesstransformer.cfg", "META-INF/${modid}_at.cfg")
}

tasks.named<net.fabricmc.loom.task.RemapJarTask>("remapJar") {
    archiveClassifier.set("")
    archiveBaseName.set("zen-1.8.9-forge")
    from(tasks.shadowJar)
    input.set(tasks.shadowJar.get().archiveFile)
}

tasks.jar {
    archiveClassifier.set("without-deps")
    destinationDirectory.set(layout.buildDirectory.dir("intermediates"))
}

val moduleRegex = Regex("@Zen\\.Module\\s*(?:\\n|\\s)*(?:object|class)\\s+(\\w+)")
val commandRegex = Regex("@Zen\\.Command\\s*(?:\\n|\\s)*(?:object|class)\\s+(\\w+)")
val pkgRegex = Regex("package\\s+([\\w.]+)")

tasks.register("generateLists") {
    doLast {
        val srcDir = file("src/main/kotlin/meowing/zen")
        val featureOutput = file("src/main/resources/features.list")
        val commandOutput = file("src/main/resources/commands.list")

        val featureClasses = mutableListOf<String>()
        val commandClasses = mutableListOf<String>()

        if (!srcDir.exists()) return@doLast

        srcDir.walkTopDown().forEach { file ->
            if (file.isFile && file.extension in listOf("kt", "java")) {
                val text = file.readText()
                val pkg = pkgRegex.find(text)?.groupValues?.get(1) ?: return@forEach

                moduleRegex.findAll(text).forEach { match ->
                    val clsName = match.groupValues[1] // get object/class name
                    featureClasses += "$pkg.$clsName"
                }

                commandRegex.findAll(text).forEach { match ->
                    val clsName = match.groupValues[1]
                    commandClasses += "$pkg.$clsName"
                }
            }
        }

        featureOutput.parentFile.mkdirs()
        commandOutput.parentFile.mkdirs()
        featureOutput.writeText(featureClasses.joinToString("\n"))
        commandOutput.writeText(commandClasses.joinToString("\n"))
    }
}

tasks.processResources {
    dependsOn("generateLists")
}

tasks.shadowJar {
    destinationDirectory.set(layout.buildDirectory.dir("archiveJars"))
    archiveClassifier.set("deps")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    configurations = listOf(shadowImpl)
    exclude("META-INF/versions/**")
    fun relocate(name: String) = relocate(name, "$baseGroup.deps.$name")
    relocate("gg.essential.elementa")
    relocate("gg.essential.universal")
    mergeServiceFiles()
}

tasks.assemble.get().dependsOn(tasks.remapJar)