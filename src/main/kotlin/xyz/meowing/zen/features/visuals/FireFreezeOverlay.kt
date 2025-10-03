package xyz.meowing.zen.features.visuals

import xyz.meowing.zen.Zen
import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ui.ConfigUI
import xyz.meowing.zen.config.ui.types.ConfigElement
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.RenderEvent
import xyz.meowing.zen.events.SkyblockEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.utils.OutlineUtils
import xyz.meowing.zen.utils.Render3D
import xyz.meowing.zen.utils.TickUtils
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import java.awt.Color

@Zen.Module
object FireFreezeOverlay : Feature("firefreezeoverlay", true) {
    private var activatedPos: Vec3? = null
    private var overlayTimerId: Long? = null
    private var freezeTimerId: Long? = null
    private var frozenEntities = mutableSetOf<EntityLivingBase>()
    private val firefreezeoverlaycolor by ConfigDelegate<Color>("firefreezeoverlaycolor")

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Visuals", "Fire freeze overlay", ConfigElement(
                "firefreezeoverlay",
                null,
                ElementType.Switch(false)
            ), isSectionToggle = true)
            .addElement("Visuals", "Fire freeze overlay", "Color", ConfigElement(
                "firefreezeoverlaycolor",
                "Fire Freeze Overlay color",
                ElementType.ColorPicker(Color(0, 255, 255, 127))
            ))
    }

    override fun initialize() {
        register<SkyblockEvent.ItemAbilityUsed> { event ->
            if (event.ability.itemId == "FIRE_FREEZE_STAFF") {
                activatedPos = player?.positionVector
                frozenEntities.clear()

                overlayTimerId = createTimer(100) {
                    overlayTimerId = null
                }

                TickUtils.scheduleServer(100) {
                    freezeTimerId = createTimer(200,
                        onComplete = {
                            frozenEntities.clear()
                            freezeTimerId = null
                        }
                    )

                    world?.loadedEntityList?.forEach { ent ->
                        if (ent is EntityLivingBase && ent !is EntityArmorStand && !ent.isInvisible && ent != player && ent.getDistanceSq(
                                BlockPos(activatedPos)
                            ) <= 25) {
                            frozenEntities.add(ent)
                        }
                    }
                }
            }
        }

        register<RenderEvent.World> { event ->
            val timer = overlayTimerId?.let { getTimer(it) } ?: return@register
            val pos = activatedPos ?: return@register
            val text = "§b${"%.1f".format(timer.ticks / 20.0)}s"

            Render3D.drawFilledCircle(
                pos,
                5f,
                72,
                firefreezeoverlaycolor.darker(),
                firefreezeoverlaycolor,
                event.partialTicks
            )

            Render3D.drawString(
                text,
                pos.addVector(0.0, 1.0, 0.0),
                event.partialTicks
            )
        }

        register<RenderEvent.World> { event ->
            val timer = freezeTimerId?.let { getTimer(it) } ?: return@register
            frozenEntities.removeAll { it.isDead }
            frozenEntities.forEach { ent ->
                val entityPos = Vec3(ent.posX, ent.posY + (ent.height / 2), ent.posZ)
                val freezeText = "§b${"%.1f".format(timer.ticks / 20.0)}s"
                Render3D.drawString(freezeText, entityPos, event.partialTicks)
            }
        }

        register<RenderEvent.EntityModel> { event ->
            if (event.entity in frozenEntities) OutlineUtils.outlineEntity(event, firefreezeoverlaycolor)
        }
    }

    override fun onUnregister() {
        overlayTimerId = null
        freezeTimerId = null
        frozenEntities.clear()
        super.onUnregister()
    }
}