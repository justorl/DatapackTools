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

class DatapackEditorScreen : VexelScreen() {
    lateinit var layout: Container
    lateinit var sidebar: Rectangle
    lateinit var functionsContainer: Container
    lateinit var createFunctionButton: Button
    lateinit var functionNameField: TextInput

    lateinit var codeLayout: Rectangle
    lateinit var codeEmptyText: Text
    lateinit var codeInput: CodeEditor

    private val functionButtons = mutableListOf<Button>()
    private var currentFunction: String? = null

    override fun afterInitialization() {
        // main layout
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

        Text("Functions")
            .setPositioning(5f, Pos.ParentPixels, 5f, Pos.ParentPixels)
            .color(0xFFFFFFFF.toInt())
            .childOf(sidebar)
        createFunctionButton = Button("Create Function")
            .setPositioning(5f, Pos.ParentPixels, 25f, Pos.ParentPixels)
            .setSizing(90f, Size.ParentPerc, 25f, Size.Pixels)
            .backgroundColor(0xFF4CAF50.toInt())
            .onClick { _, _, _ ->
                if (functionNameField.visible) {
                    val functionName = functionNameField.value
                    if (functionName.isNotEmpty()) {
                        ClientPackets.sendCreateFunctionPacket(functionName)
                        functionNameField.value = ""
                        functionNameField.visible = false
                    }
                } else {
                    functionNameField.visible = true
                }
                true
            }
            .childOf(sidebar)

        functionNameField = TextInput("")
            .setPositioning(5f, Pos.ParentPixels, 60f, Pos.ParentPixels)
            .setSizing(90f, Size.ParentPerc, 20f, Size.Pixels)
            .backgroundColor(0xFF404040.toInt())
            .childOf(sidebar)
        functionNameField.visible = false

        functionsContainer = Container()
            .setPositioning(5f, Pos.ParentPixels, 90f, Pos.ParentPixels)
            .setSizing(90f, Size.ParentPerc, 100f, Size.ParentPerc)
            .scrollable(true)
            .childOf(sidebar)

        // code editor
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

        registerPacketHandlers()
        loadFunctionsList()
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
            val functionName = buf.readString()
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

            val hoverColor = if (isCurrentFunction) 0xFF0554A3.toInt() else 0xFF282828.toInt()
            val pressedColor = if (isCurrentFunction) 0xFF034B94.toInt() else 0xFF252525.toInt()

            val button = Button(functionName)
                .setPositioning(0f, Pos.ParentPixels, yOffset, Pos.ParentPixels)
                .setSizing(100f, Size.ParentPerc, 25f, Size.Pixels)
                .backgroundColor(if (isCurrentFunction) 0xFF0066CC.toInt() else 0xFF333333.toInt())
                .hoverColor(hoverColor)
                .pressedColor(pressedColor)
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