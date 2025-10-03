package xyz.meowing.zen.config.ui.types

import xyz.meowing.zen.config.ui.ConfigData

data class ConfigElement(
    val configKey: String,
    val title: String?,
    val type: ElementType,
    val shouldShow: (ConfigData) -> Boolean = { true }
)

data class ConfigCategory(val name: String)
data class ConfigSection(val name: String)
data class ConfigSubcategory(val name: String, val elements: MutableList<ConfigElement> = mutableListOf())