package com.pulse.datapacktools.client.packets

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.minecraft.util.Identifier

object ClientPackets {
    val CONVERT_PACKET = Identifier("datapacktools", "convert_commands")

    fun sendConvertPacket(functionName: String, changeCommand: Boolean) {
        val buf = PacketByteBufs.create()
        buf.writeString(functionName)
        buf.writeBoolean(changeCommand)
        ClientPlayNetworking.send(CONVERT_PACKET, buf)
    }
}
