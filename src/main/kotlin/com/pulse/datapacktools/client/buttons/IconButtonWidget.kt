package com.pulse.datapacktools.client.buttons

import com.mojang.blaze3d.pipeline.RenderPipeline
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gl.RenderPipelines
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

    override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, deltaTicks: Float) {
        super.renderWidget(context, mouseX, mouseY, deltaTicks)

        val iconSize = 12
        val iconX = this.x + (this.width - iconSize) / 2
        val iconY = this.y + (this.height - iconSize) / 2
        
        context.drawTexture(RenderPipelines.GUI, icon, iconX, iconY, 0f, 0f, iconSize, iconSize, iconSize, iconSize)
    }
}
