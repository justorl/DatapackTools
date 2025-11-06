package com.pulse.datapacktools.client.screen

import com.pulse.datapacktools.client.packets.ClientPackets
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.text.Text
import net.minecraft.util.Colors

class NamespaceSelectionScreen(val datapackName: String, val parent: Screen? = null) : Screen(Text.translatable("gui.datapacktools.namespace_selection.screen_title")) {

    private val maxNamespaces = 15
    private val namespaceButtons: MutableList<ButtonWidget> = mutableListOf()
    private lateinit var cancelButton: ButtonWidget

    private var availableNamespaces = listOf<String>()

    override fun init() {
        super.init()

        repeat(maxNamespaces) { i ->
            val btn = ButtonWidget.builder(Text.literal("")) {
                val name = availableNamespaces.getOrNull(i)
                if (!name.isNullOrBlank()) {
                    ClientPackets.sendSetDatapackPacket(datapackName, name)
                    ClientPlayNetworking.unregisterGlobalReceiver(ClientPackets.GET_NAMESPACES_PACKET)
                    client?.setScreen(parent)
                }
            }.dimensions(width / 2 - 100, height / 2 - 40 + i * 22, 200, 20).build()
            btn.visible = false
            namespaceButtons.add(btn)
            addDrawableChild(btn)
        }

        cancelButton = ButtonWidget.builder(Text.translatable("gui.datapacktools.namespace_selection.cancel")) {
            close()
        }.dimensions(width / 2 + 5, height / 2 - 40 + maxNamespaces * 22, 95, 20).build()
        addDrawableChild(cancelButton)

        registerPacketHandlers()
        ClientPackets.sendGetNamespacesPacket(datapackName)
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground(context)

        context.drawCenteredTextWithShadow(
            textRenderer,
            Text.translatable("gui.datapacktools.namespace_selection.screen_title"),
            width / 2,
            height / 2 - 80,
            Colors.WHITE
        )

        context.drawCenteredTextWithShadow(
            textRenderer,
            Text.translatable("gui.datapacktools.namespace_selection.subtitle", datapackName),
            width / 2,
            height / 2 - 60,
            Colors.GRAY
        )

        if (availableNamespaces.isEmpty()) {
            context.drawCenteredTextWithShadow(
                textRenderer,
                Text.translatable("gui.datapacktools.namespace_selection.empty"),
                width / 2,
                height / 2 - 10,
                Colors.GRAY
            )
        }

        super.render(context, mouseX, mouseY, delta)
    }

    override fun close() {
        ClientPlayNetworking.unregisterGlobalReceiver(ClientPackets.GET_NAMESPACES_PACKET)
        super.close()
    }

    private fun registerPacketHandlers() {
        ClientPlayNetworking.registerGlobalReceiver(ClientPackets.GET_NAMESPACES_PACKET) { client, handler, buf, responseSender ->
            val namespacesCount = buf.readInt()
            val namespaces = mutableListOf<String>()
            repeat(namespacesCount) {
                namespaces.add(buf.readString())
            }

            client.execute {
                availableNamespaces = namespaces
                updateNamespaceButtons()
            }
        }
    }

    private fun updateNamespaceButtons() {
        namespaceButtons.forEachIndexed { i, btn ->
            val name = availableNamespaces.getOrNull(i)
            if (!name.isNullOrBlank()) {
                btn.visible = true
                btn.message = Text.literal(name)
                btn.active = true
            } else {
                btn.visible = false
                btn.message = Text.literal("")
                btn.active = false
            }
        }
    }
}
