package com.pulse.datapacktools.main.packets

object ServerPackets {
    fun registerAll() {
        ConvertPacket.register()
        GetFunctionPacket.register()
        SaveFunctionPacket.register()
        GetFunctionsListPacket.register()
        CreateFunctionPacket.register()
        GetDatapacksPacket.register()
        SetDefaultDatapackPacket.register()
    }
}