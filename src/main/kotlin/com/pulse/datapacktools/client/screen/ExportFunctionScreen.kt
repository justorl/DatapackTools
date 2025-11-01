package com.pulse.datapacktools.client.screen

import com.pulse.datapacktools.client.packets.ClientPackets
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

    override fun init() {
        super.init()

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

        changeCommandCheckBox = CheckboxWidget(
            width / 2 - 100,
            height / 2 + 20,
            200,
            20,
            Text.translatable("gui.datapacktools.export.replace_checkmark"),
            lastChangeCommandCheckbox
        )
        changeCommandCheckBox.tooltip = Tooltip.of(
            Text.translatable("gui.datapacktools.export.replace_checkmark.description")
        )

        exportButton = ButtonWidget.builder(Text.translatable("gui.datapacktools.export.export_button")) {
            val functionName = functionNameField.text
            if (functionName.isNotBlank()) {
                close()
                ClientPackets.sendConvertPacket(functionNameField.text, changeCommandCheckBox.isChecked)
            }
        }.dimensions(width / 2 - 100, height / 2 + 50, 95, 20).build()
        exportButton.active = false

        cancelButton = ButtonWidget.builder(Text.translatable("gui.datapacktools.export.cancel")) {
            close()
        }.dimensions(width / 2 + 5, height / 2 + 50, 95, 20).build()

        addDrawableChild(functionNameField)
        addDrawableChild(changeCommandCheckBox)
        addDrawableChild(exportButton)
        addDrawableChild(cancelButton)
    }


    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground(context)

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

        super.render(context, mouseX, mouseY, delta)
    }

    override fun tick() {
        super.tick()
        exportButton.active = functionNameField.text.isNotBlank()
    }

    override fun close() {
        lastFunctionName = functionNameField.text
        lastChangeCommandCheckbox = changeCommandCheckBox.isChecked
        super.close()
    }
}
