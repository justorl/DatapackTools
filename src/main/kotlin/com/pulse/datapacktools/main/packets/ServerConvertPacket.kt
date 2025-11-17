package com.pulse.datapacktools.main.packets

import com.pulse.datapacktools.main.config.ModConfig
import com.pulse.datapacktools.main.utils.DatapacksUtils
import com.pulse.datapacktools.main.utils.ExtensionsUtil.getCommandChain
import com.pulse.datapacktools.main.utils.ExtensionsUtil.getTargetBlock
import com.pulse.datapacktools.main.utils.SessionUtils
import com.pulse.datapacktools.packets.custom.ConvertPacket
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.block.Blocks
import net.minecraft.block.entity.CommandBlockBlockEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.math.BlockPos
import java.io.File

object ServerConvertPacket {
    fun register() {
        ServerPlayNetworking.registerGlobalReceiver(ConvertPacket.Companion.ID) { packet, context ->
            context.server().execute {
                handle( context.player(), packet.functionName, packet.changeCommand)
            }
        }
    }

    fun handle(
        player: ServerPlayerEntity,
        functionName: String,
        changeCommand: Boolean
    ) {
        player.sendMessage(Text.translatable("message.datapacktools.convert.start").formatted(Formatting.GRAY, Formatting.ITALIC))

        val worldDir: File = SessionUtils.worldFolderPath?.toFile() ?: return
        DatapacksUtils.createDefaultDatapack(worldDir)

        val commandList = mutableListOf<String>()

        val functionsDir = File(
            worldDir,
            "datapacks/${ModConfig.data.datapackName}/data/${ModConfig.data.datapackNamespace}/function"
        )
        val functionDir = File(functionsDir, functionName.substringBeforeLast('/'))
        val functionFile = File(functionsDir, "${functionName}.mcfunction")

        if (functionFile.exists()) {
            if (ModConfig.data.enableWritingInExistingFunctions) {
                functionFile.readLines().forEach { commandList.add(it) }
            } else {
                player.sendMessage(
                    Text.translatable("message.datapacktools.convert.fail.already_exists").formatted(
                        Formatting.RED))
                return
            }
        }

        val pos: BlockPos = player.getTargetBlock()?.blockPos ?: return
        val block = player.entityWorld.getBlockEntity(pos)

        if (block is CommandBlockBlockEntity) {
            commandList.add(block.commandExecutor.command.removePrefix("/"))

            if (changeCommand) {
                block.commandExecutor.command = "function ${ModConfig.data.datapackNamespace}:$functionName"
                block.markDirty()
            }

            pos.getCommandChain(block.world!!).forEach {
                if (it is CommandBlockBlockEntity) commandList.add(it.commandExecutor.command.removePrefix("/"))
                if (changeCommand) it.world?.setBlockState(it.pos, Blocks.AIR.defaultState)
            }
        }

        if (!functionDir.exists() && functionName.contains("/")) functionDir.mkdirs()
        functionFile.writeText(commandList.joinToString("\n"))

        DatapacksUtils.updateDatapack(player)

        player.sendMessage(Text.translatable("message.datapacktools.convert.done", "${ModConfig.data.datapackName}:$functionName"), false)
    }
}