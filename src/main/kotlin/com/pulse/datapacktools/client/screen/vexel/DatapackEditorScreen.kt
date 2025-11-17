package com.pulse.datapacktools.client.screen.vexel

import com.pulse.datapacktools.packets.custom.CreateFunctionPacket
import com.pulse.datapacktools.packets.custom.FunctionCreatedPacket
import com.pulse.datapacktools.packets.custom.GetFunctionsPacket
import com.pulse.datapacktools.packets.custom.SendFunctionPacket
import com.pulse.datapacktools.packets.custom.SendFunctionsPacket
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import xyz.meowing.vexel.components.base.Pos
import xyz.meowing.vexel.components.base.Size
import xyz.meowing.vexel.components.core.Container
import xyz.meowing.vexel.components.core.Rectangle
import xyz.meowing.vexel.components.core.Text
import xyz.meowing.vexel.core.VexelScreen
import xyz.meowing.vexel.elements.Button
import xyz.meowing.vexel.elements.TextInput
import xyz.meowing.vexel.utils.render.NVGRenderer

class DatapackEditorScreen : VexelScreen() {
    lateinit var layout: Container
    lateinit var sidebar: Rectangle

    lateinit var functionsContainer: Container
    lateinit var createFunctionButton: Button

    lateinit var modalBg: Rectangle
    lateinit var modalWindow: Rectangle
    lateinit var modalFunctionNameField: TextInput

    lateinit var codeLayout: Rectangle
    lateinit var codeEmptyText: Text
    lateinit var codeEditor: CodeEditor

    private val functionButtons = mutableListOf<Button>()
    private val functionButtonPath = mutableMapOf<Button, String>()

    var currentFunction: String? = null
    private var currentFunctionButton: Button? = null

    private val folderButtons = mutableListOf<Button>()
    private val expandedFolders = mutableSetOf<String>()

    var functions: List<String> = emptyList()

    data class TreeNode(
        val name: String,
        val fullPath: String,
        val isFolder: Boolean,
        val children: MutableList<TreeNode> = mutableListOf()
    )

    override fun afterInitialization() {
        createMainLayout()
        createSidebar()
        createModal()
        createCodeEditor()

        registerPacketHandlers()
        ClientPlayNetworking.send(GetFunctionsPacket)
    }

    fun createMainLayout() {
        layout = Container()
            .setPositioning(0f, Pos.ParentPixels, 0f, Pos.ParentPixels)
            .setSizing(100f, Size.ParentPerc, 100f, Size.ParentPerc)
            .childOf(window)

        sidebar = Rectangle()
            .backgroundColor(0xFF212421.toInt())
            .setPositioning(0f, Pos.ParentPixels, 0f, Pos.ParentPixels)
            .setSizing(20f, Size.ParentPerc, 100f, Size.ParentPerc)
            .childOf(layout)
    }

    fun createSidebar() {
        createFunctionButton = Button("Create Function")
            .setPositioning(5f, Pos.ParentPixels, 25f, Pos.ParentPixels)
            .setSizing(94f, Size.ParentPerc, 25f, Size.Pixels)
            .backgroundColor(0xFF667CDE.toInt())
            .hoverColor(0xFF5568BD.toInt())
            .pressedColor(0xFF5063BA.toInt())
            .borderColor(0x00000000)
            .onClick { _, _, _ ->
                modalBg.visible = true
                codeEditor.active = false
                true
            }
            .childOf(sidebar)

        Rectangle()
            .setPositioning(5f, Pos.ParentPixels, 60f, Pos.ParentPixels)
            .setSizing(94f, Size.ParentPerc, 2f, Size.Pixels)
            .backgroundColor(0xFF4A4A4A.toInt())
            .borderRadius(5F)
            .borderThickness(5F)
            .borderColor(0x00000000F)
            .childOf(sidebar)

        functionsContainer = Container()
            .setPositioning(5f, Pos.ParentPixels, 75f, Pos.ParentPixels)
            .setSizing(94F, Size.ParentPerc, 100f, Size.ParentPerc)
            .scrollable(true)
            .childOf(sidebar)
    }

