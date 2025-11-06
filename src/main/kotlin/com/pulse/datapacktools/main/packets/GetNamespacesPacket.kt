package com.pulse.datapacktools.main.packets

import com.pulse.datapacktools.client.packets.ClientPackets
import com.pulse.datapacktools.main.config.ModConfig
import com.pulse.datapacktools.main.utils.SessionUtils
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import java.io.File

object GetNamespacesPacket {
    val ID = Identifier("datapacktools", "get_namespaces")

    fun register() {
        ServerPlayNetworking.registerGlobalReceiver(ID) { server, player, handler, buf, responseSender ->
            if (player.hasPermissionLevel(ModConfig.data.modPermissionLevel)) {
                val datapackName = buf.readString()
                server.execute {
                    handle(player, datapackName)
                }
            }
        }
    }

    fun handle(player: ServerPlayerEntity, datapackName: String) {
        val worldDir: File = SessionUtils.worldFolderPath?.toFile() ?: return
        val namespacesDir = File(worldDir, "datapacks/$datapackName/data")

        if (!namespacesDir.exists()) {
            val buf = PacketByteBufs.create()
            buf.writeInt(0)
            ServerPlayNetworking.send(player, ClientPackets.GET_NAMESPACES_PACKET, buf)
            return
        }

        val namespaces = mutableListOf<String>()

        namespacesDir.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                namespaces.add(file.name)
            }
        }

        val buf = PacketByteBufs.create()
        buf.writeInt(namespaces.size)
        namespaces.forEach { buf.writeString(it) }
        ServerPlayNetworking.send(player, ClientPackets.GET_NAMESPACES_PACKET, buf)
    }
}
