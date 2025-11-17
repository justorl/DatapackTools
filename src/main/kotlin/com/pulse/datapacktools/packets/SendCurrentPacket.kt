package com.pulse.datapacktools.packets

import com.pulse.datapacktools.main.DatapackTools
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier

data class SendCurrentPacket(val current: String) : CustomPayload {
    companion object {
        val ID = CustomPayload.Id<SendCurrentPacket>(Identifier.of(DatapackTools.ID, "send_current"))
        val CODEC: PacketCodec<RegistryByteBuf, SendCurrentPacket> = PacketCodec.tuple(
            PacketCodecs.STRING,
            SendCurrentPacket::current,
            ::SendCurrentPacket
        )
    }

    override fun getId(): CustomPayload.Id<out CustomPayload> = ID
}