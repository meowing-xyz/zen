package meowing.zen.features.slayers

import meowing.zen.Zen
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.EntityEvent
import meowing.zen.events.RenderEvent
import meowing.zen.features.Feature
import meowing.zen.hud.HUDManager
import meowing.zen.utils.Render2D
import meowing.zen.utils.Render2D.width
import meowing.zen.utils.Utils.removeFormatting
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity

@Zen.Module
object SlayerHUD : Feature("slayerhud") {
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

        createCustomEvent<RenderEvent.HUD>("render") {
            if (HUDManager.isEnabled(name)) render()
        }

        register<EntityEvent.Leave> { event ->
            if (event.entity.entityId == bossID?.minus(3)) {
                unregisterEvent("render")
                bossID = null
            }
        }
    }

    fun handleSpawn(id: Int) {
        val world = world ?: return
        bossID = id
        timerEntity = world.getEntityByID(id - 1)
        hpEntity = world.getEntityByID(id - 2)
        registerEvent("render")
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
        Render2D.renderStringWithShadow(time, (hpWidth - timeWidth) / 2f, 0f, scale)
        Render2D.renderStringWithShadow(hp, 0f, 10f, scale)
        GlStateManager.popMatrix()
    }
}