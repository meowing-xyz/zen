package meowing.zen.config

import cc.polyfrost.oneconfig.config.Config
import cc.polyfrost.oneconfig.config.annotations.Color
import cc.polyfrost.oneconfig.config.annotations.Slider
import cc.polyfrost.oneconfig.config.annotations.Switch
import cc.polyfrost.oneconfig.config.core.OneColor
import cc.polyfrost.oneconfig.config.data.Mod
import cc.polyfrost.oneconfig.config.data.ModType
import cc.polyfrost.oneconfig.config.data.OptionSize

class zenconfig : Config(
    Mod("Zen", ModType.UTIL_QOL, "/assets/modicon.svg"),
    "ZenConfig.json",
) {
    @JvmField
    @Switch(
        name = "Clean guild messages",
        description = "Reformats the Guild messages that you receive.",
        size = OptionSize.SINGLE,
        category = "General",
        subcategory = "Clean chat"
    )
    var guildmsg = false

    @JvmField
    @Switch(
        name = "Clean party messages",
        description = "Reformats the Guild messages that you receive.",
        size = OptionSize.SINGLE,
        category = "General",
        subcategory = "Clean chat"
    )
    var partymsg = false

    @JvmField
    @Switch(
        name = "Clean guild join messages",
        description = "Reformats the Guild join/leave message that you receive.",
        size = OptionSize.SINGLE,
        category = "General",
        subcategory = "Clean chat"
    )
    var guildjoinleave = false

    @JvmField
    @Switch(
        name = "Clean friend join messages",
        description = "Reformats the Friend join/leave message that you receive.",
        size = OptionSize.SINGLE,
        category = "General",
        subcategory = "Clean chat"
    )
    var friendjoinleave = false

    @JvmField
    @Switch(
        name = "Slayer timer",
        description = "Slayer kill and spawn timer",
        size = OptionSize.SINGLE,
        category = "Slayers",
        subcategory = "General"
    )
    var slayertimer = false

    @JvmField
    @Switch(
        name = "Slayer highlight",
        description = "Highlights your slayer boss",
        size = OptionSize.SINGLE,
        category = "Slayers",
        subcategory = "General"
    )
    var slayerhighlight = false

    @JvmField
    @Color(
        name = "Slayer highlight color",
        description = "Slayer highlight color",
        size = OptionSize.DUAL,
        category = "Slayers",
        subcategory = "General"
    )
    var slayerhighlightcolor = OneColor(0, 255, 255, 127)

    @JvmField
    @Slider(
        name = "Slayer highlight width",
        min = 1f, max = 10f,
        step = 1,
        category = "Slayers",
        subcategory = "General"
    )
    var slayerhighlightwidth = 2f

    @JvmField
    @Switch(
        name = "Auto meow",
        description = "Automatically responds with a random meow message when someone says \"meow\".",
        size = OptionSize.SINGLE,
        category = "Meowing",
        subcategory = "Auto meow"
    )
    var automeow = false

    @JvmField
    @Switch(
        name = "Meow death sounds",
        description = "Plays cat sounds when a mob dies and spawns some particles.",
        size = OptionSize.SINGLE,
        category = "Meowing",
        subcategory = "Meow Sounds"
    )
    var meowdeathsounds = false

    @JvmField
    @Switch(
        name = "Meow sounds",
        description = "Plays cat sounds when someone's message includes \"meow\".",
        size = OptionSize.SINGLE,
        category = "Meowing",
        subcategory = "Meow Sounds"
    )
    var meowsounds = false
    init {
        initialize()
        addDependency("Slayer highlight color", "Slayer highlight")
    }

    fun registerListener(option: String, callback: Runnable) = addListener(option, callback)
}