package com.pulse.datapacktools.packets

import com.pulse.datapacktools.main.packets.ServerConvertPacket
import com.pulse.datapacktools.main.packets.ServerCreateFunctionPacket
import com.pulse.datapacktools.main.packets.ServerGetCurrentPacket
import com.pulse.datapacktools.main.packets.ServerGetDatapacksPacket
import com.pulse.datapacktools.main.packets.ServerGetFunctionPacket
import com.pulse.datapacktools.main.packets.ServerGetFunctionsPacket
import com.pulse.datapacktools.main.packets.ServerGetNamespacesPacket
import com.pulse.datapacktools.main.packets.ServerSaveFunctionPacket
import com.pulse.datapacktools.main.packets.ServerSetDatapackPacket
import com.pulse.datapacktools.packets.custom.ConvertPacket
import com.pulse.datapacktools.packets.custom.CreateFunctionPacket
import com.pulse.datapacktools.packets.custom.FunctionCreatedPacket
import com.pulse.datapacktools.packets.custom.GetCurrentPacket
import com.pulse.datapacktools.packets.custom.GetDatapacksPacket
import com.pulse.datapacktools.packets.custom.GetFunctionPacket
import com.pulse.datapacktools.packets.custom.GetFunctionsPacket
import com.pulse.datapacktools.packets.custom.GetNamespacesPacket
import com.pulse.datapacktools.packets.custom.SaveFunctionPacket
import com.pulse.datapacktools.packets.custom.SendCurrentPacket
import com.pulse.datapacktools.packets.custom.SendDatapacksPacket
import com.pulse.datapacktools.packets.custom.SendFunctionPacket
import com.pulse.datapacktools.packets.custom.SendFunctionsPacket
import com.pulse.datapacktools.packets.custom.SendNamespacesPacket
import com.pulse.datapacktools.packets.custom.SetDatapackPacket
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry

object Packets {
    fun register() {
        PayloadTypeRegistry.playS2C().register(SendFunctionsPacket.ID, SendFunctionsPacket.CODEC)
        PayloadTypeRegistry.playS2C().register(SendFunctionPacket.ID, SendFunctionPacket.CODEC)
        PayloadTypeRegistry.playS2C().register(SendCurrentPacket.ID, SendCurrentPacket.CODEC)
        PayloadTypeRegistry.playS2C().register(SendDatapacksPacket.ID, SendDatapacksPacket.CODEC)
        PayloadTypeRegistry.playS2C().register(SendNamespacesPacket.ID, SendNamespacesPacket.CODEC)
        PayloadTypeRegistry.playS2C().register(FunctionCreatedPacket.ID, FunctionCreatedPacket.CODEC)

        PayloadTypeRegistry.playC2S().register(ConvertPacket.ID, ConvertPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(GetNamespacesPacket.ID, GetNamespacesPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(CreateFunctionPacket.ID, CreateFunctionPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(GetCurrentPacket.ID, GetCurrentPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(GetDatapacksPacket.ID, GetDatapacksPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(GetFunctionPacket.ID, GetFunctionPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(GetFunctionsPacket.ID, GetFunctionsPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(SaveFunctionPacket.ID, SaveFunctionPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(SetDatapackPacket.ID, SetDatapackPacket.CODEC);

        ServerConvertPacket.register()
        ServerGetFunctionPacket.register()
        ServerSaveFunctionPacket.register()
        ServerGetFunctionsPacket.register()
        ServerCreateFunctionPacket.register()
        ServerGetDatapacksPacket.register()
        ServerSetDatapackPacket.register()
        ServerGetNamespacesPacket.register()
        ServerGetCurrentPacket.register()
    }
}