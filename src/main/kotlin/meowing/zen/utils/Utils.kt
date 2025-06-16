package meowing.zen.utils

import net.minecraft.client.Minecraft
import net.minecraft.util.EnumParticleTypes

object Utils {
    fun playSound(soundName: String, volume: Float, pitch: Float) {
        val mc = Minecraft.getMinecraft()
        if (mc.thePlayer != null && mc.theWorld != null) {
            mc.theWorld.playSound(
                mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ,
                soundName, volume, pitch, false
            )
        }
    }

    fun spawnParticle(particle: EnumParticleTypes, x: Double, y: Double, z: Double) {
        spawnParticle(particle, x, y, z, 0.0, 0.0, 0.0)
    }

    fun spawnParticle(particle: EnumParticleTypes, x: Double, y: Double, z: Double, velocityX: Double, velocityY: Double, velocityZ: Double) {
        val mc = Minecraft.getMinecraft()
        mc.theWorld?.spawnParticle(particle, x, y, z, velocityX, velocityY, velocityZ)
    }

    fun spawnParticleAtPlayer(particle: EnumParticleTypes, velocityX: Double, velocityY: Double, velocityZ: Double) {
        val mc = Minecraft.getMinecraft()
        mc.thePlayer?.let { player ->
            spawnParticle(particle,
                player.posX,
                player.posY + 1.0,
                player.posZ,
                velocityX, velocityY, velocityZ)
        }
    }

    fun showTitle(title: String?, subtitle: String?, duration: Int) {
        Minecraft.getMinecraft().ingameGUI.displayTitle(title, "", -1, -1, -1)
        Minecraft.getMinecraft().ingameGUI.displayTitle(null, subtitle, -1, -1, -1)
        Minecraft.getMinecraft().ingameGUI.displayTitle(null, null, -1, duration, -1)
    }

    fun showTitle(title: String, subtitle: String, fadeIn: Int, stay: Int, fadeOut: Int) {
        Minecraft.getMinecraft().ingameGUI.displayTitle(title, "", fadeIn, stay, fadeOut)
        Minecraft.getMinecraft().ingameGUI.displayTitle(null, subtitle, fadeIn, stay, fadeOut)
    }
}