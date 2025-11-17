package com.pulse.datapacktools.main.packets.custom

import com.pulse.datapacktools.main.config.ModConfig
import com.pulse.datapacktools.main.utils.SessionUtils
import com.pulse.datapacktools.packets.GetNamespacesPacket
import com.pulse.datapacktools.packets.SendNamespacesPacket
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.server.network.ServerPlayerEntity
import java.io.File

object ServerGetNamespacesPacket {
    fun register() {
        PayloadTypeRegistry.playC2S().register(GetNamespacesPacket.ID, GetNamespacesPacket.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(GetNamespacesPacket.ID) { packet, context ->
            if (context.player().hasPermissionLevel(ModConfig.data.modPermissionLevel)) {
                context.server().execute {
                    handle(context.player(), packet.datapackName)
                }
            }
        }
    }

    fun handle(player: ServerPlayerEntity, datapackName: String) {
        val worldDir: File = SessionUtils.worldFolderPath?.toFile() ?: return
        val namespacesDir = File(worldDir, "datapacks/$datapackName/data")

        if (!namespacesDir.exists()) {
            ServerPlayNetworking.send(player, SendNamespacesPacket(mutableListOf()))
            return
        }

        val namespaces = mutableListOf<String>()

        namespacesDir.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                namespaces.add(file.name)
            }
        }

        ServerPlayNetworking.send(player, SendNamespacesPacket(namespaces))
    }
}
