package com.pulse.datapacktools.client.keybindings

import com.pulse.datapacktools.client.DatapackToolsClient.Companion.MOD_KEYBINDING_CATEGORY
import com.pulse.datapacktools.client.screen.ExportFunctionScreen
import com.pulse.datapacktools.client.util.PlayerUtil.getCommandBlockInFront
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import org.lwjgl.glfw.GLFW

object ExportKeybinding {
    lateinit var exportKey: KeyBinding

    fun register() {
        exportKey = KeyBindingHelper.registerKeyBinding(
            KeyBinding(
                "key.datapacktools.export_key",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_F10,
                MOD_KEYBINDING_CATEGORY
            )
        )

        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick { client: MinecraftClient? ->
            if (exportKey.wasPressed()) {
                getCommandBlockInFront() ?: return@EndTick
                client?.setScreen(ExportFunctionScreen())
            }
        })
    }
}