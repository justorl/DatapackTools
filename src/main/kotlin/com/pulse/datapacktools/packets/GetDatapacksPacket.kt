package com.pulse.datapacktools.packets

import com.pulse.datapacktools.main.DatapackTools
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier

object GetDatapacksPacket : CustomPayload {
    val ID = CustomPayload.Id<GetDatapacksPacket>(Identifier.of(DatapackTools.ID, "get_datapacks"))

    val CODEC: PacketCodec<RegistryByteBuf, GetDatapacksPacket> =
        PacketCodec.unit(GetDatapacksPacket)

    override fun getId(): CustomPayload.Id<out CustomPayload> = ID
}