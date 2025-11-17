package com.pulse.datapacktools.packets.custom

import com.pulse.datapacktools.main.DatapackTools
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier

data class CreateFunctionPacket(val functionName: String) : CustomPayload {
    companion object {
        val ID = CustomPayload.Id<CreateFunctionPacket>(Identifier.of(DatapackTools.ID, "create_function"))
        val CODEC: PacketCodec<RegistryByteBuf, CreateFunctionPacket> = PacketCodec.tuple(
            PacketCodecs.STRING,
            CreateFunctionPacket::functionName,
            ::CreateFunctionPacket
        )
    }

    override fun getId(): CustomPayload.Id<out CustomPayload> = ID
}