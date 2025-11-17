package com.pulse.datapacktools.packets

import com.pulse.datapacktools.main.DatapackTools
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier

data class SendNamespacesPacket(val namespaces: List<String>) : CustomPayload {
    companion object {
        val ID = CustomPayload.Id<SendNamespacesPacket>(Identifier.of(DatapackTools.ID, "send_namespaces"))
        val CODEC: PacketCodec<RegistryByteBuf, SendNamespacesPacket> = PacketCodec.tuple(
            PacketCodecs.STRING.collect(PacketCodecs.toList()),
            SendNamespacesPacket::namespaces,
            ::SendNamespacesPacket
        )
    }

    override fun getId(): CustomPayload.Id<out CustomPayload> = ID
}