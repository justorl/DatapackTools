package com.pulse.datapacktools.main.utils

import com.google.gson.GsonBuilder
import java.io.File

object DatapacksUtils {
    const val DATAPACK_NAME = "converted"
    private val gson = GsonBuilder().setPrettyPrinting().create()

    fun createDefaultDatapack(rootDir: File) {
        val convertedPackDir = File(rootDir, "datapacks/${DATAPACK_NAME}")
        val functionsDir = File(convertedPackDir, "data/$DATAPACK_NAME/functions")

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