package com.pulse.datapacktools.main.packets.custom

import com.pulse.datapacktools.main.config.ModConfig
import com.pulse.datapacktools.main.utils.SessionUtils
import com.pulse.datapacktools.packets.GetFunctionPacket
import com.pulse.datapacktools.packets.SendFunctionPacket
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.server.network.ServerPlayerEntity
import java.io.File

object ServerGetFunctionPacket {
    fun register() {
        PayloadTypeRegistry.playC2S().register(GetFunctionPacket.ID, GetFunctionPacket.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(GetFunctionPacket.ID) { packet, context ->
            if (context.player().hasPermissionLevel(ModConfig.data.modPermissionLevel)) {
                context.server().execute {
                    handle(context.player(), packet.functionName)
                }
            }
        }
    }

    fun handle(player: ServerPlayerEntity, functionName: String) {
        val worldDir: File = SessionUtils.worldFolderPath?.toFile() ?: return

        val functionsDir = File(worldDir, "datapacks/${ModConfig.data.datapackName}/data/${ModConfig.data.datapackNamespace}/function")
        val functionFile = File(functionsDir, functionName)

        if (!functionFile.exists()) return

        val content = functionFile.readText()

        ServerPlayNetworking.send(player, SendFunctionPacket(functionName, content))
    }
}
