package com.pulse.datapacktools.main.packets

import com.pulse.datapacktools.main.config.ModConfig
import com.pulse.datapacktools.main.utils.SessionUtils
import com.pulse.datapacktools.packets.custom.GetDatapacksPacket
import com.pulse.datapacktools.packets.custom.SendDatapacksPacket
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.server.network.ServerPlayerEntity
import java.io.File

object ServerGetDatapacksPacket {
    fun register() {
        ServerPlayNetworking.registerGlobalReceiver(GetDatapacksPacket.ID) { packet, context ->
            if (context.player().hasPermissionLevel(ModConfig.data.modPermissionLevel)) {
                context.server().execute {
                    handle(context.player())
                }
            }
        }
    }

    fun handle(player: ServerPlayerEntity) {
        val worldDir: File = SessionUtils.worldFolderPath?.toFile() ?: return
        val datapacksDir = File(worldDir, "datapacks")

        if (!datapacksDir.exists()) {
            ServerPlayNetworking.send(player, SendDatapacksPacket(mutableListOf()))
            return
        }

        val datapacks = mutableListOf<String>()

        datapacksDir.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                if (File(file, "pack.mcmeta").exists()) {
                    datapacks.add(file.name)
                }
            }
        }

        ServerPlayNetworking.send(player, SendDatapacksPacket(datapacks))
    }
}
