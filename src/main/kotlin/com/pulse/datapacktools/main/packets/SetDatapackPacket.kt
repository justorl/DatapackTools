package com.pulse.datapacktools.main.packets

import com.pulse.datapacktools.main.config.ModConfig
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Identifier

object SetDatapackPacket {
    val ID = Identifier("datapacktools", "set_datapack")

    fun register() {
        ServerPlayNetworking.registerGlobalReceiver(ID) { server, player, handler, buf, responseSender ->
            val datapackName = buf.readString()
            val namespaceName = buf.readString()

            if (player.hasPermissionLevel(ModConfig.data.modPermissionLevel)) {
                server.execute {
                    handle(player, datapackName, namespaceName)
                }
            }
        }
    }

    fun handle(player: ServerPlayerEntity, datapackName: String, namespaceName: String) {
        ModConfig.data.datapackName = datapackName
        ModConfig.data.datapackNamespace = namespaceName
        ModConfig.save()

        player.sendMessage(Text.translatable("message.datapacktools.set_datapack.done", datapackName, namespaceName))
    }
}