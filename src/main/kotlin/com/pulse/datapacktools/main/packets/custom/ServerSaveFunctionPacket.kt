package com.pulse.datapacktools.main.packets.custom

import com.pulse.datapacktools.main.config.ModConfig
import com.pulse.datapacktools.main.utils.DatapacksUtils.createDefaultDatapack
import com.pulse.datapacktools.main.utils.DatapacksUtils.updateDatapack
import com.pulse.datapacktools.main.utils.SessionUtils
import com.pulse.datapacktools.packets.SaveFunctionPacket
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.server.network.ServerPlayerEntity
import java.io.File

object ServerSaveFunctionPacket {
    fun register() {
        PayloadTypeRegistry.playC2S().register(SaveFunctionPacket.ID, SaveFunctionPacket.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(SaveFunctionPacket.ID) { packet, context ->
            if (context.player().hasPermissionLevel(ModConfig.data.modPermissionLevel)) {
                context.server().execute {
                    handle(context.player(), packet.functionName, packet.content)
                }
            }
        }
    }

    fun handle(player: ServerPlayerEntity, functionName: String, content: String) {
        val worldDir: File = SessionUtils.worldFolderPath?.toFile() ?: return
        createDefaultDatapack(worldDir)

        val functionsDir = File(worldDir, "datapacks/${ModConfig.data.datapackName}/data/${ModConfig.data.datapackNamespace}/functions")
        val functionFile = File(functionsDir, functionName)
        functionFile.writeText(content)

        updateDatapack(player)
    }
}