package com.pulse.datapacktools.main.packets

import com.pulse.datapacktools.main.config.ModConfig
import com.pulse.datapacktools.main.utils.DatapacksUtils.createDefaultDatapack
import com.pulse.datapacktools.main.utils.ExtensionsUtil.getCommandChain
import com.pulse.datapacktools.main.utils.ExtensionsUtil.getTargetBlock
import com.pulse.datapacktools.main.utils.SessionUtils
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.block.Blocks
import net.minecraft.block.entity.CommandBlockBlockEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import java.io.File

object ConvertPacket {
    val ID = Identifier("datapacktools", "convert_commands")

    fun register() {
        ServerPlayNetworking.registerGlobalReceiver(ID) { server, player, handler, buf, responseSender ->
            val functionName = buf.readString()
            val changeCommand = buf.readBoolean()

            if (player.hasPermissionLevel(4)) {
                server.execute {
                    handle(player, functionName, changeCommand)
                }
            } else {
                player.sendMessage(Text.translatable("message.datapacktool.convert.fail.no_permission").formatted(Formatting.RED))
            }
        }
    }

    fun handle(
        player: ServerPlayerEntity,
        functionName: String,
        changeCommand: Boolean
    ) {
        player.sendMessage(Text.translatable("message.datapacktools.convert.start").formatted(Formatting.GRAY, Formatting.ITALIC))

        val server = player.server
        val worldDir: File = SessionUtils.worldFolderPath?.toFile() ?: return
        val commandList = mutableListOf<String>()
        createDefaultDatapack(worldDir)
        val functionsDir = File(worldDir, "datapacks/${ModConfig.data.defaultDatapack}/data/${ModConfig.data.defaultDatapack}/functions")

        if (File(functionsDir, "${functionName}.mcfunction").exists()) {
            player.sendMessage(Text.translatable("message.datapacktools.convert.fail.already_exists").formatted(Formatting.RED))
            return
        }

        val pos: BlockPos = player.getTargetBlock()?.blockPos ?: return
        val block = player.world.getBlockEntity(pos)

        if (block is CommandBlockBlockEntity) {
            commandList.add(block.commandExecutor.command.removePrefix("/"))

            if (changeCommand) {
                block.commandExecutor.command = "function ${ModConfig.data.defaultDatapack}:$functionName"
                block.markDirty()
            }

            pos.getCommandChain(block.world!!).forEach {
                if (it is CommandBlockBlockEntity) commandList.add(it.commandExecutor.command.removePrefix("/"))
                if (changeCommand) it.world?.setBlockState(it.pos, Blocks.AIR.defaultState)
            }
        }

        File(functionsDir, "${functionName}.mcfunction").apply {
            writeText(commandList.joinToString("\n"))
        }

        val dataManager = server.dataPackManager
        val keyName = "file/${ModConfig.data.defaultDatapack}"

        if (ModConfig.data.enableAutomaticDatapackReload) {
            dataManager.enabledNames.toMutableList().remove(keyName)
            dataManager.enabledNames.toMutableList().add(keyName)
            server.reloadResources(dataManager.enabledNames)
        }

        player.sendMessage(Text.translatable("message.datapacktools.convert.done", "${ModConfig.data.defaultDatapack}:$functionName"), false)
    }
}