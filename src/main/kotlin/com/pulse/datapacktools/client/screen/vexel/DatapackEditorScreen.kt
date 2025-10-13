package com.pulse.datapacktools.client.screen.vexel

import com.pulse.datapacktools.client.packets.ClientPackets
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
    lateinit var codeInput: CodeEditor

    private val functionButtons = mutableListOf<Button>()
    private var currentFunction: String? = null

    override fun afterInitialization() {
        createMainLayout()
        createSidebar()
        createModal()
        createCodeEditor()

        registerPacketHandlers()
        loadFunctionsList()
    }

    fun createMainLayout() {
        layout = Container()
            .setPositioning(0f, Pos.ParentPixels, 0f, Pos.ParentPixels)
            .setSizing(100f, Size.ParentPerc, 100f, Size.ParentPerc)
            .childOf(window)

        // sidebar
        sidebar = Rectangle()
            .backgroundColor(0xFF212421.toInt())
            .setPositioning(0f, Pos.ParentPixels, 0f, Pos.ParentPixels)
            .setSizing(20f, Size.ParentPerc, 100f, Size.ParentPerc)
            .childOf(layout)
    }

    fun createSidebar() {
        val funcWidth = NVGRenderer.textWidth("Functions", 12F, NVGRenderer.defaultFont)
        Text("Functions")
            .setPositioning(-funcWidth, Pos.ParentCenter, 5f, Pos.ParentPixels)
            .color(0xFFFFFFFF.toInt())
            .childOf(sidebar)

        createFunctionButton = Button("Create Function")
            .setPositioning(5f, Pos.ParentPixels, 25f, Pos.ParentPixels)
            .setSizing(94f, Size.ParentPerc, 25f, Size.Pixels)
            .backgroundColor(0xFF667CDE.toInt())
            .hoverColor(0xFF5568BD.toInt())
            .pressedColor(0xFF5063BA.toInt())
            .borderColor(0x00000000)
            .onClick { _, _, _ ->
                modalBg.visible = true
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
            .backgroundColor(0x80404040.toInt())
            .borderRadius(10F)
            .borderThickness(1F)
            .borderColor(0x00000000)
            .childOf(modalBg)

        val mtWidth = NVGRenderer.textWidth("Functions", 12F, NVGRenderer.defaultFont)
        Text("Enter a name for function")
            .setPositioning(-mtWidth, Pos.ParentCenter, 5F, Pos.ParentPixels)
            .fontSize(16F)
            .childOf(modalWindow)

        modalFunctionNameField = TextInput("")
            .setPositioning(5f, Pos.ParentPixels, 30f, Pos.ParentPixels)
            .setSizing(96F, Size.ParentPerc, 25f, Size.Pixels)
            .backgroundColor(0xFF404040.toInt())
            .borderColor(0x00000000)
            .childOf(modalWindow)

        Button("Create")
            .setPositioning(modalWindow.width / 2 - 120, Pos.ParentPixels, 60f, Pos.ParentPixels)
            .setSizing(0f, Size.Auto, 25f, Size.Pixels)
            .backgroundColor(0xFF667CDE.toInt())
            .hoverColor(0xFF5568BD.toInt())
            .pressedColor(0xFF5063BA.toInt())
            .borderColor(0x00000000)
            .onClick { _, _, _ ->
                val functionName = modalFunctionNameField.value
                if (functionName.isNotEmpty()) {
                    ClientPackets.sendCreateFunctionPacket(functionName)
                    modalFunctionNameField.value = ""
                    modalBg.visible = false
                }
                true
            }
            .childOf(modalWindow)

        Button("Cancel")
            .setPositioning(modalWindow.width / 2 + 50, Pos.ParentPixels, 60f, Pos.ParentPixels)
            .setSizing(0f, Size.Auto, 25f, Size.Pixels)
            .backgroundColor(0xFFDB1D1D.toInt())
            .hoverColor(0xFFB81D1D.toInt())
            .pressedColor(0xFFB51B1B.toInt())
            .borderColor(0x00000000)
            .onClick { _, _, _ ->
                modalBg.visible = false
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

        codeInput = CodeEditor("")
            .setPositioning(0f, Pos.ParentPixels, 0f, Pos.ParentPixels)
            .setSizing(100f, Size.ParentPerc, 100f, Size.ParentPerc)
            .childOf(codeLayout)
        codeInput.visible = false
    }

    private fun loadFunctionsList() {
        ClientPackets.sendGetFunctionsListPacket()
    }

    private fun registerPacketHandlers() {
        ClientPlayNetworking.registerGlobalReceiver(ClientPackets.GET_FUNCTIONS_LIST_PACKET) { client, handler, buf, responseSender ->
            val functionsCount = buf.readInt()
            val functions = mutableListOf<String>()
            
            repeat(functionsCount) {
                functions.add(buf.readString())
            }
            
            client.execute {
                updateFunctionsList(functions)
                updateButtonColors()
            }
        }
        
        ClientPlayNetworking.registerGlobalReceiver(ClientPackets.FUNCTION_CREATED_PACKET) { client, handler, buf, responseSender ->
            client.execute {
                loadFunctionsList()
            }
        }
        
        ClientPlayNetworking.registerGlobalReceiver(ClientPackets.GET_FUNCTION_PACKET) { client, handler, buf, responseSender ->
            val functionName = buf.readString()
            val content = buf.readString()
            
            client.execute {
                if (functionName == codeInput.filePath) {
                    codeInput.value = content
                    codeInput.isLoaded = true
                    codeInput.hasUnsavedChanges = false
                }
            }
        }
    }

    private fun updateFunctionsList(functions: List<String>) {
        val buttonsToDestroy = functionButtons.toList()
        buttonsToDestroy.forEach { it.destroy() }
        functionButtons.clear()

        var yOffset = 0f
        functions.filter { it.isNotEmpty() }.forEach { functionName ->
            val isCurrentFunction = currentFunction == functionName

            val button = Button(functionName)
                .setPositioning(0f, Pos.ParentPixels, yOffset, Pos.ParentPixels)
                .setSizing(100f, Size.ParentPerc, 25f, Size.Pixels)
                .backgroundColor(0xFF333333.toInt())
                .hoverColor(0xFF292929.toInt())
                .pressedColor(0xFF272727.toInt())
                .borderColor(0x00000000)
                .onClick { _, _, _ ->
                    if (!isCurrentFunction) {
                        currentFunction = functionName
                        codeInput.filePath = functionName
                        codeInput.loadFile()
                        codeInput.visible = true
                        codeEmptyText.visible = false
                        updateButtonColors()
                    }
                    true
                }
                .childOf(functionsContainer)

            functionButtons.add(button)
            yOffset += 30f
        }
    }

    private fun updateButtonColors() {
        functionButtons.forEach { button ->
            val isCurrentFunction = currentFunction == button.text
            button.backgroundColor(if (isCurrentFunction) 0xFF0066CC.toInt() else 0xFF333333.toInt())
            button.hoverColor(if (isCurrentFunction) 0xFF0066CC.toInt() else 0xFF303030.toInt())
            button.pressedColor(if (isCurrentFunction) 0xFF0066CC.toInt() else 0xFF272727.toInt())
        }
    }

    override fun onCloseGui() {
        functionButtons.clear()
        ClientPlayNetworking.unregisterGlobalReceiver(ClientPackets.GET_FUNCTIONS_LIST_PACKET)
        ClientPlayNetworking.unregisterGlobalReceiver(ClientPackets.FUNCTION_CREATED_PACKET)
        ClientPlayNetworking.unregisterGlobalReceiver(ClientPackets.GET_FUNCTION_PACKET)
        super.onCloseGui()
    }
}