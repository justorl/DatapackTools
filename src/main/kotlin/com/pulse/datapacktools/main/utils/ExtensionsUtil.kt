package com.pulse.datapacktools.main.utils

import net.minecraft.block.CommandBlock
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.CommandBlockBlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.RaycastContext
import net.minecraft.world.World

object ExtensionsUtil {
    fun PlayerEntity.getTargetBlock(): BlockHitResult? {
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

    fun BlockPos.getCommandChain(world: World): List<BlockEntity> {
        val chain = mutableListOf<BlockEntity>()
        var currentPos: BlockPos? = this

        while (currentPos != null) {
            val be = world.getBlockEntity(currentPos) ?: break
            if (be !is CommandBlockBlockEntity) break
            if (be.pos != this && be.commandBlockType != CommandBlockBlockEntity.Type.SEQUENCE) break

            if (be.pos != this) chain.add(be)

            val state = world.getBlockState(currentPos)
            val facing = state.get(CommandBlock.FACING)
            currentPos = currentPos.offset(facing)
        }

        return chain
    }

}