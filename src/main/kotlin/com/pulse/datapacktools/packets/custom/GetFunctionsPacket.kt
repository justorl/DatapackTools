package com.pulse.datapacktools.packets.custom

import com.pulse.datapacktools.main.DatapackTools
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier

object GetFunctionsPacket : CustomPayload {
    val ID = CustomPayload.Id<GetFunctionsPacket>(Identifier.of(DatapackTools.ID, "get_functions"))

    val CODEC: PacketCodec<RegistryByteBuf, GetFunctionsPacket> =
        PacketCodec.unit(GetFunctionsPacket)

    override fun getId(): CustomPayload.Id<out CustomPayload> = ID
}