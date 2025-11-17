package com.pulse.datapacktools.packets

import com.pulse.datapacktools.main.DatapackTools
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier

data class SetDatapackPacket(val datapackName: String, val namespaceName: String) : CustomPayload {
    companion object {
        val ID = CustomPayload.Id<SetDatapackPacket>(Identifier.of(DatapackTools.ID, "set_datapack"))
        val CODEC: PacketCodec<RegistryByteBuf, SetDatapackPacket> = PacketCodec.tuple(
            PacketCodecs.STRING, SetDatapackPacket::datapackName,
            PacketCodecs.STRING, SetDatapackPacket::namespaceName,
            ::SetDatapackPacket
        )
    }

    override fun getId(): CustomPayload.Id<out CustomPayload> = ID
}