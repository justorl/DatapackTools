package com.pulse.datapacktools.client

import com.pulse.datapacktools.client.keybindings.DatapackEditorKeybinding
import com.pulse.datapacktools.client.keybindings.DatapackSelectionKeybinding
import com.pulse.datapacktools.client.keybindings.ExportKeybinding
import com.pulse.datapacktools.client.packets.ClientPackets
import net.fabricmc.api.ClientModInitializer
import net.minecraft.client.option.KeyBinding
import net.minecraft.util.Identifier

class DatapackToolsClient : ClientModInitializer {
    companion object {
        val MOD_KEYBINDING_CATEGORY = KeyBinding.Category.create(Identifier.of("category.datapacktools"))
    }

    override fun onInitializeClient() {
        ExportKeybinding.register()
        DatapackEditorKeybinding.register()
        DatapackSelectionKeybinding.register()
        ClientPackets.register()
    }
}