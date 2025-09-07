package meowing.zen.features.visuals

import meowing.zen.Zen
import meowing.zen.config.ConfigDelegate
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.features.Feature
import net.minecraft.client.entity.AbstractClientPlayer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.item.ItemStack
import net.minecraft.util.MathHelper
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.pow

/**
 * Inspired by AtonAddons
 * Under AGPL 3.0 License
 */
@Zen.Module
object ItemAnimations : Feature("itemanimations") {
    private val itemanimations by ConfigDelegate<Boolean>("itemanimations")
    private val scaleswing by ConfigDelegate<Boolean>("itemswingscale")
    private val drinkingtype by ConfigDelegate<Int>("itemdrinkingtype")
    private val itemSize by ConfigDelegate<Double>("itemsize")
    private val itemX by ConfigDelegate<Double>("itemx")
    private val itemY by ConfigDelegate<Double>("itemy")
    private val itemZ by ConfigDelegate<Double>("itemz")
    private val itemPitch by ConfigDelegate<Double>("itempitch")
    private val itemYaw by ConfigDelegate<Double>("itemyaw")
    private val itemRoll by ConfigDelegate<Double>("itemroll")
    val cancelReEquip by ConfigDelegate<Boolean>("itemcancelrequip")
    val showReEquipWhenSlotsChange by ConfigDelegate<Boolean>("itemshowrequipwhenslotschanged")
    val ignoreHaste by ConfigDelegate<Boolean>("itemignorehaste")
    val swingSpeed by ConfigDelegate<Double>("itemswingspeed")

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Visuals", "Item Animations", ConfigElement(
                "itemanimations",
                "Enable item animations",
                ElementType.Switch(false)
            ), isSectionToggle = true)
            .addElement("Visuals", "Item Animations", "Size", ConfigElement(
                "itemsize",
                "Item size multiplier",
                ElementType.Slider(-2.0, 2.0, 0.0, true)
            ))
            .addElement("Visuals", "Item Animations", "Size", ConfigElement(
                "itemx",
                "Item X position",
                ElementType.Slider(-2.0, 2.0, 0.0, true)
            ))
            .addElement("Visuals", "Item Animations", "Size", ConfigElement(
                "itemy",
                "Item Y position",
                ElementType.Slider(-2.0, 2.0, 0.0, true)
            ))
            .addElement("Visuals", "Item Animations", "Size", ConfigElement(
                "itemz",
                "Item Z position",
                ElementType.Slider(-2.0, 2.0, 0.0, true)
            ))
            .addElement("Visuals", "Item Animations", "Rotation", ConfigElement(
                "itempitch",
                "Item pitch rotation",
                ElementType.Slider(-180.0, 180.0, 0.0, true)
            ))
            .addElement("Visuals", "Item Animations", "Rotation", ConfigElement(
                "itemyaw",
                "Item yaw rotation",
                ElementType.Slider(-180.0, 180.0, 0.0, true)
            ))
            .addElement("Visuals", "Item Animations", "Rotation", ConfigElement(
                "itemroll",
                "Item roll rotation",
                ElementType.Slider(-180.0, 180.0, 0.0, true)
            ))
            .addElement("Visuals", "Item Animations", "Swing", ConfigElement(
                "itemswingscale",
                "Scale swing animation",
                ElementType.Switch(false)
            ))
            .addElement("Visuals", "Item Animations", "Swing", ConfigElement(
                "itemswingspeed",
                "Swing speed multiplier",
                ElementType.Slider(-2.0, 1.0, 0.0, true)
            ))
            .addElement("Visuals", "Item Animations", "Drinking", ConfigElement(
                "itemdrinkingtype",
                "Drinking animation type",
                ElementType.Dropdown(listOf("Default", "Rotationless", "Scaled"), 0)
            ))
            .addElement("Visuals", "Item Animations", "Options", ConfigElement(
                "itemcancelrequip",
                "Cancel item re-equip animation",
                ElementType.Switch(false)
            ))
            .addElement("Visuals", "Item Animations", "Options", ConfigElement(
                "itemshowrequipwhenslotschanged",
                "Show re-equip when slots change",
                ElementType.Switch(true)
            ))
            .addElement("Visuals", "Item Animations", "Options", ConfigElement(
                "itemignorehaste",
                "Ignore haste effect",
                ElementType.Switch(false)
            ))
    }

    private fun performMatrixOperations(progress: Float) {
        val translations = doubleArrayOf(
            0.56 * (1.0 + itemX),
            -0.52 * (1.0 - itemY),
            -0.71999997 * (1.0 + itemZ)
        )
        GlStateManager.translate(translations[0].toFloat(), translations[1].toFloat(), translations[2].toFloat())
        GlStateManager.translate(0f, progress * -0.6f, 0f)
    }

    private fun applyItemRotations() {
        GlStateManager.rotate(itemPitch.toFloat(), 1f, 0f, 0f)
        GlStateManager.rotate(itemYaw.toFloat(), 0f, 1f, 0f)
        GlStateManager.rotate(itemRoll.toFloat(), 0f, 0f, 1f)
        GlStateManager.rotate(45f, 0f, 1f, 0f)
    }

    private fun calculateSwingRotations(swingProgress: Float) {
        val theta = swingProgress * swingProgress * PI.toFloat()
        val phi = MathHelper.sqrt_float(swingProgress) * PI.toFloat()

        GlStateManager.rotate(MathHelper.sin(theta) * -20f, 0f, 1f, 0f)
        GlStateManager.rotate(MathHelper.sin(phi) * -20f, 0f, 0f, 1f)
        GlStateManager.rotate(MathHelper.sin(phi) * -80f, 1f, 0f, 0f)
    }

    private fun applyScaling() {
        val scaleValue = (0.4f * exp(itemSize)).toFloat()
        GlStateManager.scale(scaleValue, scaleValue, scaleValue)
    }

    fun itemTransforHook(equipProgress: Float, swingProgress: Float): Boolean {
        return if (itemanimations) {
            performMatrixOperations(equipProgress)
            applyItemRotations()
            calculateSwingRotations(swingProgress)
            applyScaling()
            true
        } else false
    }

    fun scaledSwing(swingProgress: Float): Boolean {
        return if (itemanimations && scaleswing) {
            val multiplier = exp(itemSize).toFloat()
            val sqrtValue = MathHelper.sqrt_float(swingProgress)
            val piValue = PI.toFloat()

            GlStateManager.translate(
                -0.4f * MathHelper.sin(sqrtValue * piValue) * multiplier,
                0.2f * MathHelper.sin(sqrtValue * piValue * 2f) * multiplier,
                -0.2f * MathHelper.sin(swingProgress * piValue) * multiplier
            )
            true
        } else false
    }

    fun rotationlessDrink(clientPlayer: AbstractClientPlayer, partialTicks: Float): Boolean {
        return if (itemanimations && drinkingtype == 1) {
            val count = clientPlayer.itemInUseCount.toFloat() - partialTicks + 1f
            val duration = mc.thePlayer.heldItem.maxItemUseDuration.toFloat()
            val ratio = count / duration

            val amplitude = if (ratio < 0.8f) MathHelper.abs(MathHelper.cos(count * 0.25f * 3.1415927f) * 0.1f) else 0f

            GlStateManager.translate(0f, amplitude, 0f)
            true
        } else false
    }

    fun scaledDrinking(clientPlayer: AbstractClientPlayer, partialTicks: Float, itemToRender: ItemStack): Boolean {
        return if (itemanimations && drinkingtype == 2) {
            val remaining = clientPlayer.itemInUseCount.toFloat() - partialTicks + 1f
            val maxDuration = itemToRender.maxItemUseDuration.toFloat()
            val progress = remaining / maxDuration

            val oscillation = if (progress < 0.8f) MathHelper.abs(MathHelper.cos(remaining * 0.25f * PI.toFloat()) * 0.1f) else 0f

            val centerX = (0.56 * (1.0 + itemX)).toFloat()
            val centerY = (-0.52 * (1.0 - itemY)).toFloat()
            val centerZ = (-0.71999997 * (1.0 + itemZ)).toFloat()

            GlStateManager.translate(-0.56f, 0.52f, 0.71999997f)
            GlStateManager.translate(centerX, centerY, centerZ)
            GlStateManager.translate(0f, oscillation, 0f)

            val strength = 1f - progress.toDouble().pow(27.0).toFloat()
            GlStateManager.translate(strength * 0.6f, strength * -0.5f, 0f)

            GlStateManager.rotate(strength * 90f, 0f, 1f, 0f)
            GlStateManager.rotate(strength * 10f, 1f, 0f, 0f)
            GlStateManager.rotate(strength * 30f, 0f, 0f, 1f)

            GlStateManager.translate(0.56f, -0.52f, -0.71999997f)
            GlStateManager.translate(-centerX, -centerY, -centerZ)
            true
        } else false
    }
}