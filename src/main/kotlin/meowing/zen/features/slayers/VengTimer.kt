package meowing.zen.features.slayers

import meowing.zen.Zen
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.EntityEvent
import meowing.zen.events.RenderEvent
import meowing.zen.events.SkyblockEvent
import meowing.zen.features.Feature
import meowing.zen.hud.HUDManager
import meowing.zen.utils.LoopUtils.setTimeout
import meowing.zen.utils.Render2D
import meowing.zen.utils.TickUtils
import meowing.zen.utils.TimeUtils
import meowing.zen.utils.TimeUtils.fromNow
import meowing.zen.utils.TimeUtils.millis
import meowing.zen.utils.Utils.removeFormatting
import net.minecraft.entity.monster.EntityBlaze
import kotlin.time.Duration.Companion.seconds

@Zen.Module
object VengTimer : Feature("vengtimer") {
    private const val name = "VengTimer"
    private var starttime = TimeUtils.zero
    private var hit = false
    private var isFighting = false
    private var cachedNametag: net.minecraft.entity.Entity? = null

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Slayers", "Vengeance proc timer", ConfigElement(
                "vengtimer",
                "Vengeance proc timer",
                ElementType.Switch(false)
            ), isSectionToggle = true)
    }

    override fun initialize() {
        HUDManager.register("VengTimer", "§bVeng proc: §c4.3s")

        createCustomEvent<RenderEvent.Text>("render") {
            if (HUDManager.isEnabled("VengTimer")) render()
        }

        register<SkyblockEvent.Slayer.QuestStart> {
            isFighting = true
        }

        register<SkyblockEvent.Slayer.Death> {
            cleanup()
        }

        register<SkyblockEvent.Slayer.Fail> {
            TickUtils.scheduleServer(10) {
                cleanup()
            }
        }

        register<EntityEvent.Attack> { event ->
            if (hit || event.target !is EntityBlaze || !isFighting) return@register

            val player = player ?: return@register
            val heldItem = player.heldItem ?: return@register

            if (event.entityPlayer.name != player.name || !heldItem.displayName.removeFormatting().contains("Pyrochaos Dagger", true)) return@register

            val nametagEntity = cachedNametag ?: world?.loadedEntityList?.find { entity ->
                val name = entity.name?.removeFormatting() ?: return@find false
                name.contains("Spawned by") && name.endsWith("by: ${player.name}")
            }?.also { cachedNametag = it }

            if (nametagEntity != null && event.target.entityId == (nametagEntity.entityId - 3)) {
                starttime = 6.seconds.fromNow
                hit = true
                registerEvent("render")
                setTimeout(5950) {
                    starttime = TimeUtils.zero
                    hit = false
                    unregisterEvent("render")
                }
            }
        }
    }

    private fun render() {
        val x = HUDManager.getX(name)
        val y = HUDManager.getY(name)
        val scale = HUDManager.getScale(name)
        val text = getText()
        if (text.isNotEmpty()) Render2D.renderStringWithShadow(text, x, y, scale)
    }

    private fun getText(): String {
        if (hit && starttime.isInFuture) {
            val timeLeft = starttime.until
            return "§bVeng proc: §c${"%.1f".format(timeLeft.millis / 1000.0)}s"
        }
        return ""
    }

    private fun cleanup() {
        isFighting = false
        cachedNametag = null
        starttime = TimeUtils.zero
        unregisterEvent("render")
    }
}