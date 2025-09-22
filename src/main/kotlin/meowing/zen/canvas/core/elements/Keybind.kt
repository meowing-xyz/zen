package meowing.zen.canvas.core.elements

import meowing.zen.canvas.core.CanvasElement
import meowing.zen.canvas.core.Pos
import meowing.zen.canvas.core.Size
import meowing.zen.canvas.core.components.Rectangle
import meowing.zen.canvas.core.components.Text
import org.lwjgl.input.Keyboard

class Keybind(
    backgroundColor: Int = 0x80404040.toInt(),
    borderColor: Int = 0xFF606060.toInt(),
    borderRadius: Float = 4f,
    borderThickness: Float = 1f,
    padding: FloatArray = floatArrayOf(12f, 24f, 12f, 24f),
    hoverColor: Int? = 0x80505050.toInt(),
    pressedColor: Int? = 0x80303030.toInt(),
    widthType: Size = Size.Auto,
    heightType: Size = Size.Auto
) : CanvasElement<Keybind>(widthType, heightType) {
    var selectedKeyId: Int? = null
    var selectedScanId: Int? = null
    var listen: Boolean = false

    private val background = Rectangle(backgroundColor, borderColor, borderRadius, borderThickness, padding, hoverColor, pressedColor, Size.ParentPerc, Size.ParentPerc)
        .setSizing(100f, Size.ParentPerc, 100f, Size.ParentPerc)
        .ignoreMouseEvents()
        .childOf(this)

    private val innerText = Text("Key A", 0xFFFFFFFF.toInt(), 12f)
        .setPositioning(Pos.ParentCenter, Pos.ParentCenter)
        .childOf(background)

    init {
        setSizing(100f, Size.Pixels, 0f, Size.Auto)
        setPositioning(Pos.ParentPixels, Pos.ParentPixels)
        ignoreFocus()

        onClick { _, _, _ ->
            listenForKeybind()
            true
        }

        onCharType { keyCode, scanCode, char ->
            if (!listen) return@onCharType false

            if (keyCode == 256) {
                innerText.text = "None"
                selectedKeyId = null
                selectedScanId = null
            } else {
                innerText.text = getKeyName(keyCode)
                selectedKeyId = keyCode
                selectedScanId = scanCode
            }

            onValueChange?.invoke(keyCode)
            listen = false
            true
        }
    }

    fun listenForKeybind() {
        innerText.text = "Press a key.."
        listen = true
    }

    override fun onRender(mouseX: Float, mouseY: Float) {
        background.isHovered = hovered
        background.isPressed = pressed
    }

    private fun getKeyName(keyCode: Int): String = when (keyCode) {
        Keyboard.KEY_LSHIFT -> "LShift"
        Keyboard.KEY_RSHIFT -> "RShift"
        Keyboard.KEY_LCONTROL -> "LCtrl"
        Keyboard.KEY_RCONTROL -> "RCtrl"
        Keyboard.KEY_LMENU -> "LAlt"
        Keyboard.KEY_RMENU -> "RAlt"
        Keyboard.KEY_RETURN -> "Enter"
        Keyboard.KEY_NONE -> "None"
        Keyboard.KEY_SPACE -> "Space"
        in Keyboard.KEY_F1..Keyboard.KEY_F15 -> "F${keyCode - Keyboard.KEY_F1 + 1}"
        else -> "Key " + (Keyboard.getKeyName(keyCode)?.uppercase() ?: "$keyCode")
    }

    override fun getAutoWidth(): Float = background.getAutoWidth()
    override fun getAutoHeight(): Float = background.getAutoHeight()

    fun padding(top: Float, right: Float, bottom: Float, left: Float): Keybind = apply {
        background.padding(top, right, bottom, left)
    }

    fun padding(all: Float): Keybind = apply {
        background.padding(all)
    }

    fun backgroundColor(color: Int): Keybind = apply {
        background.backgroundColor(color)
    }

    fun borderColor(color: Int): Keybind = apply {
        background.borderColor(color)
    }

    fun borderRadius(radius: Float): Keybind = apply {
        background.borderRadius(radius)
    }

    fun borderThickness(thickness: Float): Keybind = apply {
        background.borderThickness(thickness)
    }

    fun hoverColor(color: Int): Keybind = apply {
        background.hoverColor(color)
    }

    fun pressedColor(color: Int): Keybind = apply {
        background.pressedColor(color)
    }
}