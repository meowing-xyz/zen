package meowing.zen.features.meowing

import meowing.zen.Zen
import meowing.zen.config.ConfigDelegate
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.TickEvent
import meowing.zen.features.Feature
import meowing.zen.utils.ItemUtils.createPlayerSkullByName
import net.minecraft.item.ItemStack
import java.time.LocalDateTime
import java.time.Month
import java.time.Year
import java.time.format.TextStyle
import java.util.*

@Zen.Module
object CatHead : Feature("cathead") {
    private val username by ConfigDelegate<String>("catheaduser")
    private val currentDate = (Month.entries[LocalDateTime.now().monthValue] - 1).getDisplayName(TextStyle.FULL, Locale.getDefault()) + " ${Year.now()}"
    private var helmet: ItemStack? = null
    // TODO: Store the current helmet that the player is wearing and properly replace so animated heads dont fuck up
//    private val Data = object : Data("CatHead") {
//        var helmet: ItemStack? = null
//        var helmetName: String? = null
//    }

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Meowing", "Cat Head", "", ConfigElement(
                "cathead",
                "Cat head",
                ElementType.Switch(false)
            ), isSectionToggle = true)
            .addElement("Meowing", "Cat Head", "Options", ConfigElement(
                "catheaduser",
                "Username for Cat Head",
                ElementType.TextInput("shikiimori", "Enter username...", 16)
            ))
    }

    override fun initialize() {
        Zen.configUI.registerCloseListener {
            createPlayerSkullByName(
                username,
                "§d§lCat",
                listOf(
                    "§7Crafted from the finest cosmic whiskers",
                    "§7and infused with ancient feline magic",
                    "",
                    "§7Owner: §b${player?.name ?: username}",
                    "§7Blessed by: §6The Great Cat Lords",
                    "",
                    "§8Edition #${(1..9999).random()}",
                    "§8$currentDate",
                    "",
                    "§8Grants +9 Lives",
                    "§8Perfect Landing Guaranteed",
                    "§d§lEXCLUSIVE MEOW EDITION"
                )
            ) { it ->
                it?.let {
                    helmet = it
                }
            }
        }

        register<TickEvent.Client> { event ->
            player?.inventory?.armorInventory?.set(3, helmet)
        }
    }
}