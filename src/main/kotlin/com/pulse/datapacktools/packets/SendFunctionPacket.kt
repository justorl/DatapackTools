package com.pulse.datapacktools.packets

import com.pulse.datapacktools.main.DatapackTools
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier

data class SendFunctionPacket(val functionName: String, val content: String) : CustomPayload {
    companion object {
        val ID = CustomPayload.Id<SendFunctionPacket>(Identifier.of(DatapackTools.ID, "send_function"))
        val CODEC: PacketCodec<RegistryByteBuf, SendFunctionPacket> = PacketCodec.tuple(
            PacketCodecs.STRING, SendFunctionPacket::functionName,
            PacketCodecs.STRING, SendFunctionPacket::content,
            ::SendFunctionPacket
        )
    }

    override fun getId(): CustomPayload.Id<out CustomPayload> = ID
}