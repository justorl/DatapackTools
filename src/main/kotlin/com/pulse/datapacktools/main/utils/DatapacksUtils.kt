package com.pulse.datapacktools.main.utils

import com.google.gson.GsonBuilder
import com.pulse.datapacktools.main.config.ModConfig
import net.minecraft.server.network.ServerPlayerEntity
import java.io.File

object DatapacksUtils {
    private val gson = GsonBuilder().setPrettyPrinting().create()

    fun updateDatapack(player: ServerPlayerEntity) {
        val server = player.entityWorld.server
        val dataManager = server.dataPackManager
        val keyName = "file/${ModConfig.data.datapackName}"

        if (ModConfig.data.enableAutomaticDatapackReload) {
            dataManager.disable(keyName)
            dataManager.enable(keyName)
            server.reloadResources(mutableListOf(keyName))
        }
    }

    fun createDefaultDatapack(rootDir: File) {
        val convertedPackDir = File(rootDir, "datapacks/${ModConfig.data.datapackName}")
        val functionsDir = File(convertedPackDir, "data/${ModConfig.data.datapackNamespace}/function")

        if (!convertedPackDir.exists()) {
            convertedPackDir.mkdirs()
            functionsDir.mkdirs()

            val packGson = gson.toJson(mapOf(
                "pack" to mapOf(
                    "pack_format" to 18,
                    "description" to "Datapack by DatapackTools"
                )
            ))
            File(convertedPackDir, "pack.mcmeta").writeText(packGson)
        } else {
            if (!functionsDir.exists()) {
                functionsDir.mkdirs()
            }
        }
    }
}