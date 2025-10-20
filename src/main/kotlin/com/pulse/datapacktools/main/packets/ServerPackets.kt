package com.pulse.datapacktools.main.packets

object ServerPackets {
    fun registerAll() {
        ConvertPacket.register()
        GetFunctionPacket.register()
        SaveFunctionPacket.register()
        GetFunctionsListPacket.register()
        CreateFunctionPacket.register()
        GetDatapacksPacket.register()
        SetDatapackPacket.register()
        GetNamespacesPacket.register()
    }
}