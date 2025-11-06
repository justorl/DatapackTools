package com.pulse.datapacktools.main.packets

import com.pulse.datapacktools.client.packets.ClientPackets
import com.pulse.datapacktools.main.config.ModConfig
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier

object GetCurrentPacket {
    val ID = Identifier("datapacktools", "get_current")

    fun register() {
        ServerPlayNetworking.registerGlobalReceiver(ID) { server, player, handler, buf, responseSender ->
            if (player.hasPermissionLevel(ModConfig.data.modPermissionLevel)) {
                server.execute {
                    handle(player)
                }
            }
        }
    }

    fun handle(player: ServerPlayerEntity) {
        val buf = PacketByteBufs.create()
        buf.writeString("${ModConfig.data.datapackName}:${ModConfig.data.datapackNamespace}")
        ServerPlayNetworking.send(player, ClientPackets.GET_CURRENT_PACKET, buf)
    }
}