    fun createModal() {
        modalBg = Rectangle()
            .setPositioning(Pos.ParentPixels, Pos.ParentPixels)
            .setSizing(Size.ParentPerc, Size.ParentPerc)
            .backgroundColor(0x64000000)
            .childOf(window)
        modalBg.visible = false

        modalWindow = Rectangle()
            .setPositioning(Pos.ParentCenter, Pos.ParentCenter)
            .setSizing(250F, Size.Pixels, 100F, Size.Pixels)
            .backgroundColor(0xFF171717.toInt())
            .borderRadius(10F)
            .borderThickness(1F)
            .borderColor(0x00000000)
            .childOf(modalBg)

        val mtWidth = NVGRenderer.textWidth("Enter a name for function", 16F, NVGRenderer.defaultFont)
        Text("Enter a name for function")
            .setPositioning(-mtWidth, Pos.ParentCenter, 5F, Pos.ParentPixels)
            .fontSize(16F)
            .childOf(modalWindow)

        modalFunctionNameField = TextInput("")
            .setPositioning(5f, Pos.ParentPixels, 30f, Pos.ParentPixels)
            .setSizing(96F, Size.ParentPerc, 25f, Size.Pixels)
            .backgroundColor(0xFF404040.toInt())
            .hoverColor(0xFF404040.toInt())
            .pressedColor(0xFF404040.toInt())
            .borderColor(0x00000000)
            .childOf(modalWindow)

        Button("Create")
            .setPositioning(modalWindow.width / 2 - 120, Pos.ParentPixels, 65f, Pos.ParentPixels)
            .setSizing(0f, Size.Auto, 25f, Size.Pixels)
            .backgroundColor(0xFF667CDE.toInt())
            .hoverColor(0xFF5568BD.toInt())
            .pressedColor(0xFF5063BA.toInt())
            .borderColor(0x00000000)
            .onClick { _, _, _ ->
                val functionName = modalFunctionNameField.value
                if (functionName.isNotEmpty()) {
                    ClientPlayNetworking.send(CreateFunctionPacket(functionName))
                    modalFunctionNameField.value = ""
                    modalBg.visible = false
                    codeEditor.active = true
                }
                true
            }
            .childOf(modalWindow)

        Button("Cancel")
            .setPositioning(modalWindow.width / 2 + 50, Pos.ParentPixels, 65f, Pos.ParentPixels)
            .setSizing(0f, Size.Auto, 25f, Size.Pixels)
            .backgroundColor(0xFFDB1D1D.toInt())
            .hoverColor(0xFFB81D1D.toInt())
            .pressedColor(0xFFB51B1B.toInt())
            .borderColor(0x00000000)
            .onClick { _, _, _ ->
                modalBg.visible = false
                codeEditor.active = true
                true
            }
            .childOf(modalWindow)
    }

    fun createCodeEditor() {
        codeLayout = Rectangle()
            .setPositioning(-1f, Pos.AfterSibling, 0f, Pos.ParentPixels)
            .setSizing(100f, Size.ParentPerc, 100f, Size.ParentPerc)
            .backgroundColor(0xFF000408.toInt())
            .childOf(layout)
        codeEmptyText = Text("Open a function!")
            .setPositioning(Pos.ParentCenter, Pos.ParentCenter)
            .color(0xFF525252.toInt())
            .fontSize(20F)
            .childOf(layout)

        codeEditor = CodeEditor("")
            .setPositioning(0f, Pos.ParentPixels, 0f, Pos.ParentPixels)
            .setSizing(100f, Size.ParentPerc, 100f, Size.ParentPerc)
            .childOf(codeLayout)
        codeEditor.visible = false
        
        codeEditor.onUnsavedChanges = { unsaved ->
            if (currentFunction != null && unsaved) {
                currentFunctionButton?.textColor = 0xFF787878.toInt()
            }
        }
    }


    private fun registerPacketHandlers() {
        ClientPlayNetworking.registerGlobalReceiver(SendFunctionsPacket.ID) { packet, context ->
            context.client().execute {
                functions = packet.functions
                renderFunctionTree()
                updateButtonColors()
            }
        }
        
        ClientPlayNetworking.registerGlobalReceiver(FunctionCreatedPacket.ID) { packet, context ->
            context.client().execute {
                ClientPlayNetworking.send(GetFunctionsPacket)
            }
        }
        
        ClientPlayNetworking.registerGlobalReceiver(SendFunctionPacket.ID) { packet, context ->
            context.client().execute {
                codeEditor.value = packet.content
                codeEditor.isLoaded = true
                codeEditor.hasUnsavedChanges = false
            }
        }
    }

