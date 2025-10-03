package xyz.meowing.zen.utils

import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.universal.UGraphics
import gg.essential.universal.UResolution
import xyz.meowing.zen.Zen.Companion.mc
import xyz.meowing.zen.mixins.AccessorGuiNewChat
import xyz.meowing.zen.mixins.AccessorMinecraft
import net.minecraft.client.gui.ChatLine
import net.minecraft.client.gui.GuiNewChat
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.SharedMonsterAttributes
import net.minecraft.inventory.ContainerChest
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import org.apache.commons.lang3.SystemUtils
import java.awt.Color

object Utils {
    private val formatRegex = "[§&][0-9a-fk-or]".toRegex()

    inline val partialTicks get(): Float = (mc as AccessorMinecraft).timer.renderPartialTicks

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

    val EntityLivingBase.baseMaxHealth: Int get() = this.getEntityAttribute(SharedMonsterAttributes.maxHealth).baseValue.toInt()
}