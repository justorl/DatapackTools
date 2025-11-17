package com.pulse.datapacktools.packets

import com.pulse.datapacktools.main.DatapackTools
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier

data class GetNamespacesPacket(val datapackName: String) : CustomPayload {
    companion object {
        val ID = CustomPayload.Id<GetNamespacesPacket>(Identifier.of(DatapackTools.ID, "get_namespaces"))
        val CODEC: PacketCodec<RegistryByteBuf, GetNamespacesPacket> = PacketCodec.tuple(
            PacketCodecs.STRING,
            GetNamespacesPacket::datapackName,
            ::GetNamespacesPacket
        )
    }

    override fun getId(): CustomPayload.Id<out CustomPayload> = ID
}