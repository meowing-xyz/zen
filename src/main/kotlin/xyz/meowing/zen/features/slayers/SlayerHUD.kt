package xyz.meowing.zen.features.slayers

import xyz.meowing.zen.Zen
import xyz.meowing.zen.config.ui.ConfigUI
import xyz.meowing.zen.config.ui.types.ConfigElement
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.RenderEvent
import xyz.meowing.zen.events.SkyblockEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.hud.HUDManager
import xyz.meowing.zen.utils.Render2D
import xyz.meowing.zen.utils.Render2D.width
import xyz.meowing.zen.utils.Utils.removeFormatting
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity

@Zen.Module
object SlayerHUD : Feature("slayerhud", true) {
    private const val name = "Slayer HUD"
    private var timerEntity: Entity? = null
    private var hpEntity: Entity? = null
    private var bossID: Int? = null

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Slayers", "Slayer HUD", ConfigElement(
                "slayerhud",
                "Slayer HUD",
                ElementType.Switch(false)
            ), isSectionToggle = true)
    }

    override fun initialize() {
        HUDManager.register(name, "§c02:59\n§c☠ §bVoidgloom Seraph IV §e64.2M§c❤")

        createCustomEvent<RenderEvent.Text>("render") {
            if (HUDManager.isEnabled(name)) render()
        }

        register<SkyblockEvent.Slayer.Spawn> { event ->
            val world = world ?: return@register
            bossID = event.entityID
            timerEntity = world.getEntityByID(event.packet.entityId - 1)
            hpEntity = world.getEntityByID(event.packet.entityId - 2)
            registerEvent("render")
        }

        register<SkyblockEvent.Slayer.Death> {
            unregisterEvent("render")
            bossID = null
        }

        register<SkyblockEvent.Slayer.Fail> {
            unregisterEvent("render")
            bossID = null
        }

        register<SkyblockEvent.Slayer.Cleanup> {
            unregisterEvent("render")
            bossID = null
        }
    }

    private fun render() {
        val time = timerEntity?.name ?: return
        val hp = hpEntity?.name ?: return
        val x = HUDManager.getX(name)
        val y = HUDManager.getY(name)
        val scale = HUDManager.getScale(name)

        GlStateManager.pushMatrix()
        GlStateManager.translate(x, y, 0f)
        val hpWidth = hp.removeFormatting().width()
        val timeWidth = time.removeFormatting().width()
        Render2D.renderString(time, (hpWidth - timeWidth) / 2f, 0f, scale)
        Render2D.renderString(hp, 0f, 10f, scale)
        GlStateManager.popMatrix()
    }
}