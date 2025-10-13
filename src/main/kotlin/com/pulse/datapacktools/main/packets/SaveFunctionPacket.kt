package com.pulse.datapacktools.main.packets

import com.pulse.datapacktools.main.utils.DatapacksUtils.DATAPACK_NAME
import com.pulse.datapacktools.main.utils.DatapacksUtils.createDefaultDatapack
import com.pulse.datapacktools.main.utils.SessionUtils
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import java.io.File

object SaveFunctionPacket {
    val ID = Identifier("datapacktools", "save_function")

    fun register() {
        ServerPlayNetworking.registerGlobalReceiver(ID) { server, player, handler, buf, responseSender ->
            val functionName = buf.readString()
            val content = buf.readString()

            if (player.hasPermissionLevel(4)) {
                server.execute {
                    handle(player, functionName, content)
                }
            }
        }
    }

    fun handle(player: ServerPlayerEntity, functionName: String, content: String) {
        val worldDir: File = SessionUtils.worldFolderPath?.toFile() ?: return

        createDefaultDatapack(worldDir)
        val functionsDir = File(worldDir, "datapacks/${DATAPACK_NAME}/data/${DATAPACK_NAME}/functions")
        val functionFile = File(functionsDir, "${functionName}.mcfunction")

        functionFile.writeText(content)

        val server = player.server
        val dataManager = server.dataPackManager
        val keyName = "file/${DATAPACK_NAME}"

        dataManager.enabledNames.toMutableList().remove(keyName)
        dataManager.enabledNames.toMutableList().add(keyName)
        server.reloadResources(dataManager.enabledNames)
    }
}
