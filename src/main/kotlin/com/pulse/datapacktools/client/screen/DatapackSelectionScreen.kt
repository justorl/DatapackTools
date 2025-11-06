package com.pulse.datapacktools.client.screen

import com.pulse.datapacktools.client.packets.ClientPackets
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.text.Text
import net.minecraft.util.Colors

class DatapackSelectionScreen(val parent: Screen? = null) : Screen(Text.translatable("gui.datapacktools.datapack_selection.screen_title")) {

    val maxDatapacks = 15
    var availableDatapacks = listOf<String>()

    private val datapackButtons: MutableList<ButtonWidget> = mutableListOf()
    private lateinit var cancelButton: ButtonWidget

    var current = ""

    override fun init() {
        super.init()

        repeat(maxDatapacks) { i ->
            val btn = ButtonWidget.builder(Text.literal("")) {
                val name = availableDatapacks.getOrNull(i)
                if (!name.isNullOrBlank()) {
                    ClientPlayNetworking.unregisterGlobalReceiver(ClientPackets.GET_DATAPACKS_PACKET)
                    client?.setScreen(NamespaceSelectionScreen(name, parent))
                }
            }.dimensions(width / 2 - 100, height / 2 - 40 + i * 22, 200, 20).build()
            btn.visible = false
            datapackButtons.add(btn)
            addDrawableChild(btn)
        }

        cancelButton = ButtonWidget.builder(Text.translatable("gui.datapacktools.datapack_selection.cancel")) {
            close()
        }.dimensions(width / 2 + 5, height / 2 - 40 + maxDatapacks * 22, 95, 20).build()
        addDrawableChild(cancelButton)

        registerPacketHandlers()
        ClientPackets.sendGetDatapacksPacket()
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground(context)

        context.drawCenteredTextWithShadow(
            textRenderer,
            Text.translatable("gui.datapacktools.datapack_selection.screen_title"),
            width / 2,
            height / 2 - 80,
            Colors.WHITE
        )

        context.drawCenteredTextWithShadow(
            textRenderer,
            Text.translatable("gui.datapacktools.datapack_selection.subtitle"),
            width / 2,
            height / 2 - 60,
            Colors.GRAY
        )

        context.drawCenteredTextWithShadow(
            textRenderer,
            Text.translatable("gui.datapacktools.datapack_selection.current", current),
            width / 2,
            height / 2 - 60,
            Colors.GRAY
        )

        if (availableDatapacks.isEmpty()) {
            context.drawCenteredTextWithShadow(
                textRenderer,
                Text.translatable("gui.datapacktools.datapack_selection.empty"),
                width / 2,
                height / 2 - 10,
                Colors.GRAY
            )
        }

        super.render(context, mouseX, mouseY, delta)
    }

    override fun close() {
        ClientPlayNetworking.unregisterGlobalReceiver(ClientPackets.GET_DATAPACKS_PACKET)
        super.close()
    }

    private fun registerPacketHandlers() {
        ClientPlayNetworking.registerGlobalReceiver(ClientPackets.GET_DATAPACKS_PACKET) { client, handler, buf, responseSender ->
            val datapacksCount = buf.readInt()
            val datapacks = mutableListOf<String>()
            repeat(datapacksCount) {
                datapacks.add(buf.readString())
            }

            client.execute {
                availableDatapacks = datapacks
                updateDatapackButtons()
            }
        }
        ClientPlayNetworking.registerGlobalReceiver(ClientPackets.GET_CURRENT_PACKET) { client, handler, buf, responseSender ->
            current = buf.readString()
        }
    }

    private fun updateDatapackButtons() {
        datapackButtons.forEachIndexed { i, btn ->
            val name = availableDatapacks.getOrNull(i)
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
