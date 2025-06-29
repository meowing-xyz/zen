package meowing.zen.utils

import meowing.zen.Zen.Companion.mc
import meowing.zen.mixins.AccessorMinecraft
import net.minecraft.client.Minecraft
import net.minecraft.util.EnumParticleTypes

object Utils {
    private val emoteRegex = "[^\\u0000-\\u007F]".toRegex()

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

    fun String.removeFormatting(): String {
        return this.replace(Regex("[§&][0-9a-fk-or]", RegexOption.IGNORE_CASE), "")
    }

    fun String.removeEmotes() = replace(emoteRegex, "")

    fun getPartialTicks(): Float = (mc as AccessorMinecraft).timer.renderPartialTicks

    fun convertToFloat(value: Any): Float = when (value) {
        is Double -> value.toFloat()
        is Float -> value
        is Int -> value.toFloat()
        else -> 1.0f
    }

    inline fun <reified R> Any.getField(name: String): R = javaClass.getDeclaredField(name).apply { isAccessible = true }[this] as R
}