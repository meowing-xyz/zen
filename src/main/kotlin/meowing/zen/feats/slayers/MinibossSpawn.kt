package meowing.zen.feats.slayers

import meowing.zen.Zen
import meowing.zen.Zen.Companion.prefix
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.EntityEvent
import meowing.zen.events.WorldEvent
import meowing.zen.feats.Feature
import meowing.zen.feats.carrying.CarryCounter
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.TickUtils
import meowing.zen.utils.Utils
import meowing.zen.utils.Utils.removeFormatting

@Zen.Module
object MinibossSpawn : Feature("minibossspawn") {
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
        register<EntityEvent.Join> { event ->
            if (entities.contains(event.entity.entityId)) return@register
            if (CarryCounter.carryees.isEmpty() && SlayerTimer.spawnTime.isZero) return@register
            if (event.entity.getDistanceToEntity(player) > 10) return@register
            TickUtils.scheduleServer(2) {
                val entity = event.entity
                val name = entity.name?.removeFormatting()?.replace(regex, "") ?: return@scheduleServer
                if (names.contains(name)) {
                    Utils.playSound("mob.cat.meow", 1f, 1f)
                    ChatUtils.addMessage("$prefix §b$name§fspawned.")
                    entities.add(entity.entityId)
                }
            }
        }

        register<WorldEvent.Change> {
            entities.clear()
        }
    }
}