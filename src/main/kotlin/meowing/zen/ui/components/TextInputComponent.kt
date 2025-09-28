package meowing.zen.ui.components

import meowing.zen.utils.FontUtils
import meowing.zen.utils.Render2D
import meowing.zen.utils.Render2D.drawRect
import meowing.zen.utils.Render2D.drawRoundedRect
import meowing.zen.utils.StencilUtils
import net.minecraft.client.gui.GuiScreen
import net.minecraft.util.ChatAllowedCharacters
import org.lwjgl.input.Keyboard
import java.awt.Color
import kotlin.math.max
import kotlin.math.min

/**
 * Modified version of the TextField class from NoammAddons
 * @author Noamm9
 */
class TextInputComponent(val placeholder: String = "", var x: Number, var y: Number, var width: Number, var height: Number, val radius: Number, val accentColor: Color, val hoverColor: Color) {
    var value = ""
        set(newVal) {
            if (field == newVal) return
            field = newVal
            cursorIndex = cursorIndex.coerceIn(0, field.length)
            selectionAnchor = selectionAnchor.coerceIn(0, field.length)
        }

    private val fontObj = FontUtils.getFontRenderer()
    private val padding = 6.0

    private val textRenderAreaWidth get() = fieldWidth - (textPadding * 2)
    private val fieldWidth get() = width.toDouble() - (padding * 2)
    val textPadding = 4.0

    var focused = false
    private var isDragging = false
    private var caretVisible = true
    private var isHovered = false

    private var lastBlink = System.currentTimeMillis()
    private val caretBlinkRate = 500L

    private var cursorIndex = value.length
    private var selectionAnchor = value.length

    private val selectionStart: Int get() = min(cursorIndex, selectionAnchor)
    private val selectionEnd: Int get() = max(cursorIndex, selectionAnchor)
    private val hasSelection: Boolean get() = selectionStart != selectionEnd

    var scrollOffset = 0.0
    private var lastClickTime = 0L
    private var clickCount = 0

    private var lastKeyProcessed = false

    fun draw(mx: Number, my: Number) {
        val x = x.toDouble()
        val y = y.toDouble()
        val height = height.toDouble()
        val mouseX = mx.toDouble()
        val mouseY = my.toDouble()

        val fieldRectX = x
        val fieldRectY = y
        isHovered = mouseX in fieldRectX..(fieldRectX + fieldWidth) && mouseY in fieldRectY..(fieldRectY + height)

        val borderColor = when {
            focused -> accentColor
            isHovered -> hoverColor
            else -> Color(60, 60, 60)
        }

        drawRoundedRect(borderColor, x - 1, y - 1, fieldWidth + 2, height + 2, radius)
        drawRoundedRect(Color(20, 20, 20), x, y, fieldWidth, height, radius)

        StencilUtils.beginStencilClip {
            drawRect(Color.WHITE, x, y, fieldWidth, height)
        }

        val shouldShowPlaceholder = value.isEmpty() && !focused
        val textToRender = if (shouldShowPlaceholder) placeholder else value
        val textColor = if (shouldShowPlaceholder) Color(120, 120, 120) else Color.WHITE
        val textY = y + (height - fontObj.FONT_HEIGHT) / 2

        if (hasSelection && !shouldShowPlaceholder) {
            val selStartStr = value.substring(0, selectionStart)
            val selEndStr = value.substring(0, selectionEnd)
            val x1 = x + textPadding - scrollOffset + fontObj.getStringWidth(selStartStr)
            val x2 = x + textPadding - scrollOffset + fontObj.getStringWidth(selEndStr)
            val selectionHeight = fontObj.FONT_HEIGHT.toDouble()
            drawRect(accentColor, x1, textY, x2 - x1, selectionHeight)
        }

        Render2D.renderString(textToRender, (x + textPadding - scrollOffset).toFloat(), textY.toFloat(), 1f, textColor.rgb)

        if (focused && caretVisible && !shouldShowPlaceholder) {
            val textBeforeCaret = value.take(cursorIndex)
            val caretXPos = x + textPadding - scrollOffset + fontObj.getStringWidth(textBeforeCaret)
            if (caretXPos >= x + textPadding - 1 && caretXPos <= x + textPadding + textRenderAreaWidth) {
                drawRect(Color.WHITE, caretXPos, textY, 1.0, fontObj.FONT_HEIGHT)
            }
        }

        StencilUtils.endStencilClip()

        if (System.currentTimeMillis() - lastBlink > caretBlinkRate) {
            caretVisible = !caretVisible
            lastBlink = System.currentTimeMillis()
        }
    }

