package com.pulse.datapacktools.client.packets

import com.pulse.datapacktools.packets.FunctionCreatedPacket
import com.pulse.datapacktools.packets.SendCurrentPacket
import com.pulse.datapacktools.packets.SendDatapacksPacket
import com.pulse.datapacktools.packets.SendFunctionPacket
import com.pulse.datapacktools.packets.SendFunctionsPacket
import com.pulse.datapacktools.packets.SendNamespacesPacket
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry

object ClientPackets {
    fun register() {
        PayloadTypeRegistry.playS2C().register(SendFunctionsPacket.ID, SendFunctionsPacket.CODEC)
        PayloadTypeRegistry.playS2C().register(SendFunctionPacket.ID, SendFunctionPacket.CODEC)
        PayloadTypeRegistry.playS2C().register(SendCurrentPacket.ID, SendCurrentPacket.CODEC)
        PayloadTypeRegistry.playS2C().register(SendDatapacksPacket.ID, SendDatapacksPacket.CODEC)
        PayloadTypeRegistry.playS2C().register(SendNamespacesPacket.ID, SendNamespacesPacket.CODEC)
        PayloadTypeRegistry.playS2C().register(FunctionCreatedPacket.ID, FunctionCreatedPacket.CODEC)
    }
}