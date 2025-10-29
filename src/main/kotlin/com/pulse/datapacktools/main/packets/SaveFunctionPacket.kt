package com.pulse.datapacktools.main.packets

import com.pulse.datapacktools.main.config.ModConfig
import com.pulse.datapacktools.main.utils.DatapacksUtils.createDefaultDatapack
import com.pulse.datapacktools.main.utils.DatapacksUtils.updateDatapack
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

        val functionsDir = File(worldDir, "datapacks/${ModConfig.data.datapackName}/data/${ModConfig.data.datapackNamespace}/functions")
        val functionFile = File(functionsDir, "${functionName}.mcfunction")
        functionFile.writeText(content)

        updateDatapack(player)
    }
}