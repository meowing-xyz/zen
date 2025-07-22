package meowing.zen.feats.general

import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.UIComponent
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.ScrollComponent
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.UIText
import gg.essential.elementa.components.input.UITextInput
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.dsl.pixels
import gg.essential.universal.UKeyboard
import meowing.zen.Zen
import meowing.zen.Zen.Companion.mc
import meowing.zen.Zen.Companion.prefix
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.constraint.ChildHeightConstraint
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.ChatEvent
import meowing.zen.events.GuiEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.CommandUtils
import meowing.zen.utils.DataUtils
import meowing.zen.utils.TickUtils
import meowing.zen.utils.Utils.removeFormatting
import meowing.zen.utils.Utils.getChatLine
import net.minecraft.client.gui.GuiChat
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.command.ICommandSender
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import java.awt.Color
import java.util.regex.Pattern

data class Patterns(val patterns: MutableList<String> = mutableListOf())

@Zen.Module
object ChatCleaner : Feature("chatcleaner") {
    private val compiledPatterns = mutableListOf<Pattern>()
    val patterns get() = dataUtils.getData().patterns
    val dataUtils = DataUtils("chatcleaner", Patterns())

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("General", "Chat Cleaner", ConfigElement(
                "chatcleaner",
                "Chat cleaner",
                "Removes a TON of useless messages from your chat.",
                ElementType.Switch(false)
            ))
            .addElement("General", "Chat Cleaner", ConfigElement(
                "chatcleanerkey",
                "Keybind",
                "Key to add the hovered message to the filter.",
                ElementType.Keybind(Keyboard.KEY_H)
            ))
    }

    init {
        loadDefault()
        compilePatterns()
    }

    override fun initialize() {
        register<ChatEvent.Receive> { event ->
            val message = event.event.message.unformattedText.removeFormatting()
            compiledPatterns.forEach { pattern ->
                if (pattern.matcher(message).find()) event.cancel()
            }
        }

        register<GuiEvent.Key> { event ->
            if (event.screen !is GuiChat || !Keyboard.isKeyDown(config.chatcleanerkey)) return@register
            val chat = mc.ingameGUI.chatGUI

            val scaledResolution = ScaledResolution(mc)
            val mouseX = Mouse.getX() * scaledResolution.scaledWidth / mc.displayWidth
            val mouseY = scaledResolution.scaledHeight - Mouse.getY() * scaledResolution.scaledHeight / mc.displayHeight - 1

            val text = chat.getChatLine(mouseX, mouseY)?.chatComponent?.unformattedText?.removeFormatting() ?: return@register
            if (text.isNotEmpty()) {
                event.cancel()
                addPattern(text)
                ChatUtils.addMessage("$prefix §fAdded §7\"§c$text§7\" §fto filter.")
            }
        }
    }

    fun loadDefault() {
        if (patterns.isEmpty()) {
            try {
                javaClass.getResourceAsStream("/assets/zen/chatfilter.json")?.use { stream ->
                    val defaultPatterns = com.google.gson.Gson().fromJson(
                        stream.bufferedReader().readText(), Array<String>::class.java
                    )
                    patterns.addAll(defaultPatterns.toList())
                    dataUtils.save()
                }
            } catch (e: Exception) {
                println("[Zen] Caught error while trying to load defaults in ChatCleaner: $e")
            }
        }
    }

    fun compilePatterns() {
        compiledPatterns.clear()
        patterns.forEach { pattern ->
            try {
                compiledPatterns.add(Pattern.compile(pattern))
            } catch (_: Exception) {
                ChatUtils.addMessage("§e[Zen] §fInvalid regex pattern §7[§c$pattern§7]")
            }
        }
    }

    fun addPattern(pattern: String): Boolean {
        if (pattern.isBlank() || patterns.contains(pattern)) return false
        return try {
            Pattern.compile(pattern)
            patterns.add(pattern)
            compilePatterns()
            true
        } catch (_: Exception) {
            false
        }
    }

    fun removePattern(index: Int): Boolean {
        if (index < 0) return false
        patterns.removeAt(index)
        compilePatterns()
        return true
    }

    fun updatePattern(index: Int, newPattern: String): Boolean {
        if (index < 0 || index >= patterns.size || newPattern.isBlank() ||
            (patterns.contains(newPattern) && patterns[index] != newPattern)) return false
        return try {
            Pattern.compile(newPattern)
            patterns[index] = newPattern
            compilePatterns()
            true
        } catch (_: Exception) {
            false
        }
    }
}

@Zen.Command
object ChatCleanerCommand : CommandUtils("chatcleaner", aliases = listOf("zencc", "zenchatcleaner")) {
    override fun processCommand(sender: ICommandSender, args: Array<String>) {
        TickUtils.schedule(2) { mc.displayGuiScreen(ChatCleanerGui()) }
    }
}

class ChatCleanerGui : WindowScreen(ElementaVersion.V10) {
    private val patterns = ChatCleaner.patterns
    private val colors = object {
        val bg = Color(8, 12, 16)
        val panel = Color(12, 16, 20)
        val panelHover = Color(22, 26, 30)
        val btn = Color(15, 20, 25)
        val btnHover = Color(40, 80, 90)
        val btnText = Color(100, 245, 255)
        val accent = Color(100, 245, 255)
        val inputText = Color(170, 230, 240)
        val closeNormal = Color(25, 30, 35)
        val closeHover = Color(45, 55, 65)
        val rowBg = Color(18, 22, 26)
        val placeholder = Color(70, 120, 140)
    }

