package com.pulse.datapacktools.client.packets

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.minecraft.util.Identifier

object ClientPackets {
    val CONVERT_PACKET = Identifier("datapacktools", "convert_commands")
    val GET_FUNCTION_PACKET = Identifier("datapacktools", "get_function")
    val SAVE_FUNCTION_PACKET = Identifier("datapacktools", "save_function")
    val GET_FUNCTIONS_LIST_PACKET = Identifier("datapacktools", "get_functions_list")
    val CREATE_FUNCTION_PACKET = Identifier("datapacktools", "create_function")
    val FUNCTION_CREATED_PACKET = Identifier("datapacktools", "function_created")
    val GET_DATAPACKS_PACKET = Identifier("datapacktools", "get_datapacks")
    val GET_NAMESPACES_PACKET = Identifier("datapacktools", "get_namespaces")
    val SET_DATAPACK = Identifier("datapacktools", "set_datapack")
    val GET_CURRENT_PACKET = Identifier("datapacktools", "get_current")

    fun sendConvertPacket(functionName: String, changeCommand: Boolean) {
        val buf = PacketByteBufs.create()
        buf.writeString(functionName)
        buf.writeBoolean(changeCommand)
        ClientPlayNetworking.send(CONVERT_PACKET, buf)
    }

    fun sendGetFunctionPacket(functionName: String) {
        val buf = PacketByteBufs.create()
        buf.writeString(functionName)
        ClientPlayNetworking.send(GET_FUNCTION_PACKET, buf)
    }

    fun sendSaveFunctionPacket(functionName: String, content: String) {
        val buf = PacketByteBufs.create()
        buf.writeString(functionName)
        buf.writeString(content)
        ClientPlayNetworking.send(SAVE_FUNCTION_PACKET, buf)
    }

    fun sendGetFunctionsListPacket() {
        val buf = PacketByteBufs.create()
        ClientPlayNetworking.send(GET_FUNCTIONS_LIST_PACKET, buf)
    }

    fun sendCreateFunctionPacket(functionName: String) {
        val buf = PacketByteBufs.create()
        buf.writeString(functionName)
        ClientPlayNetworking.send(CREATE_FUNCTION_PACKET, buf)
    }

    fun sendGetDatapacksPacket() {
        val buf = PacketByteBufs.create()
        ClientPlayNetworking.send(GET_DATAPACKS_PACKET, buf)
    }

    fun sendGetNamespacesPacket(datapackName: String) {
        val buf = PacketByteBufs.create()
        buf.writeString(datapackName)
        ClientPlayNetworking.send(GET_NAMESPACES_PACKET, buf)
    }

    fun sendSetDatapackPacket(datapackName: String, namespaceName: String) {
        val buf = PacketByteBufs.create()
        buf.writeString(datapackName)
        buf.writeString(namespaceName)
        ClientPlayNetworking.send(SET_DATAPACK, buf)
    }

    fun sendGetCurrentPacket() {
        val buf = PacketByteBufs.create()
        ClientPlayNetworking.send(GET_CURRENT_PACKET, buf)
    }
}
