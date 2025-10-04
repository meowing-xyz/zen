plugins {
    java
    kotlin("jvm")
    id("dev.deftu.gradle.tools") version("2.57.0")
    id("dev.deftu.gradle.tools.resources") version("2.57.0")
    id("dev.deftu.gradle.tools.bloom") version("2.57.0")
    id("dev.deftu.gradle.tools.shadow") version("2.57.0")
    id("dev.deftu.gradle.tools.minecraft.loom") version("2.57.0")
    id("dev.deftu.gradle.tools.minecraft.releases") version("2.57.0")
}

repositories {
    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
}

toolkitLoomHelper {
    useMixinRefMap(modData.id)
    useTweaker("org.spongepowered.asm.launch.MixinTweaker")
    useForgeMixin(modData.id)
}

dependencies {
    implementation(shade(kotlin("stdlib-jdk8"))!!)
    implementation(shade("org.jetbrains.kotlin:kotlin-reflect:1.6.10")!!)
    implementation(shade("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.10.2")!!)

    modImplementation(shade("org.spongepowered:mixin:0.7.11-SNAPSHOT")!!)
    modImplementation(shade("gg.essential:elementa:710")!!)
    modImplementation(shade("gg.essential:universalcraft-${mcData}:430")!!)

    modImplementation(shade("xyz.meowing:vexel-${mcData}:1.0.6")!!)

    runtimeOnly("me.djtheredstoner:DevAuth-forge-legacy:1.2.1")
}

tasks.register("generateLists") {
    val srcDir = rootProject.file("src/main/kotlin/xyz/meowing/zen")
    val featureOutput = project.file("build/generated/resources/features.list")
    val commandOutput = project.file("build/generated/resources/commands.list")

    val moduleRegex = Regex("@Zen\\.Module\\s*(?:\\n|\\s)*(?:object|class)\\s+(\\w+)")
    val commandRegex = Regex("@Zen\\.Command\\s*(?:\\n|\\s)*(?:object|class)\\s+(\\w+)")
    val pkgRegex = Regex("package\\s+([\\w.]+)")

    inputs.dir(srcDir).optional(true)
    outputs.files(featureOutput, commandOutput)

    doLast {
        val featureClasses = mutableListOf<String>()
        val commandClasses = mutableListOf<String>()

        if (!srcDir.exists()) return@doLast

        srcDir.walkTopDown().forEach { file ->
            if (file.isFile && file.extension in listOf("kt", "java")) {
                val text = file.readText()
                val pkg = pkgRegex.find(text)?.groupValues?.get(1) ?: return@forEach

                moduleRegex.findAll(text).forEach { match ->
                    featureClasses += "${pkg}.${match.groupValues[1]}"
                }

                commandRegex.findAll(text).forEach { match ->
                    commandClasses += "${pkg}.${match.groupValues[1]}"
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
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    dependsOn("generateLists")
    from("build/generated/resources")
}

tasks.classes {
    dependsOn("generateLists")
}