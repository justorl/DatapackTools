package com.pulse.datapacktools.main

import com.pulse.datapacktools.main.config.ModConfig
import com.pulse.datapacktools.main.utils.SessionUtils
import com.pulse.datapacktools.mixin.main.SessionAccessor
import com.pulse.datapacktools.packets.Packets
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.server.MinecraftServer
import java.nio.file.Path

class DatapackTools : ModInitializer {

    companion object {const val ID = "datapacktools"}

    override fun onInitialize() {
        Packets.register()
        ModConfig.load()

        ServerLifecycleEvents.SERVER_STARTING.register(ServerLifecycleEvents.ServerStarting { server: MinecraftServer? ->
            val session = (server as SessionAccessor).getSession()
            val worldName = session.directoryName

            SessionUtils.worldFolderPath =
                if (!server.isDedicated) { //here we check if the server IS NOT dedicated, if it isn't we use the integrated server's file path
                    Path.of("saves", worldName)
                } else { // if the server IS dedicated, we use the dedicated server's file path
                    Path.of(worldName)
                }
        })
    }
}