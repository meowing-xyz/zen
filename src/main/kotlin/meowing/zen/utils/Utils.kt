package meowing.zen.utils

import com.google.common.collect.ComparisonChain
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
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.network.NetworkPlayerInfo
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.SharedMonsterAttributes
import net.minecraft.inventory.ContainerChest
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import net.minecraft.world.WorldSettings
import org.apache.commons.lang3.SystemUtils
import java.awt.Color
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.absoluteValue

object Utils {
    private val emoteRegex = "[^\\u0000-\\u007F]".toRegex()
    private val formatRegex = "[§&][0-9a-fk-or]".toRegex()
    private val suffixes = arrayOf(
        1000L to "k",
        1000000L to "m",
        1000000000L to "b",
        1000000000000L to "t",
        1000000000000000L to "p",
        1000000000000000000L to "e"
    )

    inline val partialTicks get(): Float = (mc as AccessorMinecraft).timer.renderPartialTicks

    inline val tabList: List<Pair<NetworkPlayerInfo, String>>
        get() = (mc.thePlayer?.sendQueue?.playerInfoMap?.sortedWith(Comparator<NetworkPlayerInfo> { o1, o2 ->
            if (o1 == null) return@Comparator - 1
            if (o2 == null) return@Comparator 0
            return@Comparator ComparisonChain.start().compareTrueFirst(
                o1.gameType != WorldSettings.GameType.SPECTATOR,
                o2.gameType != WorldSettings.GameType.SPECTATOR
            ).compare(
                o1.playerTeam?.registeredName ?: "",
                o2.playerTeam?.registeredName ?: ""
            ).compare(o1.gameProfile.name, o2.gameProfile.name).result()
        }) ?: emptyList()).map { Pair(it, mc.ingameGUI.tabList.getPlayerName(it)) }

    fun playSound(soundName: String, volume: Float, pitch: Float) {
        if (mc.thePlayer != null && mc.theWorld != null) {
            mc.theWorld.playSound(
                mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ,
                soundName, volume, pitch, false
            )
        }
    }

    fun String.removeFormatting(): String {
        return this.replace(formatRegex, "")
    }

    fun String.removeEmotes() = replace(emoteRegex, "")

    fun String.getRegexGroups(regex: Regex): MatchGroupCollection? {
        val regexMatchResult = regex.find(this) ?: return null
        return regexMatchResult.groups
    }

    fun format(value: Number): String {
        val longValue = value.toLong()

        when {
            longValue == Long.MIN_VALUE -> return format(Long.MIN_VALUE + 1)
            longValue < 0L -> return "-${format(-longValue)}"
            longValue < 1000L -> return longValue.toString()
        }

        val (threshold, suffix) = suffixes.findLast { longValue >= it.first } ?: return longValue.toString()
        val scaled = longValue * 10 / threshold

        return if (scaled < 100 && scaled % 10 != 0L) "${scaled / 10.0}$suffix" else "${scaled / 10}$suffix"
    }

    inline val GuiContainer.chestName: String get() {
        if (this.inventorySlots !is ContainerChest) return ""
        val chest = this.inventorySlots as ContainerChest
        val inv = chest.lowerChestInventory
        return inv.displayName.unformattedText.trim()
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

    fun createBlock(radius: Float = 0f): UIComponent {
        return if (SystemUtils.IS_OS_MAC_OSX) UIBlock() else UIRoundedRectangle(radius)
    }

    fun canSeePosition(playerPos: Vec3, renderPos: BlockPos): Boolean {
        val world = mc.theWorld ?: return false
        val endPos = Vec3(renderPos).add(Vec3(0.5, 0.5, 0.5))

        val rayTrace = world.rayTraceBlocks(
            playerPos,
            endPos,
            false, true, false
        )

        return rayTrace == null
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

        fun getFormattedDate(): String {
            val today = LocalDate.now()
            val day = today.dayOfMonth
            val suffix = getDaySuffix(day)
            val formatter = DateTimeFormatter.ofPattern("MMMM d'$suffix', yyyy", Locale.ENGLISH)
            return today.format(formatter)
        }

        private fun getDaySuffix(day: Int): String {
            return when {
                day in 11..13 -> "th"
                day % 10 == 1 -> "st"
                day % 10 == 2 -> "nd"
                day % 10 == 3 -> "rd"
                else -> "th"
            }
        }

    fun getPlayerTexture(
        playerUuid: String,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit = {}
    ) {
        NetworkUtils.getJson(
            url = "https://sessionserver.mojang.com/session/minecraft/profile/$playerUuid",
            onSuccess = { json ->
                val properties = json.getAsJsonArray("properties")
                properties?.forEach { element ->
                    val property = element.asJsonObject
                    if (property.get("name")?.asString == "textures") {
                        val texture = property.get("value")?.asString
                        if (texture != null) {
                            onSuccess(texture)
                            return@getJson
                        }
                    }
                }
                onError(IllegalArgumentException("No texture found for player UUID: $playerUuid"))
            },
            onError = onError
        )
    }

    fun getPlayerUuid(
        playerName: String,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit = {}
    ) {
        NetworkUtils.getJson(
            url = "https://api.mojang.com/users/profiles/minecraft/$playerName",
            onSuccess = { json ->
                val id = json.get("id")?.asString
                if (id != null) {
                    onSuccess(id)
                } else {
                    onError(IllegalArgumentException("No UUID found for player: $playerName"))
                }
            },
            onError = onError
        )
    }

    fun Number.formatNumber(): String {
        return "%,.0f".format(Locale.US, this.toDouble())
    }

    fun Number.abbreviateNumber(): String {
        val num = this.toDouble().absoluteValue
        val sign = if (this.toDouble() < 0) "-" else ""

        val (divisor, suffix) = when {
            num >= 1_000_000_000_000 -> 1_000_000_000_000.0 to "T"
            num >= 1_000_000_000 -> 1_000_000_000.0 to "B"
            num >= 1_000_000 -> 1_000_000.0 to "M"
            num >= 1_000 -> 1_000.0 to "k"
            else -> return sign + "%.0f".format(Locale.US, num)
        }

        val value = num / divisor
        val formatted = if (value % 1.0 == 0.0) {
            value.toInt().toString()
        } else {
            val decimal = "%.1f".format(Locale.US, value)
            if (decimal.endsWith(".0")) decimal.dropLast(2) else decimal
        }
        return sign + formatted + suffix
    }

    val EntityLivingBase.baseMaxHealth: Int get() = this.getEntityAttribute(SharedMonsterAttributes.maxHealth).baseValue.toInt()
}