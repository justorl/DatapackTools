package com.pulse.datapacktools.client.screen

import com.pulse.datapacktools.packets.ConvertPacket
import com.pulse.datapacktools.packets.GetCurrentPacket
import com.pulse.datapacktools.packets.SendCurrentPacket
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.CheckboxWidget
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.text.Text
import net.minecraft.util.Colors

class ExportFunctionScreen() : Screen(Text.translatable("gui.datapacktools.export.screen_title")) {
    companion object {
        var lastFunctionName: String = ""
        var lastChangeCommandCheckbox: Boolean = true
    }

    private lateinit var functionNameField: TextFieldWidget
    private lateinit var exportButton: ButtonWidget
    private lateinit var cancelButton: ButtonWidget
    private lateinit var changeCommandCheckBox: CheckboxWidget
    private lateinit var selectAnotherButton: ButtonWidget

    var current = "Загрузка.."

    override fun init() {
        functionNameField = TextFieldWidget(
            textRenderer,
            width / 2 - 100,
            height / 2 - 20,
            200,
            20,
            Text.of("Function Name")
        )
        functionNameField.setMaxLength(16)
        functionNameField.text = lastFunctionName

        changeCommandCheckBox = CheckboxWidget.builder(
            Text.translatable("gui.datapacktools.export.replace_checkmark"),
            textRenderer,
        )
            .checked(lastChangeCommandCheckbox)
            .pos(width / 2 - 100, height / 2 + 10)
            .maxWidth(200)
            .build()

        changeCommandCheckBox.setTooltip(Tooltip.of(
            Text.translatable("gui.datapacktools.export.replace_checkmark.description")
        ))

        exportButton = ButtonWidget.builder(Text.translatable("gui.datapacktools.export.export_button")) {
            val functionName = functionNameField.text
            if (functionName.isNotBlank()) {
                close()
                ClientPlayNetworking.send(ConvertPacket(functionNameField.text, changeCommandCheckBox.isChecked))
            }
        }.dimensions(width / 2 - 100, height / 2 + 40, 95, 20).build()
        exportButton.active = false

        cancelButton = ButtonWidget.builder(Text.translatable("gui.datapacktools.export.cancel")) {
            close()
        }.dimensions(width / 2 + 5, height / 2 + 40, 95, 20).build()

        selectAnotherButton = ButtonWidget.builder(Text.translatable("gui.datapacktools.export.select_another_button")) {
            lastFunctionName = functionNameField.text
            lastChangeCommandCheckbox = changeCommandCheckBox.isChecked
            ClientPlayNetworking.unregisterReceiver(GetCurrentPacket.ID.id)
            client?.setScreen(DatapackSelectionScreen(this))
        }.dimensions(width / 2 - 100, height / 2 + 90, 200, 20).build()

        addDrawableChild(functionNameField)
        addDrawableChild(changeCommandCheckBox)
        addDrawableChild(exportButton)
        addDrawableChild(cancelButton)
        addDrawableChild(selectAnotherButton)

        registerPacketHandlers()
        ClientPlayNetworking.send(GetCurrentPacket)
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        context.drawCenteredTextWithShadow(
            textRenderer,
            Text.translatable("gui.datapacktools.export.screen_title"),
            width / 2,
            height / 2 - 60,
            Colors.WHITE
        )

        context.drawTextWithShadow(
            textRenderer,
            Text.translatable("gui.datapacktools.export.enter_name"),
            width / 2 - 100,
            height / 2 - 30,
            Colors.GRAY
        )

        context.drawCenteredTextWithShadow(
            textRenderer,
            Text.translatable("gui.datapacktools.export.current", current),
            width / 2,
            height / 2 + 80,
            Colors.GRAY
        )

        super.render(context, mouseX, mouseY, delta)
    }

    override fun tick() {
        super.tick()
        exportButton.active = functionNameField.text.isNotBlank()
    }

    override fun close() {
        lastFunctionName = functionNameField.text
        lastChangeCommandCheckbox = changeCommandCheckBox.isChecked
        ClientPlayNetworking.unregisterReceiver(GetCurrentPacket.ID.id)
        super.close()
    }

    private fun registerPacketHandlers() {
        ClientPlayNetworking.registerReceiver(SendCurrentPacket.ID) { packet, context ->
            current = packet.current
        }
    }
}
