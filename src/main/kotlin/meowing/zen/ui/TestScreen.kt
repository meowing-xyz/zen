package meowing.zen.ui

import meowing.zen.Zen
import meowing.zen.Zen.Companion.prefix
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.CommandUtils
import meowing.zen.utils.TickUtils
import meowing.zen.utils.gui.PositionConstraint
import meowing.zen.utils.gui.SizeConstraint
import meowing.zen.utils.gui.components.NanoRectangle
import meowing.zen.utils.gui.components.NanoText
import meowing.zen.utils.rendering.NVGRenderer
import net.minecraft.client.gui.GuiScreen
import net.minecraft.command.ICommandSender
import java.awt.Color

class NewConfigScreen : GuiScreen() {
    val mainBoundingRectangle = NanoRectangle(backgroundColor = Color(40,40,40, 200).rgb, borderColor = Color(0, 255, 255).rgb, borderThickness = 3f, borderRadius = 10f)
        .setSizing(70f, SizeConstraint.ScreenPercent, 70f, SizeConstraint.ScreenPercent)
        .setPositioning(PositionConstraint.ScreenCenter, PositionConstraint.ScreenCenter)

    val logo = NanoText("Zen", textColor = Color.CYAN.rgb, fontSize = 50f)
        .setPositioning(20f, PositionConstraint.ParentPixels, 20f, PositionConstraint.ParentPixels)
        .childOf(mainBoundingRectangle)

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        NVGRenderer.beginFrame(mc.displayWidth.toFloat(), mc.displayHeight.toFloat())
        NVGRenderer.push()
        mainBoundingRectangle.render(mouseX.toFloat(), mouseY.toFloat())
        NVGRenderer.pop()
        NVGRenderer.endFrame()
    }

    override fun doesGuiPauseGame(): Boolean {
        return false
    }
}

@Zen.Command
object TestScreen : CommandUtils(
    "testscreen",
    ""
) {
    override fun processCommand(sender: ICommandSender?, args: Array<out String?>?) {
        ChatUtils.addMessage("$prefix Opening test screen...")
        TickUtils.schedule(1) {
            Zen.mc.displayGuiScreen(NewConfigScreen())
        }
    }
}