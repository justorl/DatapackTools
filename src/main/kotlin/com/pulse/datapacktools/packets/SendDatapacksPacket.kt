package com.pulse.datapacktools.packets

import com.pulse.datapacktools.main.DatapackTools
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier

data class SendDatapacksPacket(val datapacks: List<String>) : CustomPayload {
    companion object {
        val ID = CustomPayload.Id<SendDatapacksPacket>(Identifier.of(DatapackTools.ID, "send_datapacks"))
        val CODEC: PacketCodec<RegistryByteBuf, SendDatapacksPacket> = PacketCodec.tuple(
            PacketCodecs.STRING.collect(PacketCodecs.toList()),
            SendDatapacksPacket::datapacks,
            ::SendDatapacksPacket
        )
    }

    override fun getId(): CustomPayload.Id<out CustomPayload> = ID
}