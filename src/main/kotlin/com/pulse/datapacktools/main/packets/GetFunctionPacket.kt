package com.pulse.datapacktools.main.packets

import com.pulse.datapacktools.client.packets.ClientPackets
import com.pulse.datapacktools.main.config.ModConfig
import com.pulse.datapacktools.main.utils.SessionUtils
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import java.io.File

object GetFunctionPacket {
    val ID = Identifier("datapacktools", "get_function")

    fun register() {
        ServerPlayNetworking.registerGlobalReceiver(ID) { server, player, handler, buf, responseSender ->
            val functionName = buf.readString()

            if (player.hasPermissionLevel(4)) {
                server.execute {
                    handle(player, functionName)
                }
            }
        }
    }

    fun handle(player: ServerPlayerEntity, functionName: String) {
        val worldDir: File = SessionUtils.worldFolderPath?.toFile() ?: return

        val functionsDir = File(worldDir, "datapacks/${ModConfig.data.datapackName}/data/${ModConfig.data.datapackNamespace}/functions")
        val functionFile = File(functionsDir, "${functionName}.mcfunction")

        if (!functionFile.exists()) return

        val content = functionFile.readText()
        
        val buf = PacketByteBufs.create()
        buf.writeString(functionName)
        buf.writeString(content)
        ServerPlayNetworking.send(player, ClientPackets.GET_FUNCTION_PACKET, buf)
    }
}
