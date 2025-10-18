package com.pulse.datapacktools.main.config

import com.google.gson.GsonBuilder

import java.io.File
import java.io.FileReader
import java.io.FileWriter

object ModConfig {
    val configFile = File("config/datapacktools.json")

    private val gson = GsonBuilder().setPrettyPrinting().create()
    var data: ConfigData = ConfigData()

    fun save() {
        val newData = gson.toJson(data)
        configFile.writeText(newData)
    }

    fun load() {
        if (configFile.exists() && configFile.readText().isNotEmpty()) {
            try {
                FileReader(configFile).use { reader ->
                    val loadedData = gson.fromJson(reader, ConfigData::class.java)
                    data = loadedData
                }
            } catch (e: Exception) {
                init()
            }
        } else {
            init()
        }
    }

    fun init() {
        data = ConfigData()

        configFile.parentFile.mkdirs()
        FileWriter(configFile).use { writer ->
            gson.toJson(data, writer)
        }
    }
}