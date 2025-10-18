package com.pulse.datapacktools.main.config

import com.google.gson.GsonBuilder

import java.io.File
import java.io.FileReader
import java.io.FileWriter

object ModConfig {
    val configFile = File("config/datapacktools.json")

    private val gson = GsonBuilder().setPrettyPrinting().create()
    var data: ConfigData = ConfigData()

    fun load() {
        if (configFile.exists()) {
            data = gson.fromJson(FileReader(configFile), ConfigData::class.java)
        } else {
            save()
        }
    }

    fun save() {
        configFile.parentFile.mkdirs()
        gson.toJson(data, FileWriter(configFile))
    }
}