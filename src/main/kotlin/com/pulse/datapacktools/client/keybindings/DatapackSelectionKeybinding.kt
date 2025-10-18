package com.pulse.datapacktools.client.keybindings

import com.pulse.datapacktools.client.screen.DatapackSelectionScreen
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import org.lwjgl.glfw.GLFW

object DatapackSelectionKeybinding {
    lateinit var datapackSelectionKey: KeyBinding

    fun register() {
        datapackSelectionKey = KeyBindingHelper.registerKeyBinding(
            KeyBinding(
                "key.datapacktools.datapack_selection",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_F8,
                "category.datapacktools"
            )
        )

        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick { client: MinecraftClient? ->
            if (datapackSelectionKey.wasPressed()) {
                client?.setScreen(DatapackSelectionScreen())
            }
        })
    }
}