    private val scrollComponent = ScrollComponent()
    private val listContainer = UIContainer()
    private val inputField = UITextInput("Enter regex pattern...").apply {
        setColor(Color(170, 230, 240))
        setTextScale(0.9f.pixels())
    }

    init {
        buildGui()
    }

    override fun onScreenClose() {
        super.onScreenClose()
        ChatCleaner.dataUtils.save()
    }

    private fun buildGui() {
        val mainContainer = UIRoundedRectangle(6f).apply {
            setX(CenterConstraint())
            setY(CenterConstraint())
            setWidth(70.percent())
            setHeight(80.percent())
            setColor(colors.bg)
        } childOf window

        UIText("Chat Cleaner").apply {
            setX(1.5.percent())
            setY(3.percent())
            setColor(colors.accent)
            setTextScale(1.35f.pixels())
        } childOf mainContainer

        createButton("✕", colors.closeNormal, colors.closeHover) {
            mc.displayGuiScreen(null)
        }.apply {
            setX(95.5.percent())
            setY(1.5.percent())
            setWidth(3.percent())
            setHeight(5.percent())
        } childOf mainContainer

        scrollComponent.apply {
            setX(2.percent())
            setY(8.percent())
            setWidth(96.percent())
            setHeight(79.percent())
        } childOf mainContainer

        listContainer.apply {
            setWidth(100.percent())
            setHeight(ChildHeightConstraint(5f))
        } childOf scrollComponent

        val inputContainer = UIRoundedRectangle(6f).apply {
            setX(1.5.percent())
            setY(88.percent())
            setWidth(97.percent())
            setHeight(9.percent())
            setColor(colors.panel)
        } childOf mainContainer

        inputField.apply {
            setX(1.5f.percent())
            setY(CenterConstraint())
            setWidth(95.percent())
            onMouseClick { inputField.grabWindowFocus() }
        } childOf inputContainer

        createButton("+", colors.btn, colors.btnHover) { addPattern() }.apply {
            setX(93.percent())
            setY(CenterConstraint())
            setWidth(5.percent())
            setHeight(60.percent())
        } childOf inputContainer

        renderPatterns()
    }

    private fun createButton(text: String, normalColor: Color, hoverColor: Color, onClick: () -> Unit): UIComponent {
        val button = UIRoundedRectangle(6f).apply {
            setColor(normalColor)
            onMouseEnter { setColor(hoverColor) }
            onMouseLeave { setColor(normalColor) }
            onMouseClick { onClick() }
        }

        UIText(text).apply {
            setX(CenterConstraint())
            setY(CenterConstraint())
            setColor(colors.btnText)
            setTextScale(1.1f.pixels())
        } childOf button

        return button
    }

    private fun renderPatterns() {
        listContainer.clearChildren()

        if (patterns.isEmpty()) {
            UIText("No patterns added...").apply {
                setX(3.percent())
                setY(20.pixels())
                setColor(colors.placeholder)
                setTextScale(0.95f.pixels())
            } childOf listContainer
            return
        }

        patterns.forEachIndexed { index, pattern ->
            val row = UIRoundedRectangle(6f).apply {
                setX(0.percent())
                setY((index * 40 + 8).pixels())
                setWidth(100.percent())
                setHeight(35.pixels())
                setColor(colors.rowBg)
                onMouseEnter { setColor(colors.panelHover) }
                onMouseLeave { setColor(colors.rowBg) }
            } childOf listContainer

            UITextInput(pattern).apply {
                setX(2.percent())
                setY(CenterConstraint())
                setWidth(80.percent())
                setColor(colors.inputText)
                setTextScale(0.9f.pixels())
                onKeyType { _, keyCode ->
                    if (keyCode == UKeyboard.KEY_ENTER && getText() != pattern) {
                        updatePattern(index, getText())
                    }
                }
                onFocusLost {
                    if (getText() != pattern) updatePattern(index, getText())
                }
                onMouseClick {
                    setText(pattern)
                    grabWindowFocus()
                }
            } childOf row

            createButton("⿻", colors.btn, colors.btnHover) { copyPattern(index) }.apply {
                setX(87.percent())
                setY(CenterConstraint())
                setWidth(5.percent())
                setHeight(60.percent())
            } childOf row

            createButton("✕", colors.btn, colors.btnHover) { removePattern(index) }.apply {
                setX(93.percent())
                setY(CenterConstraint())
                setWidth(5.percent())
                setHeight(60.percent())
            } childOf row
        }
    }

    private fun addPattern() {
        val pattern = inputField.getText().trim()
        if (pattern.isEmpty() || pattern == "Enter regex pattern...") {
            ChatUtils.addMessage("§cEnter a pattern!")
            return
        }

        if (ChatCleaner.addPattern(pattern)) {
            inputField.setText("")
            renderPatterns()
        }
    }

    private fun copyPattern(index: Int) {
        setClipboardString(ChatCleaner.patterns[index])
    }

    private fun updatePattern(index: Int, newPattern: String) {
        if (ChatCleaner.updatePattern(index, newPattern)) renderPatterns()
    }

    private fun removePattern(index: Int) {
        if (ChatCleaner.removePattern(index)) renderPatterns()
    }
}