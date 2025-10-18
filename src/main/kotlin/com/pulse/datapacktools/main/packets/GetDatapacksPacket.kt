package com.pulse.datapacktools.main.packets

import com.pulse.datapacktools.client.packets.ClientPackets
import com.pulse.datapacktools.main.utils.SessionUtils
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import java.io.File

object GetDatapacksPacket {
    val ID = Identifier("datapacktools", "get_datapacks")

    fun register() {
        ServerPlayNetworking.registerGlobalReceiver(ID) { server, player, handler, buf, responseSender ->
            if (player.hasPermissionLevel(4)) {
                server.execute {
                    handle(player)
                }
            }
        }
    }

    fun handle(player: ServerPlayerEntity) {
        val worldDir: File = SessionUtils.worldFolderPath?.toFile() ?: return
        val datapacksDir = File(worldDir, "datapacks")

        if (!datapacksDir.exists()) {
            val buf = PacketByteBufs.create()
            buf.writeInt(0)
            ServerPlayNetworking.send(player, ClientPackets.GET_DATAPACKS_PACKET, buf)
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

        val buf = PacketByteBufs.create()
        buf.writeInt(datapacks.size)
        datapacks.forEach { buf.writeString(it) }
        ServerPlayNetworking.send(player, ClientPackets.GET_DATAPACKS_PACKET, buf)
    }
}
