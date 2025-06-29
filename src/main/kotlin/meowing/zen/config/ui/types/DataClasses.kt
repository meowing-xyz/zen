package meowing.zen.config.ui.types

import meowing.zen.config.ui.ConfigData

data class ConfigElement(
    val configKey: String,
    val title: String?,
    val description: String?,
    val type: ElementType,
    val shouldShow: (ConfigData) -> Boolean = { true }
)

data class ConfigSection(
    val name: String,
    val elements: MutableList<ConfigElement> = mutableListOf()
)

data class ConfigCategory(
    val name: String,
    val sections: MutableList<ConfigSection> = mutableListOf()
)