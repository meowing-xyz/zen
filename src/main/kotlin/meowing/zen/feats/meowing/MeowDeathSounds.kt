package meowing.zen.feats.meowing

import meowing.zen.Zen
import meowing.zen.feats.Feature
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.EntityEvent
import meowing.zen.utils.Utils
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.util.EnumParticleTypes
import kotlin.random.Random

@Zen.Module
object MeowDeathSounds : Feature("meowdeathsounds") {
    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Meowing", "Meow Sounds", ConfigElement(
                "meowdeathsounds",
                "Meow Death Sounds",
                "Plays a cat sound whenever an entity dies",
                ElementType.Switch(false)
            ))
    }

    override fun initialize() {
        register<EntityEvent.Leave> { event ->
            val entity = event.entity
            if (entity is EntityArmorStand || entity.isInvisible) return@register
            Utils.playSound("mob.cat.meow", 0.8f, 1.0f)
        }
    }
}