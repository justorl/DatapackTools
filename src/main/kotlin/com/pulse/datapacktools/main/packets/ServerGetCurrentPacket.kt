package com.pulse.datapacktools.main.packets

import com.pulse.datapacktools.main.config.ModConfig
import com.pulse.datapacktools.packets.custom.GetCurrentPacket
import com.pulse.datapacktools.packets.custom.SendCurrentPacket
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.server.network.ServerPlayerEntity

object ServerGetCurrentPacket {
    fun register() {
        ServerPlayNetworking.registerGlobalReceiver(GetCurrentPacket.ID) { packet, context ->
            if (context.player().hasPermissionLevel(ModConfig.data.modPermissionLevel)) {
                context.server().execute {
                    handle(context.player())
                }
            }
        }
    }

    fun handle(player: ServerPlayerEntity) {
        val current = "${ModConfig.data.datapackName}:${ModConfig.data.datapackNamespace}"
        ServerPlayNetworking.send(player, SendCurrentPacket(current))
    }
}