    private fun renderFunctionTree() {
        (functionButtons + folderButtons).forEach { it.destroy() }
        functionButtons.clear()
        folderButtons.clear()
        functionButtonPath.clear()

        val tree = buildFunctionTree(functions)
        renderTree(tree, 0, 0f)
    }

    fun buildFunctionTree(functions: List<String>): TreeNode {
        val root = TreeNode("", "", true)

        functions.forEach { path ->
            val parts = path
                .substringAfter(":", path)
                .split("/")
                .filter { it.isNotEmpty() }

            var currentFunc = root
            var fullPath = ""

            parts.forEachIndexed { index, part ->
                val isFile = index == parts.lastIndex && part.endsWith(".mcfunction")

                fullPath = if (fullPath.isEmpty()) part else "$fullPath/$part"

                currentFunc = currentFunc.children.find { it.name == part } ?: TreeNode(
                    part,
                    fullPath,
                    !isFile
                ).also { currentFunc.children.add(it) }
            }
        }

        return root
    }

    private fun renderTree(node: TreeNode, depth: Int, startYOffset: Float): Float {
        var yOffset = startYOffset
        for (child in node.children) {
            if (child.isFolder) {
                val isExpanded = expandedFolders.contains(child.fullPath)
                val label = (if (isExpanded) "- " else "+ ") + child.name
                val btn = Button(label)
                    .setPositioning(depth * 12f, Pos.ParentPixels, yOffset, Pos.ParentPixels)
                    .setSizing(100f - depth * 12f, Size.ParentPerc, 25f, Size.Pixels)
                    .backgroundColor(0x00000000)
                    .hoverColor(0x50303030)
                    .pressedColor(0x50272727)
                    .borderColor(0x00000000)
                    .onClick { _, _, _ ->
                        if (isExpanded) expandedFolders.remove(child.fullPath) else expandedFolders.add(child.fullPath)
                        renderFunctionTree()
                        true
                    }
                    .childOf(functionsContainer)
                folderButtons.add(btn)
                yOffset += 30f
                if (isExpanded) {
                    yOffset = renderTree(child, depth + 1, yOffset)
                }
            } else {
                val functionName = child.fullPath
                val isCurrentFunction = currentFunction == functionName
                val btn = Button(child.name.removeSuffix(".mcfunction"))
                    .setPositioning(depth * 12f, Pos.ParentPixels, yOffset, Pos.ParentPixels)
                    .setSizing(100f - depth * 12f, Size.ParentPerc, 25f, Size.Pixels)
                    .backgroundColor(0xFF333333.toInt())
                    .hoverColor(0xFF303030.toInt())
                    .pressedColor(0xFF272727.toInt())
                    .borderColor(0x00000000)
                    .textColor(0xFFFFFFFF.toInt())
                    .onClick { _, _, _ ->
                        if (isCurrentFunction) return@onClick false

                        currentFunction = functionName
                        codeEditor.filePath = functionName
                        codeEditor.loadFile()
                        codeEditor.visible = true
                        codeEmptyText.visible = false
                        updateButtonColors()
                        true
                    }
                    .childOf(functionsContainer)

                if (isCurrentFunction) currentFunctionButton = btn
                functionButtons.add(btn)
                functionButtonPath[btn] = functionName
                updateButtonColors()

                yOffset += 30f
            }
        }
        return yOffset
    }

    private fun updateButtonColors() {
        functionButtons.forEach { button ->
            val path = functionButtonPath[button]
            val isCurrentFunction = currentFunction == path
            button.backgroundColor(if (isCurrentFunction) 0xFF0066CC.toInt() else 0xFF333333.toInt())
            button.hoverColor(if (isCurrentFunction) 0xFF0066CC.toInt() else 0xFF303030.toInt())
            button.pressedColor(if (isCurrentFunction) 0xFF0066CC.toInt() else 0xFF272727.toInt())
        }
    }

    override fun onCloseGui() {
        ClientPlayNetworking.unregisterGlobalReceiver(SendFunctionsPacket.ID.id)
        ClientPlayNetworking.unregisterGlobalReceiver(FunctionCreatedPacket.ID.id)
        ClientPlayNetworking.unregisterGlobalReceiver(GetFunctionsPacket.ID.id)
        super.onCloseGui()
    }
}