package com.pulse.datapacktools.packets.custom

import com.pulse.datapacktools.main.DatapackTools
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier

object GetCurrentPacket : CustomPayload {
    val ID = CustomPayload.Id<GetCurrentPacket>(Identifier.of(DatapackTools.ID, "get_current"))

    val CODEC: PacketCodec<RegistryByteBuf, GetCurrentPacket> =
        PacketCodec.unit(GetCurrentPacket)

    override fun getId(): CustomPayload.Id<out CustomPayload> = ID
}