    fun mouseClicked(mouseX: Double, mouseY: Double, button: Int) {
        if (button != 0) return

        val fieldRectX = x.toDouble()
        val fieldRectY = y.toDouble()
        val fieldHeight = height.toDouble()

        val clickedOnField = mouseX in fieldRectX..(fieldRectX + fieldWidth) &&
                mouseY in fieldRectY..(fieldRectY + fieldHeight)

        if (clickedOnField) {
            focused = true
            isDragging = true

            val clickRelX = mouseX - (fieldRectX + textPadding - scrollOffset)
            val newCursorIndex = getCharIndexAtAbsX(clickRelX)

            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime < 250) clickCount++
            else clickCount = 1

            lastClickTime = currentTime

            when (clickCount) {
                1 -> {
                    cursorIndex = newCursorIndex
                    if (!GuiScreen.isShiftKeyDown()) {
                        selectionAnchor = cursorIndex
                    }
                }
                2 -> selectWordAt(newCursorIndex)
                else -> {
                    selectAll()
                    clickCount = 0
                }
            }
            resetCaretBlink()
        } else {
            focused = false
            isDragging = false
        }
    }

    fun mouseRelease(button: Int) {
        if (button == 0) isDragging = false
    }

    fun mouseDragged(x: Double, mouseX: Double, button: Int) {
        if (focused && isDragging && button == 0) {
            val fieldRectX = x + padding
            val clickRelX = mouseX - (fieldRectX + textPadding - scrollOffset)
            cursorIndex = getCharIndexAtAbsX(clickRelX)
            ensureCaretVisible()
            resetCaretBlink()
        }
    }

    fun keyTyped(typedChar: Char, keyCode: Int): Boolean {
        if (!focused) return false

        val isKeyPressed = Keyboard.getEventKeyState()
        if (!isKeyPressed && lastKeyProcessed) {
            lastKeyProcessed = false
            return false
        }
        if (isKeyPressed) lastKeyProcessed = true

        val ctrlDown = GuiScreen.isCtrlKeyDown()
        val shiftDown = GuiScreen.isShiftKeyDown()

        when (keyCode) {
            Keyboard.KEY_ESCAPE -> {
                focused = false
                return true
            }
            Keyboard.KEY_RETURN -> {
                focused = false
                return true
            }
            Keyboard.KEY_BACK -> {
                if (ctrlDown) deletePrevWord()
                else deleteChar(-1)
                return true
            }
            Keyboard.KEY_DELETE -> {
                if (ctrlDown) deleteNextWord()
                else deleteChar(1)
                return true
            }
            Keyboard.KEY_LEFT -> {
                if (ctrlDown) moveWord(-1, shiftDown)
                else moveCaret(-1, shiftDown)
                return true
            }
            Keyboard.KEY_RIGHT -> {
                if (ctrlDown) moveWord(1, shiftDown)
                else moveCaret(1, shiftDown)
                return true
            }
            Keyboard.KEY_HOME -> {
                moveCaretTo(0, shiftDown)
                return true
            }
            Keyboard.KEY_END -> {
                moveCaretTo(value.length, shiftDown)
                return true
            }
            Keyboard.KEY_A -> {
                if (ctrlDown) {
                    selectAll()
                    return true
                }
            }
            Keyboard.KEY_C -> {
                if (ctrlDown) {
                    copySelection()
                    return true
                }
            }
            Keyboard.KEY_V -> {
                if (ctrlDown) {
                    paste()
                    return true
                }
            }
            Keyboard.KEY_X -> {
                if (ctrlDown) {
                    cutSelection()
                    return true
                }
            }
            Keyboard.KEY_F -> {
                if (ctrlDown) {
                    focused = true
                    return true
                }
            }
        }

        if (ChatAllowedCharacters.isAllowedCharacter(typedChar)) {
            insertText(typedChar.toString())
            return true
        }
        return false
    }

    private fun resetCaretBlink() {
        lastBlink = System.currentTimeMillis()
        caretVisible = true
    }

    private fun getCharIndexAtAbsX(absClickX: Double): Int {
        if (absClickX <= 0) return 0
        var currentWidth = 0.0
        for (i in value.indices) {
            val charWidth = fontObj.getStringWidth(value[i].toString())
            if (absClickX < currentWidth + charWidth / 2.0) {
                return i
            }
            currentWidth += charWidth
        }
        return value.length
    }

    private fun selectWordAt(pos: Int) {
        if (value.isEmpty()) return
        val currentPos = pos.coerceIn(0, value.length)

        if (currentPos < value.length && !Character.isWhitespace(value[currentPos])) {
            var start = currentPos
            while (start > 0 && !Character.isWhitespace(value[start - 1])) {
                start--
            }
            var end = currentPos
            while (end < value.length && !Character.isWhitespace(value[end])) {
                end++
            }
            cursorIndex = end
            selectionAnchor = start
        } else {
            cursorIndex = currentPos
            selectionAnchor = currentPos
        }
        ensureCaretVisible()
    }

    private fun insertText(text: String) {
        val builder = StringBuilder(value)
        val textToInsert = ChatAllowedCharacters.filterAllowedCharacters(text)

        val newCursorPos = if (!hasSelection) cursorIndex
        else {
            val currentSelectionStart = selectionStart
            builder.delete(currentSelectionStart, selectionEnd)
            currentSelectionStart
        }

        builder.insert(newCursorPos, textToInsert)
        this.value = builder.toString()
        cursorIndex = (newCursorPos + textToInsert.length).coerceIn(0, this.value.length)
        selectionAnchor = cursorIndex

        ensureCaretVisible()
        resetCaretBlink()
    }

    private fun deleteChar(direction: Int) {
        var textChanged = false
        var newText = value
        var newCursor = cursorIndex

        if (hasSelection) {
            val builder = StringBuilder(value)
            val selStart = selectionStart
            builder.delete(selStart, selectionEnd)
            newText = builder.toString()
            newCursor = selStart
            textChanged = true
        } else {
            if (direction == -1 && cursorIndex > 0) {
                val originalCursor = cursorIndex
                val builder = StringBuilder(value)
                builder.deleteCharAt(originalCursor - 1)
                newText = builder.toString()
                newCursor = originalCursor - 1
                textChanged = true
            } else if (direction == 1 && cursorIndex < value.length) {
                val builder = StringBuilder(value)
                builder.deleteCharAt(cursorIndex)
                newText = builder.toString()
                textChanged = true
            }
        }

        if (!textChanged) resetCaretBlink()
        else {
            this.value = newText
            cursorIndex = newCursor.coerceIn(0, this.value.length)
            selectionAnchor = cursorIndex

            val maxScroll = max(0.0, fontObj.getStringWidth(this.value) - textRenderAreaWidth)
            if (scrollOffset > maxScroll) {
                scrollOffset = maxScroll
            }

            ensureCaretVisible()
            resetCaretBlink()
        }
    }

    private fun moveCaret(amount: Int, shiftHeld: Boolean) {
        cursorIndex = (cursorIndex + amount).coerceIn(0, value.length)
        if (!shiftHeld) {
            selectionAnchor = cursorIndex
        }
        ensureCaretVisible()
        resetCaretBlink()
    }

    private fun moveCaretTo(position: Int, shiftHeld: Boolean) {
        cursorIndex = position.coerceIn(0, value.length)
        if (!shiftHeld) {
            selectionAnchor = cursorIndex
        }
        ensureCaretVisible()
        resetCaretBlink()
    }

    private fun moveWord(direction: Int, shiftHeld: Boolean) {
        cursorIndex = findWordBoundary(cursorIndex, direction)
        if (!shiftHeld) {
            selectionAnchor = cursorIndex
        }
        ensureCaretVisible()
        resetCaretBlink()
    }

    private fun findWordBoundary(startIndex: Int, direction: Int): Int {
        var i = startIndex
        val len = value.length
        if (direction < 0) {
            if (i > 0) i--
            while (i > 0 && Character.isWhitespace(value[i])) i--
            while (i > 0 && !Character.isWhitespace(value[i - 1])) i--
        } else {
            while (i < len && !Character.isWhitespace(value[i])) i++
            while (i < len && Character.isWhitespace(value[i])) i++
        }
        return i.coerceIn(0, len)
    }

    private fun deletePrevWord() {
        if (hasSelection) {
            deleteChar(0)
            return
        }
        if (cursorIndex == 0) return
        val oldCursor = cursorIndex
        cursorIndex = findWordBoundary(cursorIndex, -1)
        selectionAnchor = oldCursor
        deleteChar(0)
    }

    private fun deleteNextWord() {
        if (hasSelection) {
            deleteChar(0)
            return
        }
        if (cursorIndex == value.length) return
        val oldCursor = cursorIndex
        cursorIndex = findWordBoundary(cursorIndex, 1)
        selectionAnchor = oldCursor
        deleteChar(0)
    }

    private fun selectAll() {
        selectionAnchor = 0
        cursorIndex = value.length
        resetCaretBlink()
    }

    private fun getSelectedText(): String {
        return if (hasSelection) value.substring(selectionStart, selectionEnd) else ""
    }

    private fun copySelection() {
        if (!hasSelection) return
        GuiScreen.setClipboardString(getSelectedText())
    }

    private fun cutSelection() {
        if (!hasSelection) return
        copySelection()
        deleteChar(0)
    }

    private fun paste() = GuiScreen.getClipboardString()?.run(::insertText)

    private fun ensureCaretVisible() {
        val caretXAbsolute = fontObj.getStringWidth(value.substring(0, cursorIndex.coerceIn(0, value.length))).toDouble()
        val visibleTextStart = scrollOffset
        val visibleTextEnd = scrollOffset + textRenderAreaWidth

        if (caretXAbsolute < visibleTextStart) {
            scrollOffset = caretXAbsolute
        } else if (caretXAbsolute > visibleTextEnd - 1) {
            scrollOffset = caretXAbsolute - textRenderAreaWidth + 1
        }

        val maxScrollPossible = max(0.0, fontObj.getStringWidth(value) - textRenderAreaWidth)
        scrollOffset = scrollOffset.coerceIn(0.0, maxScrollPossible)
        if (fontObj.getStringWidth(value) <= textRenderAreaWidth) {
            scrollOffset = 0.0
        }
    }
}