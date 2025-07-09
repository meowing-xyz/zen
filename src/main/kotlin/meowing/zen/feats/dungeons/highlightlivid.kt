package meowing.zen.feats.dungeons

import meowing.zen.Zen.Companion.config
import meowing.zen.Zen.Companion.mc
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.ChatEvent
import meowing.zen.events.EventBus
import meowing.zen.events.RenderEvent
import meowing.zen.events.TickEvent
import meowing.zen.events.WorldEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.OutlineUtils
import meowing.zen.utils.TickUtils
import meowing.zen.utils.Utils.removeFormatting
import net.minecraft.block.BlockStainedGlass
import net.minecraft.block.state.IBlockState
import net.minecraft.init.Blocks
import net.minecraft.item.EnumDyeColor
import net.minecraft.util.BlockPos
import net.minecraft.entity.Entity
import java.awt.Color

object highlightlivid : Feature("highlightlivid", area = "catacombs", subarea = listOf("F5", "M5")) {
    private var lividEntity: Entity? = null;

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
    }

    override fun initialize() {
        val renderLividCall: EventBus.EventCall =
            EventBus.register<RenderEvent.EntityModel> ({ event ->
                if (lividEntity != null && lividEntity == event.entity) {
                    OutlineUtils.outlineEntity(event, config.highlightlividcolor, config.highlightlividwidth)
                }
            }, false)
        var tickCall: EventBus.EventCall? = null

        tickCall = EventBus.register<TickEvent.Server> ({
            val state: IBlockState = mc.theWorld.getBlockState(BlockPos(5, 108, 42)) ?: return@register
            if (state.block != Blocks.stained_glass) return@register
            val color = state.getValue(BlockStainedGlass.COLOR)
            val lividType = when (color) {
                EnumDyeColor.WHITE -> "Vendetta"
                EnumDyeColor.MAGENTA, EnumDyeColor.PINK -> "Crossed"
                EnumDyeColor.RED -> "Hockey"
                EnumDyeColor.SILVER, EnumDyeColor.GRAY -> "Doctor"
                EnumDyeColor.GREEN -> "Frog"
                EnumDyeColor.LIME -> "Smile"
                EnumDyeColor.BLUE -> "Scream"
                EnumDyeColor.PURPLE -> "Purple"
                EnumDyeColor.YELLOW -> "Arcade"
                else -> return@register
            }

            mc.theWorld.playerEntities.find { it.name.contains(lividType) }?.let {
                lividEntity = it
                renderLividCall.register()
                tickCall?.unregister()
            }
        }, false)

        register<ChatEvent.Receive> { event ->
            if (event.event.type.toInt() == 2) return@register
            if (event.event.message.unformattedText.removeFormatting() == "[BOSS] Livid: I respect you for making it to here, but I'll be your undoing.") {
                TickUtils.scheduleServer(80) {
                    tickCall.register()
                }
            }
        }

        register<WorldEvent.Change> {
            renderLividCall.unregister()
            tickCall.unregister()
        }
    }
}