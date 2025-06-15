package meowing.zen.feats.meowing

import meowing.zen.Zen
import net.minecraft.client.Minecraft
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.util.EnumParticleTypes
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.random.Random

class meowdeathsounds private constructor() {
    companion object {
        private val instance = meowdeathsounds()

        @JvmStatic
        fun initialize() {
            Zen.registerListener("meowdeathsounds", instance)
        }
    }

    @SubscribeEvent
    fun onEntityDeath(event: LivingDeathEvent) {
        val entity = event.entityLiving
        if (entity is EntityArmorStand || entity.isInvisible) return

        val mc = Minecraft.getMinecraft()
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