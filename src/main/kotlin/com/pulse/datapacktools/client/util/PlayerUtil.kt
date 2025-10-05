package com.pulse.datapacktools.client.util

import net.minecraft.block.entity.CommandBlockBlockEntity
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.screen.NamedScreenHandlerFactory
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.Vec3d
import net.minecraft.world.RaycastContext

object PlayerUtil {
    fun ClientPlayerEntity.getTargetBlock(): BlockHitResult? {
        val eyePos: Vec3d = this.getCameraPosVec(1.0f)
        val lookVec: Vec3d = this.getRotationVec(1.0f)
        val reachVec: Vec3d = eyePos.add(lookVec.multiply(10.0))

        val rayResult: BlockHitResult = this.world.raycast(
            RaycastContext(
                eyePos,
                reachVec,
                RaycastContext.ShapeType.OUTLINE,
                RaycastContext.FluidHandling.NONE,
                this
            )
        )

        return if (rayResult.type == HitResult.Type.BLOCK) rayResult else null
    }

    fun getCommandBlockInFront(): CommandBlockBlockEntity? {
        val client = MinecraftClient.getInstance()
        val player = client.player ?: return null
        val world = client.world ?: return null

        val targetHit = player.getTargetBlock()
        if (targetHit != null) {
            val be = world.getBlockEntity(targetHit.blockPos)
            if (be is CommandBlockBlockEntity) {
                return be
            }
        }
        return null
    }

    fun openCommandBlockInFront() {
        val client = MinecraftClient.getInstance()
        val player = client.player ?: return
        val be = getCommandBlockInFront() ?: return
        player.openCommandBlockScreen(be)
    }
}