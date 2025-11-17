package com.pulse.datapacktools.main.packets

import com.pulse.datapacktools.main.packets.custom.*

object ServerPackets {
    fun register() {
        ServerConvertPacket.register()
        ServerGetFunctionPacket.register()
        ServerSaveFunctionPacket.register()
        ServerGetFunctionsPacket.register()
        ServerCreateFunctionPacket.register()
        ServerGetDatapacksPacket.register()
        ServerSetDatapackPacket.register()
        ServerGetNamespacesPacket.register()
        ServerGetCurrentPacket.register()
    }
}