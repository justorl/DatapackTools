package com.pulse.datapacktools.client.screen

import com.pulse.datapacktools.packets.GetDatapacksPacket
import com.pulse.datapacktools.packets.SendCurrentPacket
import com.pulse.datapacktools.packets.SendDatapacksPacket
import com.pulse.datapacktools.packets.SendNamespacesPacket
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
                    ClientPlayNetworking.unregisterGlobalReceiver(SendNamespacesPacket.ID.id)
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
        ClientPlayNetworking.send(GetDatapacksPacket)
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
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
            height / 2 - 20,
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
        ClientPlayNetworking.unregisterGlobalReceiver(SendDatapacksPacket.ID.id)
        super.close()
    }

    private fun registerPacketHandlers() {
        ClientPlayNetworking.registerGlobalReceiver(SendDatapacksPacket.ID) { packet, context ->
            context.client().execute {
                availableDatapacks = packet.datapacks
                updateDatapackButtons()
            }
        }
        ClientPlayNetworking.registerGlobalReceiver(SendCurrentPacket.ID) { packet, context ->
            current = packet.current
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
