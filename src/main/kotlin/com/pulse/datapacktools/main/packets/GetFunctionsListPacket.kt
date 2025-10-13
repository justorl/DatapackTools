package com.pulse.datapacktools.main.packets

import com.pulse.datapacktools.client.packets.ClientPackets
import com.pulse.datapacktools.main.utils.DatapacksUtils.DATAPACK_NAME
import com.pulse.datapacktools.main.utils.DatapacksUtils.createDefaultDatapack
import com.pulse.datapacktools.main.utils.SessionUtils
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import java.io.File

object GetFunctionsListPacket {
    val ID = Identifier("datapacktools", "get_functions_list")

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

        createDefaultDatapack(worldDir)
        val functionsDir = File(worldDir, "datapacks/${DATAPACK_NAME}/data/${DATAPACK_NAME}/functions")
        
        if (!functionsDir.exists()) functionsDir.mkdirs()

        val functions = functionsDir.listFiles { file ->
            file.isFile && file.extension == "mcfunction" && file.name.isNotEmpty()
        }.map { it.nameWithoutExtension }

        val buf = PacketByteBufs.create()
        buf.writeInt(functions.size)
        functions.forEach { functionName ->
            buf.writeString(functionName)
        }
        ServerPlayNetworking.send(player, ClientPackets.GET_FUNCTIONS_LIST_PACKET, buf)
    }
}
