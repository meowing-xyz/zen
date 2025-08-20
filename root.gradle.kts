plugins {
    id("dev.deftu.gradle.multiversion-root")
}

preprocess {
    createNode("1.8.9-forge", 1_08_09, "srg")
//    val forge_1_16_05 = createNode("1.16.5-forge", 1_16_05, "srg")
//    val fabric_1_16_05 = createNode("1.16.5-fabric", 1_16_05, "yarn")
//    val fabric_1_21_5 = createNode("1.21.5-fabric", 1_21_05, "yarn")
//    val fabric_1_21_7 = createNode("1.21.7-fabric", 1_21_07, "yarn")
}