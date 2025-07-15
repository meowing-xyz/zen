package meowing.zen.feats.meowing

import meowing.zen.Zen
import meowing.zen.feats.Feature
import meowing.zen.Zen.Companion.mc
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.EntityEvent
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.util.EnumParticleTypes
import kotlin.random.Random

@Zen.Module
object meowdeathsounds : Feature("meowdeathsounds") {
    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Meowing", "Meow Sounds", ConfigElement(
                "meowsounds",
                "Meow Sounds",
                "Plays a cat sound whenever someone sends \"meow\" in chat",
                ElementType.Switch(false)
            ))
    }

    override fun initialize() {
        register<EntityEvent.Leave> {
            val entity = it.entity
            if (entity is EntityArmorStand || entity.isInvisible) return@register
            mc.theWorld?.playSound(entity.posX, entity.posY, entity.posZ, "mob.cat.meow", 0.8f, 1.0f, false)

            repeat(5) {
                mc.theWorld?.spawnParticle(
                    EnumParticleTypes.NOTE,
                    entity.posX + (Random.nextDouble() - 0.5),
                    entity.posY + 1.0 + Random.nextDouble() * 0.5,
                    entity.posZ + (Random.nextDouble() - 0.5),
                    0.0, 0.2, 0.0
                )
            }
        }
    }
}