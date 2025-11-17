package com.pulse.datapacktools.packets

import com.pulse.datapacktools.main.DatapackTools
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier

data class SaveFunctionPacket(val functionName: String, val content: String) : CustomPayload {
    companion object {
        val ID = CustomPayload.Id<SaveFunctionPacket>(Identifier.of(DatapackTools.ID, "save_function"))
        val CODEC: PacketCodec<RegistryByteBuf, SaveFunctionPacket> = PacketCodec.tuple(
            PacketCodecs.STRING, SaveFunctionPacket::functionName,
            PacketCodecs.STRING, SaveFunctionPacket::content,
            ::SaveFunctionPacket
        )
    }

    override fun getId(): CustomPayload.Id<out CustomPayload> = ID
}