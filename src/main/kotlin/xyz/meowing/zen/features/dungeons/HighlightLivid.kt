package xyz.meowing.zen.features.dungeons

import xyz.meowing.zen.Zen
import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ui.ConfigUI
import xyz.meowing.zen.config.ui.types.ConfigElement
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.ChatEvent
import xyz.meowing.zen.events.RenderEvent
import xyz.meowing.zen.events.TickEvent
import xyz.meowing.zen.events.WorldEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.utils.OutlineUtils
import xyz.meowing.zen.utils.Render3D
import xyz.meowing.zen.utils.TickUtils
import xyz.meowing.zen.utils.Utils.removeFormatting
import net.minecraft.block.BlockStainedGlass
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.Entity
import net.minecraft.init.Blocks
import net.minecraft.item.EnumDyeColor
import net.minecraft.util.BlockPos
import java.awt.Color

@Zen.Module
object HighlightLivid : Feature("highlightlivid", area = "catacombs", subarea = listOf("F5", "M5")) {
    private var lividEntity: Entity? = null
    private val lividPos = BlockPos(5, 108, 42)
    private val lividTypes = mapOf(
        EnumDyeColor.WHITE to "Vendetta",
        EnumDyeColor.MAGENTA to "Crossed",
        EnumDyeColor.PINK to "Crossed",
        EnumDyeColor.RED to "Hockey",
        EnumDyeColor.SILVER to "Doctor",
        EnumDyeColor.GRAY to "Doctor",
        EnumDyeColor.GREEN to "Frog",
        EnumDyeColor.LIME to "Smile",
        EnumDyeColor.BLUE to "Scream",
        EnumDyeColor.PURPLE to "Purple",
        EnumDyeColor.YELLOW to "Arcade"
    )
    private val highlightlividline by ConfigDelegate<Boolean>("highlightlividline")
    private val hidewronglivid by ConfigDelegate<Boolean>("hidewronglivid")
    private val highlightlividwidth by ConfigDelegate<Double>("highlightlividwidth")
    private val highlightlividcolor by ConfigDelegate<Color>("highlightlividcolor")

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Dungeons", "Highlight Livid", ConfigElement(
                "highlightlivid",
                "Highlight correct livid",
                ElementType.Switch(false)
            ), isSectionToggle = true)
            .addElement("Dungeons", "Highlight Livid", "Color", ConfigElement(
                "highlightlividcolor",
                "Highlight correct livid color",
                ElementType.ColorPicker(Color(0, 255, 255, 127))
            ))
            .addElement("Dungeons", "Highlight Livid", "Width", ConfigElement(
                "highlightlividwidth",
                "Highlight correct livid width",
                ElementType.Slider(1.0, 5.0, 2.0, false)
            ))
            .addElement("Dungeons", "Highlight Livid", "Options", ConfigElement(
                "hidewronglivid",
                "Hide incorrect livid entity",
                ElementType.Switch(false)
            ))
            .addElement("Dungeons", "Highlight Livid", "Options", ConfigElement(
                "highlightlividline",
                "Line to correct livid entity",
                ElementType.Switch(false)
            ))
    }

    override fun initialize() {
        createCustomEvent<RenderEvent.EntityModel>("renderLivid") { event ->
            if (lividEntity == event.entity) {
                OutlineUtils.outlineEntity(event, highlightlividcolor, highlightlividwidth.toFloat())
            }
        }

        createCustomEvent<RenderEvent.World>("renderLine") { event ->
            lividEntity?.let { entity ->
                if (mc.thePlayer.canEntityBeSeen(entity)) {
                    Render3D.drawLineToEntity(
                        entity,
                        highlightlividwidth.toFloat(),
                        highlightlividcolor,
                        event.partialTicks
                    )
                }
            }
        }

        createCustomEvent<RenderEvent.Player.Pre>("renderWrong") { event ->
            if (event.player != lividEntity && event.player.name.contains(" Livid")) {
                event.cancel()
            }
        }

        createCustomEvent<TickEvent.Server>("tick") {
            val state: IBlockState = world?.getBlockState(lividPos) ?: return@createCustomEvent
            if (state.block != Blocks.stained_glass) return@createCustomEvent

            val color = state.getValue(BlockStainedGlass.COLOR)
            val lividType = lividTypes[color] ?: return@createCustomEvent

            world?.playerEntities?.find { it.name.contains(lividType) }?.let {
                lividEntity = it
                registerRender()
                unregisterEvent("tick")
            }
        }

        register<ChatEvent.Receive> { event ->
            if (event.event.type.toInt() == 2) return@register
            if (event.event.message.unformattedText.removeFormatting() == "[BOSS] Livid: I respect you for making it to here, but I'll be your undoing.") {
                TickUtils.scheduleServer(80) {
                    registerEvent("tick")
                }
            }
        }

        register<WorldEvent.Change> {
            unregisterRender()
        }
    }

    private fun registerRender() {
        registerEvent("renderLivid")
        if (hidewronglivid) registerEvent("renderWrong")
        if (highlightlividline) registerEvent("renderLine")
    }

    private fun unregisterRender() {
        unregisterEvent("renderLivid")
        unregisterEvent("renderWrong")
        unregisterEvent("renderLine")
        lividEntity = null
    }
}