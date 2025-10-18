package com.pulse.datapacktools.main.utils

import com.google.gson.GsonBuilder
import com.pulse.datapacktools.main.config.ModConfig
import java.io.File

object DatapacksUtils {
    private val gson = GsonBuilder().setPrettyPrinting().create()

    fun createDefaultDatapack(rootDir: File) {
        val convertedPackDir = File(rootDir, "datapacks/${ModConfig.data.defaultDatapack}")
        val functionsDir = File(convertedPackDir, "data/${ModConfig.data.defaultDatapack}/functions")

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