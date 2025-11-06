package com.pulse.datapacktools.client.screen.vexel

import com.pulse.datapacktools.client.packets.ClientPackets
import net.minecraft.client.MinecraftClient
import org.lwjgl.glfw.GLFW
import xyz.meowing.knit.api.input.KnitKeyboard
import xyz.meowing.knit.api.input.KnitKeys
import xyz.meowing.vexel.Vexel
import xyz.meowing.vexel.components.base.Pos
import xyz.meowing.vexel.components.base.Size
import xyz.meowing.vexel.components.base.VexelElement
import xyz.meowing.vexel.components.core.Container
import xyz.meowing.vexel.components.core.Rectangle
import xyz.meowing.vexel.components.core.Text
import xyz.meowing.vexel.utils.render.NVGRenderer
import kotlin.math.max

class CodeEditor(
    var filePath: String,
    initialValue: String = "",
    var fontSize: Float = 16f,
    var lineHeight: Float = 20f,
    val selectionColor: Int = 0x80aac7ff.toInt(),
    var textColor: Int = 0xFFFFFFFF.toInt(),
    backgroundColor: Int = 0x80404040.toInt(),
    borderColor: Int = 0xFF606060.toInt(),
    borderRadius: Float = 0f,
    borderThickness: Float = 0f,
    padding: FloatArray = floatArrayOf(8f, 8f, 8f, 8f),
    var lineNumberTextColor: Int = 0xFF808080.toInt(),
    var lineNumberBgColor: Int = 0x80303030.toInt(),
    var commentColor: Int = 0xFF1ABA4F.toInt(),
    widthType: Size = Size.Auto,
    heightType: Size = Size.Auto
) : VexelElement<CodeEditor>(widthType, heightType) {

    data class EditorState(
        val lines: List<String>,
        val cursorLine: Int,
        val cursorCol: Int,
        val selectionAnchorLine: Int,
        val selectionAnchorCol: Int
    )

    private val undoStack = mutableListOf<EditorState>()
    private val redoStack = mutableListOf<EditorState>()
    private val maxHistorySize = 100

    private val commandSuggester = CommandSuggester(
        mc = MinecraftClient.getInstance(),
        maxSuggestions = 10,
        fontSize = fontSize
    ).childOf(this)

    var value = initialValue
        set(newVal) {
            if (field == newVal) return
            field = newVal
            lines = field.split('\n').toMutableList()
            if (lines.isEmpty()) lines.add("")
            cursorLine = cursorLine.coerceIn(0, lines.size - 1)
            cursorCol = cursorCol.coerceIn(0, lines[cursorLine].length)
            selectionAnchorLine = selectionAnchorLine.coerceIn(0, lines.size - 1)
            selectionAnchorCol = selectionAnchorCol.coerceIn(0, lines[selectionAnchorLine].length)
            syncComponents()
            onValueChange?.invoke(field)
        }

    private var lines = value.split('\n').toMutableList()
    private var isDragging = false
    private var caretVisible = true
    private var lastBlink = System.currentTimeMillis()
    private val caretBlinkRate = 500L

    private var cursorLine = 0
    private var cursorCol = 0
    private var selectionAnchorLine = 0
    private var selectionAnchorCol = 0

    private val hasSelection: Boolean
        get() = !(cursorLine == selectionAnchorLine && cursorCol == selectionAnchorCol)

    private var scrollOffsetX = -1f
    private var scrollOffsetY = 0f

    private var lastClickTime = 0L
    private var clickCount = 0
    var isLoaded = false
    var hasUnsavedChanges = false
    
    var onUnsavedChanges: ((Boolean) -> Unit)? = null

    private val lineNumberWidth: Float = 50f

    var active = true

    private val bg = Rectangle(
        backgroundColor,
        borderColor,
        borderRadius,
        borderThickness,
        padding
    )
        .setSizing(100f, Size.ParentPerc, 100f, Size.ParentPerc)
        .ignoreMouseEvents()
        .ignoreFocus()
        .childOf(this)

    private val background = Container()
        .setPositioning(Pos.ParentPixels, Pos.ParentPixels)
        .setSizing(Size.ParentPerc, Size.ParentPerc)
        .scrollable(true)
        .ignoreFocus()
        .childOf(bg)

    private val lineNumberBg = Rectangle(lineNumberBgColor, 0x00000000, 0f, 0f)
        .setPositioning(Pos.ParentPixels, Pos.ParentPixels)
        .setSizing(Size.Pixels, Size.ParentPerc)
        .ignoreMouseEvents()
        .ignoreFocus()
        .childOf(background)

    private val lineNumberTexts = mutableMapOf<Int, Text>()
    private val lineTexts = mutableMapOf<Int, Text>()
    private val selectionRectangles = mutableMapOf<Int, Rectangle>()

    private val caret = Rectangle(0xFFFFFFFF.toInt(), 0xFF000000.toInt(), 1f, 0f)
        .setPositioning(Pos.ParentPixels, Pos.ParentPixels)
        .setSizing(Size.Pixels, Size.Pixels)
        .ignoreMouseEvents()
        .ignoreFocus()
        .childOf(background)

    init {
        setPositioning(Pos.ParentPixels, Pos.ParentPixels)
        setRequiresFocus()

        if (lines.isEmpty()) lines.add("")
        syncComponents()
        saveState()

        loadFile()

        background.mouseClickListeners.add { _, _, _ -> false }
        background.mouseReleaseListeners.add { _, _, _ -> false }
        background.mouseEnterListeners.add { _, _ -> false }
        background.mouseExitListeners.add { _, _ -> false }

        commandSuggester.apply {
            getCurrentText = { lines[cursorLine] }
            getCursorPosition = { cursorCol }
            onSuggestionApplied = { suggestion ->
                saveState()
                lines[cursorLine] = suggestion
                cursorCol = suggestion.length
                selectionAnchorLine = cursorLine
                selectionAnchorCol = cursorCol
                updateValue()
                syncComponents()
                resetCaretBlink()
            }
        }

        onClick { mouseX, mouseY, button ->
            if (button != 0 || !active) return@onClick false

            val clickedOnField = mouseX in x..(x + width) && mouseY in y..(y + height)

            if (clickedOnField) {
                isFocused = true
                isDragging = true

                val clickRelX = mouseX - (x + lineNumberWidth - scrollOffsetX)
                val clickRelY = mouseY - y + scrollOffsetY

                val clickedLine = (clickRelY / lineHeight).toInt().coerceIn(0, lines.size - 1)
                val clickedCol = getCharIndexAtX(lines[clickedLine], clickRelX)

                val currentTime = System.currentTimeMillis()
                if (currentTime - lastClickTime < 250) clickCount++
                else clickCount = 1

                lastClickTime = currentTime

				when (clickCount) {
                    1 -> {
                        cursorLine = clickedLine
                        cursorCol = clickedCol
                        if (!KnitKeyboard.isShiftKeyPressed) {
                            selectionAnchorLine = cursorLine
                            selectionAnchorCol = cursorCol
                        }
                    }

                    2 -> selectWordAt(clickedLine, clickedCol)

                    else -> {
                        selectAll()
                        clickCount = 0
                    }
                }

                resetCaretBlink()
				commandSuggester.refresh(lines[cursorLine], cursorCol)
                return@onClick true
            } else {
                isFocused = false
                commandSuggester.hideCmp()
                isDragging = false
                return@onClick false
            }
        }

        onCharType { keyCode, scanCode, char ->
            if (!active) return@onCharType false
            val keyHandled = keyCode != GLFW.GLFW_KEY_UNKNOWN && keyTyped(keyCode, scanCode, char)
            val charHandled = char != '\u0000' && keyCode == GLFW.GLFW_KEY_UNKNOWN && charTyped(char)

            if (keyHandled || charHandled) return@onCharType true
            false
        }

    }

    private fun saveState() {
        val state = EditorState(
            lines.toList(),
            cursorLine,
            cursorCol,
            selectionAnchorLine,
            selectionAnchorCol
        )
        undoStack.add(state)
        if (undoStack.size > maxHistorySize) {
            undoStack.removeAt(0)
        }
        redoStack.clear()
    }

    private fun restoreState(state: EditorState) {
        lines = state.lines.toMutableList()
        cursorLine = state.cursorLine
        cursorCol = state.cursorCol
        selectionAnchorLine = state.selectionAnchorLine
        selectionAnchorCol = state.selectionAnchorCol

        value = lines.joinToString("\n")
        syncComponents()
        resetCaretBlink()
        commandSuggester.refresh(lines[cursorLine], cursorCol)
    }

    private fun undo() {
        if (undoStack.size > 1) {
            val currentState = undoStack.removeAt(undoStack.size - 1)
            redoStack.add(currentState)

            if (redoStack.size > maxHistorySize) {
                redoStack.removeAt(0)
            }

            restoreState(undoStack.last())
        }
    }

    private fun redo() {
        if (redoStack.isNotEmpty()) {
            val state = redoStack.removeAt(redoStack.size - 1)
            undoStack.add(state)

            if (undoStack.size > maxHistorySize) {
                undoStack.removeAt(0)
            }

            restoreState(state)
        }
    }

    private fun syncComponents() {
        lineNumberTexts.keys.filter { it !in lines.indices }.forEach {
            lineNumberTexts[it]?.destroy()
            lineNumberTexts.remove(it)
        }

        // line numbers update
        lines.indices.forEach { value ->
            val textComponent = lineNumberTexts.getOrPut(value) {
                Text((value + 1).toString(), lineNumberTextColor, fontSize)
                    .setPositioning(Pos.ParentPixels, Pos.ParentPixels)
                    .ignoreMouseEvents()
                    .ignoreFocus()
                    .childOf(background)
            }
            textComponent.text = (value + 1).toString()
        }

        lineTexts.keys.filter { it >= lines.size }.forEach {
            lineTexts[it]?.destroy()
            lineTexts.remove(it)
        }

        // text update
        lines.indices.forEach { value ->
            val lineText = lines[value].ifEmpty { " " }
            val isComment = lineText.trimStart().startsWith("#")
            val color = if (isComment) commentColor else textColor

            if (lineTexts.containsKey(value)) {
                lineTexts[value]?.text = lineText
                lineTexts[value]?.textColor = color
            } else {
                val lnText = Text(lineText, color, fontSize)
                    .setPositioning(Pos.ParentPixels, Pos.ParentPixels)
                    .ignoreMouseEvents()
                    .ignoreFocus()
                    .childOf(background)
                lineTexts[value] = lnText
            }
        }

        // remove sel rectangles
        selectionRectangles.keys.filter { it >= lines.size }.forEach {
            selectionRectangles[it]?.destroy()
            selectionRectangles.remove(it)
        }

        // update sel rectangles
        lines.indices.forEach { value ->
            if (!selectionRectangles.containsKey(value)) {
                val selRect = Rectangle(selectionColor, 0x00000000, 0f, 0f)
                    .setPositioning(Pos.ParentPixels, Pos.ParentPixels)
                    .setSizing(Size.Pixels, Size.Pixels)
                    .ignoreMouseEvents()
                    .ignoreFocus()
                    .childOf(background)
                selRect.visible = false
                selectionRectangles[value] = selRect
            }
        }
    }

    override fun onRender(mouseX: Float, mouseY: Float) {
        background.isHovered = hovered
        background.isPressed = pressed

        lineNumberBg.width = lineNumberWidth
        lineNumberBg.height = height

        for ((i, lineNumText) in lineNumberTexts.toList()) {
            val lineY = (i * lineHeight) - scrollOffsetY
            val lineNumStr = (i + 1).toString()
            val lineX = lineNumberWidth - 10f - NVGRenderer.textWidth(lineNumStr, fontSize, NVGRenderer.defaultFont)

            lineNumText.setPositioning(lineX, Pos.ParentPixels, lineY, Pos.ParentPixels)
            lineNumText.visible = lineY >= -lineHeight && lineY < height
        }

        //render selection
        if (hasSelection) {
            updateSelectionRectangles()
        } else {
            selectionRectangles.values.forEach { it.visible = false }
        }

        for ((i, lineText) in lineTexts.toList()) {
            val lineY = (i * lineHeight) - scrollOffsetY
            val lineX = lineNumberWidth - scrollOffsetX

            lineText.setPositioning(lineX, Pos.ParentPixels, lineY, Pos.ParentPixels)
            lineText.visible = lineY >= -lineHeight && lineY < height
        }

        // render caret
        if (focused) {
            val caretX = lineNumberWidth - scrollOffsetX +
                    NVGRenderer.textWidth(lines[cursorLine].substring(0, cursorCol), fontSize, NVGRenderer.defaultFont)
            val caretY = (cursorLine * lineHeight) - scrollOffsetY

            caret.setPositioning(caretX, Pos.ParentPixels, caretY, Pos.ParentPixels)
            caret.setSizing(1f, Size.Pixels, lineHeight, Size.Pixels)
            caret.visible = caretVisible

            if (System.currentTimeMillis() - lastBlink > caretBlinkRate) {
                caretVisible = !caretVisible
                lastBlink = System.currentTimeMillis()
            }
        } else {
            caret.visible = false
        }

        if (focused && commandSuggester.visible) {
            val suggesterX = lineNumberWidth - scrollOffsetX +
                    NVGRenderer.textWidth(lines[cursorLine].substring(0, cursorCol), fontSize, NVGRenderer.defaultFont)
            val suggesterY = (cursorLine * lineHeight) - scrollOffsetY + lineHeight

            commandSuggester.position(suggesterX, suggesterY)
        }

        ensureCaretVisible()
    }

    private fun updateSelectionRectangles() {
        val (startLine, startCol) = if (cursorLine < selectionAnchorLine ||
            (cursorLine == selectionAnchorLine && cursorCol < selectionAnchorCol)) {
            Pair(cursorLine, cursorCol)
        } else {
            Pair(selectionAnchorLine, selectionAnchorCol)
        }

        val (endLine, endCol) = if (cursorLine > selectionAnchorLine ||
            (cursorLine == selectionAnchorLine && cursorCol > selectionAnchorCol)) {
            Pair(cursorLine, cursorCol)
        } else {
            Pair(selectionAnchorLine, selectionAnchorCol)
        }

        for ((i, selRect) in selectionRectangles) {
            if (i < startLine || i > endLine) {
                selRect.visible = false
                continue
            }

            val lineText = lines[i]
            val selStart = if (i == startLine) startCol else 0
            val selEnd = if (i == endLine) endCol else lineText.length

            if (selStart < selEnd) {
                val x1 = lineNumberWidth - scrollOffsetX +
                        NVGRenderer.textWidth(lineText.substring(0, selStart), fontSize, NVGRenderer.defaultFont)
                val x2 = lineNumberWidth - scrollOffsetX +
                        NVGRenderer.textWidth(lineText.substring(0, selEnd), fontSize, NVGRenderer.defaultFont)
                val y1 = (i * lineHeight) - scrollOffsetY

                selRect.setPositioning(x1, Pos.ParentPixels, y1, Pos.ParentPixels)
                selRect.setSizing(x2 - x1, Size.Pixels, lineHeight, Size.Pixels)
                selRect.visible = true
            } else {
                selRect.visible = false
            }
        }
    }

    fun keyTyped(keyCode: Int, scanCode: Int, char: Char): Boolean {
        if (!isFocused) return false

        val ctrlDown = KnitKeyboard.isCtrlKeyPressed
        val shiftDown = KnitKeyboard.isShiftKeyPressed

        if (commandSuggester.visible && commandSuggester.handleKey(keyCode, scanCode, 0)) {
            return true
        }

            when (keyCode) {
            KnitKeys.KEY_ESCAPE.code -> {
                isFocused = false
                return true
            }
            KnitKeys.KEY_ENTER.code -> {
                saveState()
                insertText("\n")
                    commandSuggester.refresh(lines[cursorLine], cursorCol)
                return true
            }
            KnitKeys.KEY_TAB.code -> {
                saveState()
                insertText("    ")
                    commandSuggester.refresh(lines[cursorLine], cursorCol)
                return true
            }
            KnitKeys.KEY_BACKSPACE.code -> {
                saveState()
                if (ctrlDown) deletePrevWord()
                else deleteChar()
                    commandSuggester.refresh(lines[cursorLine], cursorCol)
                return true
            }
            KnitKeys.KEY_LEFT.code -> {
                if (ctrlDown) moveWord(-1, shiftDown)
                else moveCaret(-1, 0, shiftDown)
                    commandSuggester.refresh(lines[cursorLine], cursorCol)
                return true
            }
            KnitKeys.KEY_RIGHT.code -> {
                if (ctrlDown) moveWord(1, shiftDown)
                else moveCaret(1, 0, shiftDown)
                    commandSuggester.refresh(lines[cursorLine], cursorCol)
                return true
            }
            KnitKeys.KEY_UP.code -> {
                moveCaret(0, -1, shiftDown)
                    commandSuggester.refresh(lines[cursorLine], cursorCol)
                return true
            }
            KnitKeys.KEY_DOWN.code -> {
                moveCaret(0, 1, shiftDown)
                    commandSuggester.refresh(lines[cursorLine], cursorCol)
                return true
            }
            KnitKeys.KEY_HOME.code -> {
                if (ctrlDown) moveCaretTo(0, 0, shiftDown)
                else moveCaretTo(cursorLine, 0, shiftDown)
                    commandSuggester.refresh(lines[cursorLine], cursorCol)
                return true
            }
            KnitKeys.KEY_END.code -> {
                if (ctrlDown) moveCaretTo(lines.size - 1, lines.last().length, shiftDown)
                else moveCaretTo(cursorLine, lines[cursorLine].length, shiftDown)
                    commandSuggester.refresh(lines[cursorLine], cursorCol)
                return true
            }
        }

        if (ctrlDown) {
            when (keyCode) {
                KnitKeys.KEY_A.code -> {
                    selectAll()
                    return true
                }
                KnitKeys.KEY_C.code -> {
                    copySelection()
                    return true
                }
                KnitKeys.KEY_V.code -> {
                    saveState()
                    paste()
                    return true
                }
                KnitKeys.KEY_X.code -> {
                    saveState()
                    cutSelection()
                    return true
                }
                KnitKeys.KEY_Y.code -> {
                    redo()
                    return true
                }
                KnitKeys.KEY_Z.code -> {
                    if (shiftDown) {
                        redo()
                    } else {
                        undo()
                    }
                    return true
                }
                KnitKeys.KEY_S.code -> {
                    saveFile()
                    return true
                }
            }
            return false
        }

        if (char.code >= 32) {
            saveState()
            insertText(char.toString())
            commandSuggester.refresh(lines[cursorLine], cursorCol)
            return true
        }

        return false
    }

    fun charTyped(chr: Char): Boolean {
        if (!isFocused || chr.code < 32 || chr == 127.toChar()) return false
        saveState()
        insertText(chr.toString())
        commandSuggester.refresh(lines[cursorLine], cursorCol)
        return true
    }

    private fun resetCaretBlink() {
        lastBlink = System.currentTimeMillis()
        caretVisible = true
    }

    private fun getCharIndexAtX(line: String, clickX: Float): Int {
        if (clickX <= 0) return 0
        var currentWidth = 0f
        line.indices.forEach { value ->
            val charWidth = NVGRenderer.textWidth(line[value].toString(), fontSize, NVGRenderer.defaultFont)
            if (clickX < currentWidth + charWidth / 2) {
                return value
            }
            currentWidth += charWidth
        }
        return line.length
    }

    private fun selectWordAt(line: Int, col: Int) {
        val lineText = lines[line]
        if (lineText.isEmpty()) return

        val pos = col.coerceIn(0, lineText.length)

        if (pos < lineText.length && !lineText[pos].isWhitespace()) {
            var start = pos
            while (start > 0 && !lineText[start - 1].isWhitespace()) start--
            var end = pos
            while (end < lineText.length && !lineText[end].isWhitespace()) end++

            cursorLine = line
            cursorCol = end
            selectionAnchorLine = line
            selectionAnchorCol = start
        }
    }

    private fun insertText(text: String) {
        if (hasSelection) {
            deleteSelection()
        }

        val insertLines = text.split('\n')
        val currentLine = lines[cursorLine]

        if (insertLines.size == 1) {
            lines[cursorLine] = currentLine.substring(0, cursorCol) + text + currentLine.substring(cursorCol)
            cursorCol += text.length
        } else {
            val before = currentLine.substring(0, cursorCol)
            val after = currentLine.substring(cursorCol)

            lines[cursorLine] = before + insertLines[0]

            for (i in 1 until insertLines.size - 1) {
                lines.add(cursorLine + i, insertLines[i])
            }

            cursorLine += insertLines.size - 1
            cursorCol = insertLines.last().length
            lines.add(cursorLine, insertLines.last() + after)
        }

        selectionAnchorLine = cursorLine
        selectionAnchorCol = cursorCol
        updateValue()
        syncComponents()
        resetCaretBlink()
    }

    private fun deleteChar() {
        if (hasSelection) {
            deleteSelection()
            return
        }

        if (cursorCol > 0) {
            val line = lines[cursorLine]
            lines[cursorLine] = line.substring(0, cursorCol - 1) + line.substring(cursorCol)
            cursorCol--
        } else if (cursorLine > 0) {
            val currentLine = lines[cursorLine]
            lines.removeAt(cursorLine)
            cursorLine--
            cursorCol = lines[cursorLine].length
            lines[cursorLine] += currentLine
        }

        selectionAnchorLine = cursorLine
        selectionAnchorCol = cursorCol
        updateValue()
        syncComponents()
        resetCaretBlink()
    }

    private fun deleteSelection() {
        val (startLine, startCol) = if (cursorLine < selectionAnchorLine ||
            (cursorLine == selectionAnchorLine && cursorCol < selectionAnchorCol)) {
            Pair(cursorLine, cursorCol)
        } else {
            Pair(selectionAnchorLine, selectionAnchorCol)
        }

        val (endLine, endCol) = if (cursorLine > selectionAnchorLine ||
            (cursorLine == selectionAnchorLine && cursorCol > selectionAnchorCol)) {
            Pair(cursorLine, cursorCol)
        } else {
            Pair(selectionAnchorLine, selectionAnchorCol)
        }

        if (startLine == endLine) {
            val line = lines[startLine]
            lines[startLine] = line.substring(0, startCol) + line.substring(endCol)
        } else {
            val firstPart = lines[startLine].substring(0, startCol)
            val lastPart = lines[endLine].substring(endCol)

            for (i in endLine downTo startLine + 1) {
                lines.removeAt(i)
            }

            lines[startLine] = firstPart + lastPart
        }

        cursorLine = startLine
        cursorCol = startCol
        selectionAnchorLine = cursorLine
        selectionAnchorCol = cursorCol

        selectionRectangles.values.forEach { it.visible = false }

        updateValue()
        syncComponents()
    }

    private fun moveCaret(deltaCol: Int, deltaLine: Int, shiftHeld: Boolean) {
        if (deltaLine != 0) {
            cursorLine = (cursorLine + deltaLine).coerceIn(0, lines.size - 1)
            cursorCol = cursorCol.coerceIn(0, lines[cursorLine].length)
        } else {
            cursorCol += deltaCol
            while (cursorCol < 0 && cursorLine > 0) {
                cursorLine--
                cursorCol = lines[cursorLine].length
            }
            while (cursorCol > lines[cursorLine].length && cursorLine < lines.size - 1) {
                cursorCol -= lines[cursorLine].length + 1
                cursorLine++
            }
            cursorCol = cursorCol.coerceIn(0, lines[cursorLine].length)
        }

        if (!shiftHeld) {
            selectionAnchorLine = cursorLine
            selectionAnchorCol = cursorCol
        }
        resetCaretBlink()
        
        commandSuggester.refresh(lines[cursorLine], cursorCol)
    }

    private fun moveCaretTo(line: Int, col: Int, shiftHeld: Boolean) {
        cursorLine = line.coerceIn(0, lines.size - 1)
        cursorCol = col.coerceIn(0, lines[cursorLine].length)

        if (!shiftHeld) {
            selectionAnchorLine = cursorLine
            selectionAnchorCol = cursorCol
        }
        resetCaretBlink()

        commandSuggester.refresh(lines[cursorLine], cursorCol)
    }

    private fun moveWord(direction: Int, shiftHeld: Boolean) {
        val boundary = findWordBoundary(cursorLine, cursorCol, direction)
        cursorLine = boundary.first
        cursorCol = boundary.second

        if (!shiftHeld) {
            selectionAnchorLine = cursorLine
            selectionAnchorCol = cursorCol
        }
        resetCaretBlink()

        commandSuggester.refresh(lines[cursorLine], cursorCol)
    }

    private fun findWordBoundary(line: Int, col: Int, direction: Int): Pair<Int, Int> {
        var currentLine = line
        var currentCol = col

        if (direction < 0) {
            if (currentCol > 0) currentCol--
            while (currentCol > 0 && lines[currentLine][currentCol].isWhitespace()) currentCol--
            while (currentCol > 0 && !lines[currentLine][currentCol - 1].isWhitespace()) currentCol--
        } else {
            while (currentCol < lines[currentLine].length && !lines[currentLine][currentCol].isWhitespace()) currentCol++
            while (currentCol < lines[currentLine].length && lines[currentLine][currentCol].isWhitespace()) currentCol++

            if (currentCol >= lines[currentLine].length && currentLine < lines.size - 1) {
                currentLine++
                currentCol = 0
            }
        }

        return Pair(currentLine, currentCol.coerceIn(0, lines[currentLine].length))
    }

    private fun deletePrevWord() {
        if (hasSelection) {
            deleteSelection()
            return
        }

        val oldLine = cursorLine
        val oldCol = cursorCol
        val boundary = findWordBoundary(cursorLine, cursorCol, -1)

        selectionAnchorLine = oldLine
        selectionAnchorCol = oldCol
        cursorLine = boundary.first
        cursorCol = boundary.second

        deleteSelection()

        commandSuggester.refresh(lines[cursorLine], cursorCol)
    }

    private fun deleteNextWord() {
        if (hasSelection) {
            deleteSelection()
            return
        }

        val oldLine = cursorLine
        val oldCol = cursorCol
        val boundary = findWordBoundary(cursorLine, cursorCol, 1)

        selectionAnchorLine = oldLine
        selectionAnchorCol = oldCol
        cursorLine = boundary.first
        cursorCol = boundary.second

        deleteSelection()

        commandSuggester.refresh(lines[cursorLine], cursorCol)
    }

    private fun selectAll() {
        selectionAnchorLine = 0
        selectionAnchorCol = 0
        cursorLine = lines.size - 1
        cursorCol = lines.last().length
        resetCaretBlink()
    }

    private fun getSelectedText(): String {
        if (!hasSelection) return ""

        val (startLine, startCol) = if (cursorLine < selectionAnchorLine ||
            (cursorLine == selectionAnchorLine && cursorCol < selectionAnchorCol)) {
            Pair(cursorLine, cursorCol)
        } else {
            Pair(selectionAnchorLine, selectionAnchorCol)
        }

        val (endLine, endCol) = if (cursorLine > selectionAnchorLine ||
            (cursorLine == selectionAnchorLine && cursorCol > selectionAnchorCol)) {
            Pair(cursorLine, cursorCol)
        } else {
            Pair(selectionAnchorLine, selectionAnchorCol)
        }

        if (startLine == endLine) {
            return lines[startLine].substring(startCol, endCol)
        }

        val result = StringBuilder()
        result.append(lines[startLine].substring(startCol))

        for (i in startLine + 1 until endLine) {
            result.append('\n').append(lines[i])
        }

        result.append('\n').append(lines[endLine].substring(0, endCol))
        return result.toString()
    }

    private fun copySelection() {
        if (!hasSelection) return
        Vexel.mc.keyboard.clipboard = getSelectedText()
    }

    private fun cutSelection() {
        if (!hasSelection) return
        copySelection()
        deleteSelection()
    }

    private fun paste() {
        val clipboardText = Vexel.mc.keyboard.clipboard
        if (clipboardText.isNotEmpty()) {
            insertText(clipboardText)
        }
    }

    private fun ensureCaretVisible() {
        val caretX = NVGRenderer.textWidth(lines[cursorLine].substring(0, cursorCol), fontSize, NVGRenderer.defaultFont)
        val caretY = cursorLine * lineHeight

        val visibleWidth = width - lineNumberWidth
        val visibleHeight = height

        if (caretX < scrollOffsetX) {
            scrollOffsetX = caretX
        } else if (caretX > scrollOffsetX + visibleWidth - 10) {
            scrollOffsetX = caretX - visibleWidth + 10
        }

        if (caretY < scrollOffsetY) {
            scrollOffsetY = caretY
        } else if (caretY + lineHeight > scrollOffsetY + visibleHeight) {
            scrollOffsetY = caretY + lineHeight - visibleHeight
        }

        scrollOffsetX = max(0f, scrollOffsetX)
        scrollOffsetY = max(0f, scrollOffsetY)
    }

    private fun updateValue() {
        val newValue = lines.joinToString("\n")
        if (value != newValue) {
            value = newValue
            if (isLoaded) {
                hasUnsavedChanges = true
                onUnsavedChanges?.invoke(true)
            }
        }
    }

    override fun getAutoWidth(): Float = background.getAutoWidth()
    override fun getAutoHeight(): Float = background.getAutoHeight()

    fun padding(top: Float, right: Float, bottom: Float, left: Float): CodeEditor = apply {
        background.padding(top, right, bottom, left)
    }

    fun padding(all: Float): CodeEditor = apply {
        background.padding(all)
    }

    fun fontSize(size: Float): CodeEditor = apply {
        this.fontSize = size
        lineTexts.values.forEach { it.fontSize = size }
        lineNumberTexts.values.forEach { it.fontSize = size }
    }

    fun lineHeight(height: Float): CodeEditor = apply {
        this.lineHeight = height
    }

    fun backgroundColor(color: Int): CodeEditor = apply {
        bg.backgroundColor(color)
    }

    fun borderColor(color: Int): CodeEditor = apply {
        bg.borderColor(color)
    }

    fun borderRadius(radius: Float): CodeEditor = apply {
        bg.borderRadius(radius)
    }

    fun borderThickness(thickness: Float): CodeEditor = apply {
        bg.borderThickness(thickness)
    }

    fun lineNumberColor(color: Int): CodeEditor = apply {
        this.lineNumberTextColor = color
        lineNumberTexts.values.forEach { it.textColor = color }
    }

    fun commentColor(color: Int): CodeEditor = apply {
        this.commentColor = color
        syncComponents()
    }

    fun loadFile() {
        if (filePath.isNotEmpty()) ClientPackets.sendGetFunctionPacket(filePath)
    }

    fun saveFile() {
        if (filePath.isNotEmpty() && isLoaded) {
            ClientPackets.sendSaveFunctionPacket(filePath, value)
            hasUnsavedChanges = false
            onUnsavedChanges?.invoke(false)
        }
    }

}