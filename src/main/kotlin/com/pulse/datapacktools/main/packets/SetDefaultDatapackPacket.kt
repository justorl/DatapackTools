package com.pulse.datapacktools.main.packets

import com.pulse.datapacktools.main.config.ModConfig
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Identifier

object SetDefaultDatapackPacket {
    val ID = Identifier("datapacktools", "set_default_datapack")

    fun register() {
        ServerPlayNetworking.registerGlobalReceiver(ID) { server, player, handler, buf, responseSender ->
            val datapackName = buf.readString()

            if (player.hasPermissionLevel(4)) {
                server.execute {
                    handle(player, datapackName)
                }
            }
        }
    }

    fun handle(player: ServerPlayerEntity, datapackName: String) {
        ModConfig.data.defaultDatapack = datapackName
        ModConfig.save()

        player.sendMessage(Text.translatable("message.datapacktools.set_default_datapack.done", datapackName))
    }
}
