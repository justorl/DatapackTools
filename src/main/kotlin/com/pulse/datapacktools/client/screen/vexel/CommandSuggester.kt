package com.pulse.datapacktools.client.screen.vexel

import com.mojang.brigadier.ParseResults
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.suggestion.Suggestion
import com.mojang.brigadier.suggestion.Suggestions
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientCommandSource
import net.minecraft.util.math.MathHelper
import org.lwjgl.glfw.GLFW
import xyz.meowing.vexel.components.base.Pos
import xyz.meowing.vexel.components.base.Size
import xyz.meowing.vexel.components.base.VexelElement
import xyz.meowing.vexel.components.core.Container
import xyz.meowing.vexel.components.core.Rectangle
import xyz.meowing.vexel.components.core.Text
import xyz.meowing.vexel.utils.render.NVGRenderer
import java.util.concurrent.CompletableFuture

class CommandSuggester(
    private val mc: MinecraftClient,
    private val maxSuggestions: Int = 10,
    private val suggestionBgColor: Int = 0xFF38434A.toInt(),
    private val suggestionTextColor: Int = 0xFFAAAAAA.toInt(),
    private val selectedTextColor: Int = 0xFFFFFF00.toInt(),
    private val fontSize: Float = 12f
) : VexelElement<CommandSuggester>(Size.Pixels, Size.Pixels) {

    private var pendingSuggestions: CompletableFuture<Suggestions>? = null
    private var parse: ParseResults<ClientCommandSource>? = null

    private var suggestions: List<Suggestion> = emptyList()
    private var selectedIndex = 0
    private var scrollOffset = 0
    private var completed = false

    private val container = Container()
        .setPositioning(Pos.ParentPixels, Pos.ParentPixels)
        .setSizing(Size.Pixels, Size.Pixels)
        .childOf(this)

    private val suggestionTexts = mutableListOf<Text>()
    private val suggestionBgs = mutableListOf<Rectangle>()

    var onSuggestionApplied: ((String) -> Unit)? = null
    var getCurrentText: (() -> String)? = null
    var getCursorPosition: (() -> Int)? = null

    init {
        setPositioning(Pos.ParentPixels, Pos.ParentPixels)
        setRequiresFocus()
        visible = false
    }

    fun refresh(inputText: String, cursorPos: Int) {
        if (inputText.isBlank()) { hideCmp(); return }

        if (parse?.reader?.string != inputText) parse = null

        val dispatcher = mc.player?.networkHandler?.commandDispatcher ?: return
        val reader = StringReader(inputText)

        if (parse == null) {
            parse = dispatcher.parse(reader, mc.player?.networkHandler?.commandSource)
        }

        if (cursorPos >= reader.cursor) {
            pendingSuggestions = dispatcher.getCompletionSuggestions(parse, cursorPos)
            pendingSuggestions?.thenRun {
                if (pendingSuggestions?.isDone == true) showSuggestions()
            }
        }
    }

    private fun showSuggestions() {
        val result = pendingSuggestions?.join() ?: return
        if (result.isEmpty) { visible = false; return }

        suggestions = sortSuggestions(result)
        selectedIndex = 0
        scrollOffset = 0
        visible = true
        createSuggestion()
    }

    private fun sortSuggestions(suggestions: Suggestions): List<Suggestion> {
        val text = getCurrentText?.invoke() ?: ""
        val cursor = getCursorPosition?.invoke() ?: 0
        val typedPart = text.substring(0, cursor).substring(getStartOfWord(text.substring(0, cursor))).lowercase()

        return suggestions.list.partition {
            it.text.startsWith(typedPart) || it.text.startsWith("minecraft:$typedPart")
        }.let { (matching, nonMatching) -> matching + nonMatching }
    }

    private fun getStartOfWord(input: String): Int {
        if (input.isEmpty()) return 0
        val lastSpace = input.lastIndexOf(' ')
        return if (lastSpace == -1) 0 else lastSpace + 1
    }

    private fun createSuggestion() {
        suggestionTexts.forEach { it.destroy() }
        suggestionBgs.forEach { it.destroy() }
        suggestionTexts.clear()
        suggestionBgs.clear()

        if (!visible || suggestions.isEmpty()) return

        val maxWidth = suggestions.maxOfOrNull {
            NVGRenderer.textWidth(it.text, fontSize, NVGRenderer.defaultFont)
        } ?: 0f
        val displayCount = suggestions.size.coerceAtMost(maxSuggestions)
        val itemHeight = fontSize + 4f
        val totalHeight = displayCount * itemHeight

        container.setSizing(maxWidth + 10f, Size.Pixels, totalHeight, Size.Pixels)
        width = maxWidth + 10f
        height = totalHeight

        repeat(displayCount) { i ->
            suggestionBgs.add(
                Rectangle(suggestionBgColor, 0x00000000, 0f, 0f)
                    .setPositioning(0f, Pos.ParentPixels, i * itemHeight, Pos.ParentPixels)
                    .setSizing(maxWidth + 10f, Size.Pixels, itemHeight, Size.Pixels)
                    .ignoreMouseEvents()
                    .ignoreFocus()
                    .childOf(container)
            )

            suggestionTexts.add(
                Text("", suggestionTextColor, fontSize)
                    .setPositioning(5f, Pos.ParentPixels, i * itemHeight + 2f, Pos.ParentPixels)
                    .ignoreMouseEvents()
                    .ignoreFocus()
                    .childOf(container)
            )
        }

        updateSuggestionDisplay()
    }

    private fun updateSuggestionDisplay() {
        repeat(suggestions.size.coerceAtMost(maxSuggestions)) { i ->
            val suggestionIndex = i + scrollOffset
            if (suggestionIndex >= suggestions.size) return@repeat

            val suggestion = suggestions[suggestionIndex]
            val isSelected = suggestionIndex == selectedIndex

            suggestionTexts[i].apply {
                text = suggestion.text
                textColor = if (isSelected) selectedTextColor else suggestionTextColor
            }
            suggestionBgs[i].backgroundColor(if (isSelected) 0xE0333333.toInt() else suggestionBgColor)
        }
    }

    fun handleKey(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (!visible || suggestions.isEmpty()) return false

        when (keyCode) {
            GLFW.GLFW_KEY_UP -> {
                scroll(-1)
                completed = false
                return true
            }
            GLFW.GLFW_KEY_DOWN -> {
                scroll(1)
                completed = false
                return true
            }
            GLFW.GLFW_KEY_TAB -> {
                makeSuggestion()
                return true
            }
            GLFW.GLFW_KEY_ESCAPE -> {
                hideCmp()
                return true
            }
        }
        return false
    }

    override fun handleMouseClick(mouseX: Float, mouseY: Float, button: Int): Boolean {
        if (!visible || button != 0) return false

        val relX = mouseX - x
        val relY = mouseY - y

        return if (relX in 0f..width && relY in 0f..height) {
            val clickedIndex = ((relY / (fontSize + 4f)).toInt() + scrollOffset)
            if (clickedIndex in suggestions.indices) {
                selectedIndex = clickedIndex
                makeSuggestion()
                true
            } else false
        } else false
    }

    override fun handleMouseScroll(mouseX: Float, mouseY: Float, horizontal: Double, vertical: Double): Boolean {
        if (!visible) return false

        val mouseX = mc.mouse.x * mc.window.scaledWidth / mc.window.width
        val mouseY = mc.mouse.y * mc.window.scaledHeight / mc.window.height
        val relX = mouseX - x
        val relY = mouseY - y

        return if (relX in 0f..width && relY in 0f..height) {
            scrollOffset = MathHelper.clamp(
                (scrollOffset - horizontal).toInt(),
                0,
                (suggestions.size - maxSuggestions).coerceAtLeast(0)
            )
            updateSuggestionDisplay()
            true
        } else false
    }

    private fun scroll(offset: Int) {
        selectedIndex = (selectedIndex + offset).coerceIn(0, suggestions.size - 1)
        scrollOffset = when {
            selectedIndex < scrollOffset -> selectedIndex
            selectedIndex >= scrollOffset + maxSuggestions -> selectedIndex - maxSuggestions + 1
            else -> scrollOffset
        }.coerceIn(0, (suggestions.size - maxSuggestions).coerceAtLeast(0))
        updateSuggestionDisplay()
    }

    private fun makeSuggestion() {
        if (suggestions.isEmpty()) return

        val currentText = getCurrentText?.invoke() ?: ""
        onSuggestionApplied?.invoke(suggestions[selectedIndex].apply(currentText))
        completed = true
        hideCmp()
    }

    fun hideCmp() {
        visible = false
        suggestions = emptyList()
        suggestionTexts.forEach { it.destroy() }
        suggestionBgs.forEach { it.destroy() }
        suggestionTexts.clear()
        suggestionBgs.clear()
    }

    override fun onRender(mouseX: Float, mouseY: Float) {
        container.visible = visible
        if (!visible) {
            width = 0f
            height = 0f
        }
    }

    override fun getAutoWidth(): Float = if (visible) width else 0f
    override fun getAutoHeight(): Float = if (visible) height else 0f

    fun position(x: Float, y: Float): CommandSuggester = apply {
        setPositioning(x, Pos.ParentPixels, y, Pos.ParentPixels)
    }
}