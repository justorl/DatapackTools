package com.pulse.datapacktools.packets.custom

import com.pulse.datapacktools.main.DatapackTools
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier

object FunctionCreatedPacket : CustomPayload {
    val ID = CustomPayload.Id<FunctionCreatedPacket>(Identifier.of(DatapackTools.ID, "function_created"))

    val CODEC: PacketCodec<RegistryByteBuf, FunctionCreatedPacket> =
        PacketCodec.unit(FunctionCreatedPacket)

    override fun getId(): CustomPayload.Id<out CustomPayload> = ID
}