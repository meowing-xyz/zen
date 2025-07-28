package meowing.zen.utils

import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.universal.UGraphics
import gg.essential.universal.UResolution
import meowing.zen.Zen.Companion.mc
import meowing.zen.mixins.AccessorGuiNewChat
import meowing.zen.mixins.AccessorMinecraft
import net.minecraft.client.gui.ChatLine
import net.minecraft.client.gui.GuiNewChat
import net.minecraft.util.EnumParticleTypes
import org.apache.commons.lang3.SystemUtils
import java.awt.Color

object Utils {
    private val emoteRegex = "[^\\u0000-\\u007F]".toRegex()

    inline val partialTicks get(): Float = (mc as AccessorMinecraft).timer.renderPartialTicks

    fun playSound(soundName: String, volume: Float, pitch: Float) {
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
        mc.theWorld?.spawnParticle(particle, x, y, z, velocityX, velocityY, velocityZ)
    }

    fun String.removeFormatting(): String {
        return this.replace(Regex("[§&][0-9a-fk-or]", RegexOption.IGNORE_CASE), "")
    }

    fun String.removeEmotes() = replace(emoteRegex, "")

    fun convertToFloat(value: Any): Float = when (value) {
        is Double -> value.toFloat()
        is Float -> value
        is Int -> value.toFloat()
        else -> 1.0f
    }

    fun Map<*, *>.toColorFromMap(): Color? {
        return try {
            val r = (get("r") as? Number)?.toInt() ?: 255
            val g = (get("g") as? Number)?.toInt() ?: 255
            val b = (get("b") as? Number)?.toInt() ?: 255
            val a = (get("a") as? Number)?.toInt() ?: 255
            Color(r, g, b, a)
        } catch (e: Exception) {
            null
        }
    }

    fun List<*>.toColorFromList(): Color? {
        return try {
            if (size < 4) return null
            Color(
                (this[0] as? Number)?.toInt() ?: return null,
                (this[1] as? Number)?.toInt() ?: return null,
                (this[2] as? Number)?.toInt() ?: return null,
                (this[3] as? Number)?.toInt() ?: return null
            )
        } catch (e: Exception) {
            null
        }
    }

    fun Int.toColorFloat(): Float {
        return this / 255f
    }

    fun createBlock(radius: Float = 0f): UIComponent {
        return if (SystemUtils.IS_OS_MAC_OSX) UIBlock() else UIRoundedRectangle(radius)
    }

    inline fun <reified R> Any.getField(name: String): R = javaClass.getDeclaredField(name).apply { isAccessible = true }[this] as R

    /*
     * Skytils - Hypixel Skyblock Quality of Life Mod
     * Copyright (C) 2020-2023 Skytils
     */
    fun GuiNewChat.getChatLine(mouseX: Int, mouseY: Int): ChatLine? {
        if (chatOpen && this is AccessorGuiNewChat) {
            val extraOffset = if (
                runCatching {
                    Class.forName("club.sk1er.patcher.config.PatcherConfig")
                        .getDeclaredConstructor()
                        .newInstance()
                        .getField<Boolean>("chatPosition")
                }.getOrNull() == true
            ) 12 else 0
            val x = ((mouseX - 3) / chatScale)
            val y = (((UResolution.scaledHeight - mouseY) - 30 - extraOffset) / chatScale)

            if (x >= 0 && y >= 0) {
                val l = lineCount.coerceAtMost(drawnChatLines.size)
                if (x <= chatWidth / chatScale && y < UGraphics.getFontHeight() * l + l) {
                    val lineNum = y / UGraphics.getFontHeight() + scrollPos
                    return drawnChatLines.getOrNull(lineNum.toInt())
                }
            }
        }
        return null
    }

    fun decodeRoman(roman: String): Int {
        val values = mapOf('I' to 1, 'V' to 5, 'X' to 10, 'L' to 50, 'C' to 100, 'D' to 500, 'M' to 1000)
        var result = 0
        var prev = 0

        for (char in roman.reversed()) {
            val current = values[char] ?: 0
            if (current < prev) result -= current
            else result += current
            prev = current
        }
        return result
    }

    fun Long.toFormattedDuration(short: Boolean = false): String {
        val seconds = this / 1000
        val days = seconds / 86400
        val hours = (seconds % 86400) / 3600
        val minutes = (seconds % 3600) / 60
        val remainingSeconds = seconds % 60

        if (short) {
            return when {
                days > 0 -> "${days}d"
                hours > 0 -> "${hours}h"
                minutes > 0 -> "${minutes}m"
                else -> "${remainingSeconds}s"
            }
        }

        return buildString {
            if (days > 0) append("${days}d ")
            if (hours > 0) append("${hours}h ")
            if (minutes > 0) append("${minutes}m ")
            if (remainingSeconds > 0) append("${remainingSeconds}s")
        }.trimEnd()
    }
}