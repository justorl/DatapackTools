package com.pulse.datapacktools.packets.custom

import com.pulse.datapacktools.main.DatapackTools
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier

data class SendFunctionsPacket(val functions: List<String>) : CustomPayload {
    companion object {
        val ID = CustomPayload.Id<SendFunctionsPacket>(Identifier.of(DatapackTools.Companion.ID, "send_functions"))
        val CODEC: PacketCodec<RegistryByteBuf, SendFunctionsPacket> = PacketCodec.tuple(
            PacketCodecs.STRING.collect(PacketCodecs.toList()),
            SendFunctionsPacket::functions,
            ::SendFunctionsPacket
        )
    }

    override fun getId(): CustomPayload.Id<out CustomPayload> = ID
}