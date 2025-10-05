package com.pulse.datapacktools.client.buttons

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class IconButtonWidget(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    private val icon: Identifier,
    onPress: PressAction
) : ButtonWidget(x, y, width, height, Text.empty(), onPress, DEFAULT_NARRATION_SUPPLIER) {

    override fun renderButton(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        super.renderButton(context, mouseX, mouseY, delta)

        val iconSize = 12
        val iconX = this.x + (this.width - iconSize) / 2
        val iconY = this.y + (this.height - iconSize) / 2
        context.drawTexture(icon, iconX, iconY, 0f, 0f, iconSize, iconSize, iconSize, iconSize)
    }
}
