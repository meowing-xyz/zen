import dev.deftu.gradle.utils.version.MinecraftVersions
import dev.deftu.gradle.utils.includeOrShade

plugins {
    java
    kotlin("jvm")
    id("dev.deftu.gradle.multiversion")
    id("dev.deftu.gradle.tools")
    id("dev.deftu.gradle.tools.resources")
    id("dev.deftu.gradle.tools.bloom")
    id("dev.deftu.gradle.tools.shadow")
    id("dev.deftu.gradle.tools.minecraft.loom")
    id("dev.deftu.gradle.tools.minecraft.releases")
}

toolkitMultiversion {
    moveBuildsToRootProject.set(true)
}

toolkitLoomHelper {
    if (!mcData.isNeoForge) {
        useMixinRefMap(modData.id)
    }

    if (mcData.isForge) {
        useTweaker("org.spongepowered.asm.launch.MixinTweaker")
        useForgeMixin(modData.id)
    }

    if (mcData.isForgeLike && mcData.version >= MinecraftVersions.VERSION_1_16_5) {
        useKotlinForForge()
    }
}

repositories {
    maven("https://maven.deftu.dev/releases")
    maven("https://maven.fabricmc.net")
    maven("https://maven.architectury.dev/")
    maven("https://maven.minecraftforge.net")
    maven("https://repo.essential.gg/repository/maven-public")
    maven("https://server.bbkr.space/artifactory/libs-release/")
    maven("https://jitpack.io/")
    maven("https://maven.terraformersmc.com/")
    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
    maven("https://maven.deftu.dev/snapshots")
    mavenLocal()
    mavenCentral()
}

dependencies {
    if (mcData.isFabric) {
        modImplementation("net.fabricmc.fabric-api:fabric-api:${mcData.dependencies.fabric.fabricApiVersion}")
        modImplementation("net.fabricmc:fabric-language-kotlin:${mcData.dependencies.fabric.fabricLanguageKotlinVersion}")
        modImplementation(includeOrShade("gg.essential:elementa:710")!!)

        if (mcData.version != MinecraftVersions.VERSION_1_16_5) {
            modImplementation(includeOrShade("gg.essential:universalcraft-${mcData}:427")!!)
        }

        modImplementation(includeOrShade("org.reflections:reflections:0.10.2")!!)
        modImplementation(includeOrShade("org.javassist:javassist:3.30.2-GA")!!)


        if (mcData.version == MinecraftVersions.VERSION_1_21_7) {
            modImplementation("com.terraformersmc:modmenu:15.0.0-beta.3")
        } else if (mcData.version == MinecraftVersions.VERSION_1_21_5) {
            modImplementation("com.terraformersmc:modmenu:14.0.0-rc.2")
        }

        runtimeOnly("me.djtheredstoner:DevAuth-fabric:1.2.1")
    } else if (mcData.version <= MinecraftVersions.VERSION_1_12_2) {
        implementation(includeOrShade(kotlin("stdlib-jdk8"))!!)
        implementation(includeOrShade("org.jetbrains.kotlin:kotlin-reflect:1.6.10")!!)
        implementation(includeOrShade("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.6.4")!!)
        implementation(includeOrShade("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")!!)
        modImplementation(includeOrShade("org.spongepowered:mixin:0.7.11-SNAPSHOT")!!)
        modImplementation(includeOrShade("gg.essential:elementa:710")!!)
        modImplementation(includeOrShade("gg.essential:universalcraft-${mcData}:427")!!)
        modImplementation(includeOrShade("org.reflections:reflections:0.10.2")!!)
        runtimeOnly("me.djtheredstoner:DevAuth-forge-legacy:1.2.1")
    }
}