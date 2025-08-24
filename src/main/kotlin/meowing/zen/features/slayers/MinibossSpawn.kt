package meowing.zen.features.slayers

import meowing.zen.Zen
import meowing.zen.Zen.Companion.prefix
import meowing.zen.config.ConfigDelegate
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.EntityEvent
import meowing.zen.features.Feature
import meowing.zen.features.carrying.CarryCounter
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.Utils
import meowing.zen.utils.Utils.removeFormatting

@Zen.Module
object MinibossSpawn : Feature("minibossspawn") {
    private val carrycounter by ConfigDelegate<Boolean>("carrycounter")
    private val slayertimer by ConfigDelegate<Boolean>("slayertimer")
    private val names = listOf(
        "Atoned Revenant ", "Atoned Champion ", "Deformed Revenant ", "Revenant Champion ", "Revenant Sycophant ",
        "Mutant Tarantula ", "Tarantula Beast ", "Tarantula Vermin ",
        "Sven Alpha ", "Sven Follower ", "Pack Enforcer ",
        "Voidcrazed Maniac ", "Voidling Radical ", "Voidling Devotee "
    )
    private val regex = "\\d[\\d.,]*[kKmMbBtT]?❤?$".toRegex()

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Slayers", "Miniboss spawn alert", ConfigElement(
                "minibossspawn",
                "Miniboss spawn alert",
                ElementType.Switch(false)
            ), isSectionToggle = true)
    }

    override fun initialize() {
        register<EntityEvent.Spawn> { event ->
            if (event.entity.getDistanceToEntity(player) > 10) return@register
            if ((carrycounter && CarryCounter.carryees.isEmpty()) && (slayertimer && SlayerTimer.spawnTime.isZero)) return@register
            val name = event.name.removeFormatting().replace(regex, "")
            if (names.contains(name)) {
                Utils.playSound("mob.cat.meow", 1f, 1f)
                ChatUtils.addMessage("$prefix §b$name§fspawned.")
            }
        }
    }
}