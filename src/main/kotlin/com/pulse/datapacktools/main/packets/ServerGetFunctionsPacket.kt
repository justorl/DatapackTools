package com.pulse.datapacktools.main.packets

import com.pulse.datapacktools.main.config.ModConfig
import com.pulse.datapacktools.main.utils.DatapacksUtils.createDefaultDatapack
import com.pulse.datapacktools.main.utils.SessionUtils
import com.pulse.datapacktools.packets.custom.GetFunctionsPacket
import com.pulse.datapacktools.packets.custom.SendFunctionsPacket
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.server.network.ServerPlayerEntity
import java.io.File

object ServerGetFunctionsPacket {
    fun register() {
        ServerPlayNetworking.registerGlobalReceiver(GetFunctionsPacket.ID) { packet, context ->
            if (context.player().hasPermissionLevel(ModConfig.data.modPermissionLevel)) {
                context.server().execute {
                    handle(context.player())
                }
            }
        }
    }

    fun handle(player: ServerPlayerEntity) {
        val worldDir: File = SessionUtils.worldFolderPath?.toFile() ?: return

        createDefaultDatapack(worldDir)
        val functionsDir = File(worldDir, "datapacks/${ModConfig.data.datapackName}/data/${ModConfig.data.datapackNamespace}/function")
        
        if (!functionsDir.exists()) functionsDir.mkdirs()

        val functions = mutableListOf<String>()
        functionsDir.walkTopDown()
            .filter { it.isFile && it.extension == "mcfunction" }
            .forEach { file ->
                val rel = file.relativeTo(functionsDir).invariantSeparatorsPath
                functions.add(rel)
            }

        ServerPlayNetworking.send(player, SendFunctionsPacket(functions))
    }
}
