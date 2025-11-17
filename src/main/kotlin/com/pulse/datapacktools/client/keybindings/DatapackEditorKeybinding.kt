package com.pulse.datapacktools.client.keybindings

import com.pulse.datapacktools.client.DatapackToolsClient.Companion.MOD_KEYBINDING_CATEGORY
import com.pulse.datapacktools.client.screen.vexel.DatapackEditorScreen
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import org.lwjgl.glfw.GLFW

object DatapackEditorKeybinding {
    lateinit var editorKey: KeyBinding

    fun register() {
        editorKey = KeyBindingHelper.registerKeyBinding(
            KeyBinding(
                "key.datapacktools.editor_key",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_ALT,
                MOD_KEYBINDING_CATEGORY
            )
        )

        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick { client: MinecraftClient? ->
            if (editorKey.wasPressed()) {
                client?.setScreen(DatapackEditorScreen())
            }
        })
    }
}