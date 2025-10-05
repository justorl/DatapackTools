package com.pulse.datapacktools.client

import com.pulse.datapacktools.client.keybindings.ExportKeybinding
import net.fabricmc.api.ClientModInitializer

class DatapackToolsClient : ClientModInitializer {
    override fun onInitializeClient() {
        ExportKeybinding.register()
    }
}