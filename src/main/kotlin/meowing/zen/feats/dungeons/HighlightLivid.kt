package meowing.zen.feats.dungeons

import meowing.zen.Zen
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.*
import meowing.zen.feats.Feature
import meowing.zen.utils.*
import meowing.zen.utils.Utils.removeFormatting
import net.minecraft.block.BlockStainedGlass
import net.minecraft.block.state.IBlockState
import net.minecraft.init.Blocks
import net.minecraft.item.EnumDyeColor
import net.minecraft.util.BlockPos
import net.minecraft.entity.Entity
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

    private val renderLividCall: EventBus.EventCall = EventBus.register<RenderEvent.EntityModel>({ event ->
        if (lividEntity == event.entity) {
            OutlineUtils.outlineEntity(event, config.highlightlividcolor, config.highlightlividwidth)
        }
    }, false)

    private val renderLineCall: EventBus.EventCall = EventBus.register<RenderEvent.World>({ event ->
        lividEntity?.let { entity ->
            if (mc.thePlayer.canEntityBeSeen(entity)) {
                Render3D.drawLineToEntity(
                    entity,
                    config.highlightlividwidth,
                    config.highlightlividcolor,
                    event.partialTicks
                )
            }
        }
    }, false)

    private val renderWrongCall: EventBus.EventCall = EventBus.register<RenderEvent.Player.Pre>({ event ->
        if (event.player != lividEntity && event.player.name.contains(" Livid")) {
            event.cancel()
        }
    }, false)

    private val tickCall: EventBus.EventCall = EventBus.register<TickEvent.Server>({
        val state: IBlockState = mc.theWorld.getBlockState(lividPos) ?: return@register
        if (state.block != Blocks.stained_glass) return@register

        val color = state.getValue(BlockStainedGlass.COLOR)
        val lividType = lividTypes[color] ?: return@register

        mc.theWorld.playerEntities.find { it.name.contains(lividType) }?.let {
            lividEntity = it
            registerRender()
            tickCall.unregister()
        }
    }, false)

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Dungeons", "Livid", ConfigElement(
                    "highlightlivid",
                    "Highlight correct livid",
                    "Highlights the correct livid.",
                    ElementType.Switch(false)
            ))
            .addElement("Dungeons", "Livid", ConfigElement(
                "highlightlividcolor",
                "Highlight correct livid color",
                "Color for the correct livid's outline",
                ElementType.ColorPicker(Color(0, 255, 255, 127)),
                { config -> config["highlightlivid"] as? Boolean == true }
            ))
            .addElement("Dungeons", "Livid", ConfigElement(
                "highlightlividwidth",
                "Highlight correct livid width",
                "Width for the correct livid's outline",
                ElementType.Slider(1.0, 5.0, 2.0, false),
                { config -> config["highlightlivid"] as? Boolean == true }
            ))
            .addElement("Dungeons", "Livid", ConfigElement(
                "hidewronglivid",
                "Hide incorrect livid entity",
                "Cancels the rendering of incorrect livid entities",
                ElementType.Switch(false),
                { config -> config["highlightlivid"] as? Boolean == true }
            ))
            .addElement("Dungeons", "Livid", ConfigElement(
                "highlightlividline",
                "Line to correct livid entity",
                "Renders a line to the correct livid entity",
                ElementType.Switch(false),
                { config -> config["highlightlivid"] as? Boolean == true }
            ))
    }

    override fun initialize() {
        register<ChatEvent.Receive> { event ->
            if (event.event.type.toInt() == 2) return@register
            if (event.event.message.unformattedText.removeFormatting() == "[BOSS] Livid: I respect you for making it to here, but I'll be your undoing.") {
                TickUtils.scheduleServer(80) {
                    tickCall.register()
                }
            }
        }

        register<WorldEvent.Change> {
            unregisterRender()
        }
    }

    private fun registerRender() {
        renderLividCall.register()
        if (config.hidewronglivid) renderWrongCall.register()
        if (config.highlightlividline) renderLineCall.register()
    }

    private fun unregisterRender() {
        renderLividCall.unregister()
        renderWrongCall.unregister()
        renderLineCall.unregister()
        lividEntity = null
    }
}