package com.pulse.datapacktools.main.packets

import com.pulse.datapacktools.main.config.ModConfig
import com.pulse.datapacktools.packets.custom.SetDatapackPacket
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text

object ServerSetDatapackPacket {
    fun register() {
        ServerPlayNetworking.registerGlobalReceiver(SetDatapackPacket.ID) { packet, context ->
            if (context.player().hasPermissionLevel(ModConfig.data.modPermissionLevel)) {
                context.server().execute {
                    handle(context.player(), packet.datapackName, packet.namespaceName)
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