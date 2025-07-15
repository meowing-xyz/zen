package meowing.zen.feats.slayers

import meowing.zen.Zen
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.EntityEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.Utils
import meowing.zen.utils.Utils.removeFormatting

@Zen.Module
object minibossspawn : Feature("minibossspawn") {
    private val entities = mutableListOf<Int>()
    private val names = listOf(
        "Atoned Revenant ", "Atoned Champion ", "Deformed Revenant ", "Revenant Champion ", "Revenant Sycophant ",
        "Mutant Tarantula ", "Tarantula Beast ", "Tarantula Vermin ",
        "Sven Alpha ", "Sven Follower ", "Pack Enforcer ",
        "Voidcrazed Maniac ", "Voidling Radical ", "Voidling Devotee "
    )
    private val regex = "\\d[\\d.,]*[kKmMbBtT]?❤?$".toRegex()

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Slayers", "Miniboss", ConfigElement(
                "minibossspawn",
                "Miniboss spawn alert",
                "Plays a sound when a miniboss spawns near you.",
                ElementType.Switch(false)
            ))
    }

    override fun initialize() {
        register<EntityEvent.Metadata> { event ->
            if (entities.contains(event.packet.entityId)) return@register
            event.packet.func_149376_c()?.find { it.dataValueId == 2 && it.`object` is String }?.let { obj ->
                val name = (obj.`object` as String).removeFormatting()
                val clean = name.removeFormatting().replace(regex, "")
                if (names.contains(clean)) {
                    Utils.playSound("mob.cat.meow", 1f, 1f)
                    ChatUtils.addMessage("§c[Zen] §b$clean§fspawned.")
                    entities.add(event.packet.entityId)
                }
            }
        }
    }
}