package xyz.meowing.zen.features.hud

import xyz.meowing.zen.Zen
import xyz.meowing.zen.config.ui.ConfigUI
import xyz.meowing.zen.config.ui.types.ConfigElement
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.EntityEvent
import xyz.meowing.zen.events.MouseEvent
import xyz.meowing.zen.events.RenderEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.hud.HUDManager
import xyz.meowing.zen.utils.ItemUtils.extraAttributes
import xyz.meowing.zen.utils.LoopUtils
import xyz.meowing.zen.utils.Render2D

@Zen.Module
object FatalTempoOverlay : Feature("fataltempooverlay", true) {
    private val hits = mutableListOf<Long>()
    private var level = 0
    private var currentPercent = 0

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("HUD", "Fatal Tempo Overlay", ConfigElement(
                "fataltempooverlay",
                null,
                ElementType.Switch(false)
            ), isSectionToggle = true)
    }

    override fun initialize() {
        HUDManager.register("Fatal Tempo", "§eFatal Tempo: §a200%")

        register<EntityEvent.Interact> {
            checkFatal()
        }

        register<MouseEvent.Click> { event ->
            if (event.event.button == 0) checkFatal()
        }

        register<RenderEvent.Text> {
            if (HUDManager.isEnabled("Fatal Tempo")) render()
        }
    }

    private fun checkFatal() {
        val item = player?.heldItem ?: return
        val enchantments = item.extraAttributes?.getCompoundTag("enchantments") ?: return
        val ftLevel = enchantments.getInteger("ultimate_fatal_tempo")
        if (ftLevel <= 0) return

        level = ftLevel * if (item.displayName.contains("Terminator")) 3 else 1
        val currentTime = System.currentTimeMillis()
        hits.add(currentTime)
        hits.removeAll { currentTime - it > 3000 }
        currentPercent = minOf(200, hits.size * level * 10)

        LoopUtils.setTimeout(3100) {
            hits.removeAll { System.currentTimeMillis() - it > 3000 }
            currentPercent = minOf(200, hits.size * level * 10)
        }
    }

    private fun render() {
        val x = HUDManager.getX("Fatal Tempo")
        val y = HUDManager.getY("Fatal Tempo")
        val scale = HUDManager.getScale("Fatal Tempo")

        val color = if (currentPercent > 0) "§a" else "§c"
        val text = "§eFatal Tempo: $color$currentPercent%"

        Render2D.renderString(text, x, y, scale)
    }
}