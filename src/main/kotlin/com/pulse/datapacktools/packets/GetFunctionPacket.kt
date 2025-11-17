package com.pulse.datapacktools.packets

import com.pulse.datapacktools.main.DatapackTools
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier

data class GetFunctionPacket(val functionName: String) : CustomPayload {
    companion object {
        val ID = CustomPayload.Id<GetFunctionPacket>(Identifier.of(DatapackTools.ID, "get_function"))
        val CODEC: PacketCodec<RegistryByteBuf, GetFunctionPacket> = PacketCodec.tuple(
            PacketCodecs.STRING,
            GetFunctionPacket::functionName,
            ::GetFunctionPacket
        )
    }

    override fun getId(): CustomPayload.Id<out CustomPayload> = ID
}