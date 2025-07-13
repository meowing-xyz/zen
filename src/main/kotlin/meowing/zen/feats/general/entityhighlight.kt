package meowing.zen.feats.general

import meowing.zen.Zen.Companion.config
import meowing.zen.Zen.Companion.mc
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.RenderEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.ItemUtils.isHolding
import meowing.zen.utils.ItemUtils.isShortbow
import meowing.zen.utils.OutlineUtils
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.monster.EntityMob
import net.minecraft.entity.passive.EntityAnimal
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.projectile.EntityArrow
import net.minecraft.item.ItemBow
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.MovingObjectPosition
import net.minecraft.util.Vec3
import java.awt.Color
import kotlin.math.*

object entityhighlight : Feature("entityhighlight") {
    private var bowTargetEntity: Entity? = null

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("General", "Entity highlight", ConfigElement(
                "entityhighlight",
                "Entity highlight",
                "Highlights the entity you are looking at",
                ElementType.Switch(false)
            ))
            .addElement("General", "Entity highlight", ConfigElement(
                "entityhighlightbow",
                "Bow prediction",
                "Highlights the entity that your bow will hit when shot",
                ElementType.Switch(false),
                { config -> config["entityhighlight"] as? Boolean == true }
            ))
            .addElement("General", "Entity highlight", ConfigElement(
                "entityhighlightplayercolor",
                "Player color",
                "Color for highlighted players",
                ElementType.ColorPicker(Color(0, 255, 255, 255)),
                { config -> config["entityhighlight"] as? Boolean == true }
            ))
            .addElement("General", "Entity highlight", ConfigElement(
                "entityhighlightmobcolor",
                "Mob color",
                "Color for highlighted mobs",
                ElementType.ColorPicker(Color(255, 0, 0, 255)),
                { config -> config["entityhighlight"] as? Boolean == true }
            ))
            .addElement("General", "Entity highlight", ConfigElement(
                "entityhighlightanimalcolor",
                "Animal color",
                "Color for highlighted animals",
                ElementType.ColorPicker(Color(0, 255, 0, 255)),
                { config -> config["entityhighlight"] as? Boolean == true }
            ))
            .addElement("General", "Entity highlight", ConfigElement(
                "entityhighlightothercolor",
                "Other entity color",
                "Color for other highlighted entities",
                ElementType.ColorPicker(Color(255, 255, 255, 255)),
                { config -> config["entityhighlight"] as? Boolean == true }
            ))
            .addElement("General", "Entity highlight", ConfigElement(
                "entityhighlightwidth",
                "Entity highlight width",
                "Width of the entity highlight outline",
                ElementType.Slider(1.0, 10.0, 2.0, false),
                { config -> config["entityhighlight"] as? Boolean == true }
            ))
    }

    override fun initialize() {
        register<RenderEvent.EntityModel> { event ->
            val entity = event.entity
            val player = mc.thePlayer ?: return@register

            if (entity == player || entity.isInvisible) return@register

            val shouldHighlight = if (config.entityhighlightbow && player.heldItem?.item is ItemBow) {
                updateTarget()
                entity == bowTargetEntity
            } else {
                isEntityUnderCrosshair(entity)
            }

            if (!shouldHighlight) return@register

            OutlineUtils.outlineEntity(
                event,
                getEntityColor(entity),
                config.entityhighlightwidth,
                false
            )
        }
    }

    private fun isEntityUnderCrosshair(entity: Entity): Boolean {
        val mouseOver = mc.objectMouseOver ?: return false
        return mouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY && mouseOver.entityHit == entity
    }

    private fun getEntityColor(entity: Entity): Color {
        return when (entity) {
            is EntityPlayer -> config.entityhighlightplayercolor
            is EntityMob -> config.entityhighlightmobcolor
            is EntityAnimal -> config.entityhighlightanimalcolor
            else -> config.entityhighlightothercolor
        }
    }

    private fun updateTarget() {
        val player = mc.thePlayer ?: return
        val heldItem = player.heldItem

        if (heldItem?.item !is ItemBow || (!heldItem.isShortbow && player.itemInUseDuration == 0)) {
            bowTargetEntity = null
            return
        }

        bowTargetEntity = calcTarget()
    }

    private fun calcTarget(): Entity? {
        val player = mc.thePlayer ?: return null
        val heldItem = player.heldItem ?: return null

        val isShortbow = heldItem.isShortbow
        val charge = if (isShortbow) {
            2f
        } else {
            if (player.itemInUseDuration == 0) return null
            minOf((72000 - player.itemInUseCount) / 20f, 1.0f) * 2
        }

        return if (isShortbow) {
            val targets = mutableListOf<Entity?>()
            targets.add(getTarget(0f, charge))

            if (isHolding("TERMINATOR")) {
                targets.add(getTarget(-5f, charge))
                targets.add(getTarget(5f, charge))
            }

            targets.filterNotNull().firstOrNull()
        } else {
            getTarget(0f, charge)
        }
    }

    private fun getTarget(yawOffset: Float, charge: Float): Entity? {
        val player = mc.thePlayer ?: return null

        val yawRadians = Math.toRadians((player.rotationYaw + yawOffset).toDouble())
        val pitchRadians = Math.toRadians(player.rotationPitch.toDouble())

        val posX = player.posX - cos(Math.toRadians(player.rotationYaw.toDouble())) * 0.16
        val posY = player.posY + player.eyeHeight - 0.1
        val posZ = player.posZ - sin(Math.toRadians(player.rotationYaw.toDouble())) * 0.16

        var motionX = -sin(yawRadians) * cos(pitchRadians)
        var motionY = -sin(pitchRadians)
        var motionZ = cos(yawRadians) * cos(pitchRadians)

        val lengthOffset = sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ)
        val velocity = charge * 1.5 / lengthOffset

        motionX *= velocity
        motionY *= velocity
        motionZ *= velocity

        return simulateArrow(Vec3(motionX, motionY, motionZ), Vec3(posX, posY, posZ))
    }

    private fun simulateArrow(motionVec: Vec3, posVec: Vec3): Entity? {
        var motion = motionVec
        var pos = posVec

        repeat(30) {
            val aabb = AxisAlignedBB(0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
                .offset(pos.xCoord, pos.yCoord, pos.zCoord)
                .addCoord(motion.xCoord, motion.yCoord, motion.zCoord)
                .expand(0.01, 0.01, 0.01)

            val entityHit = mc.theWorld?.getEntitiesWithinAABBExcludingEntity(mc.thePlayer, aabb)?.firstOrNull { it !is EntityArrow && it !is EntityArmorStand }

            if (entityHit != null) return entityHit

            val blockHit = mc.theWorld?.rayTraceBlocks(pos, pos.add(motion), false, true, false)
            if (blockHit != null) return null

            pos = pos.add(motion)
            motion = Vec3(
                motion.xCoord * 0.99,
                motion.yCoord * 0.99 - 0.05,
                motion.zCoord * 0.99
            )
        }

        return null
    }
}