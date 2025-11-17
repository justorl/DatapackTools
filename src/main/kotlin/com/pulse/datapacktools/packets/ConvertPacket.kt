package com.pulse.datapacktools.packets

import com.pulse.datapacktools.main.DatapackTools
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier

data class ConvertPacket(val functionName: String, val changeCommand: Boolean) : CustomPayload {
    companion object {
        val ID = CustomPayload.Id<ConvertPacket>(Identifier.of(DatapackTools.ID, "convert"))
        val CODEC: PacketCodec<RegistryByteBuf, ConvertPacket> = PacketCodec.tuple(
            PacketCodecs.STRING, ConvertPacket::functionName,
            PacketCodecs.BOOLEAN, ConvertPacket::changeCommand,
            ::ConvertPacket
        )
    }

    override fun getId(): CustomPayload.Id<out CustomPayload> = ID
}