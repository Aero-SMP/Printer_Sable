package dev.ryanhcode.sable.mixin.assembly;

import dev.ryanhcode.sable.api.SubLevelAssemblyHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.NeighborUpdater;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NeighborUpdater.class)
public interface NeighborUpdaterMixin {

    @Inject(method = "executeShapeUpdate", at = @At("HEAD"),cancellable = true)
    private static void sable$skipMovingBlockShapeUpdate(
            final LevelAccessor levelAccessor,
            final Direction direction,
            final BlockState state,
            final BlockPos pos,
            final BlockPos neighborPos,
            final int flags,
            final int recursionLevel,
            final CallbackInfo ci
    ){
        if(levelAccessor instanceof final Level level && SubLevelAssemblyHelper.isMovingBlock(level,pos)){
            ci.cancel();
        }
    }

   @Inject(method = "executeUpdate", at = @At("HEAD"),cancellable = true)
    private static void sable$skipMovingBlockNeighborUpdate(
           final Level level,
           final BlockState state,
           final BlockPos pos,
           final Block neighborBlock,
           final BlockPos neighborPos,
           final boolean movedByPiston,
           final CallbackInfo ci
            ){
        if(SubLevelAssemblyHelper.isMovingBlock(level,pos)){
            ci.cancel();
        }
    }
}
