package dev.ryanhcode.sable.physics.callback;

import dev.ryanhcode.sable.api.physics.callback.LevelAwareBlockSubLevelCollisionCallback;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.mixinterface.block_properties.BlockStateExtension;
import dev.ryanhcode.sable.physics.config.block_properties.PhysicsBlockPropertyTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3d;

public class FragileBlockCallback implements LevelAwareBlockSubLevelCollisionCallback {

    public static final FragileBlockCallback INSTANCE = new FragileBlockCallback();

    protected FragileBlockCallback() {}

    public double getTriggerVelocity() {
        return 4.0;
    }

    @Override
    public CollisionResult sable$onCollision(final ServerLevel level, final BlockPos pos, final Vector3d hitPos, final double impactVelocity) {
        final double triggerVelocity = this.getTriggerVelocity();

        if (impactVelocity * impactVelocity < triggerVelocity * triggerVelocity) {
            return CollisionResult.NONE;
        }

        final ServerSubLevelContainer container = ServerSubLevelContainer.getContainer(level);
        if(container == null){
            return CollisionResult.NONE;
        }

        final BlockPos queuedPos = pos.immutable();
        final Vector3d queuedHitPos = new Vector3d(hitPos);

        container.queueSimulationMainThreadTask(mainThreadContainer->{
            final ServerLevel mainThreadLevel = mainThreadContainer.getLevel();

            // Double check that we're actually fragile before breaking (in-case pipeline gave us a slightly off collision position)
            final BlockState state = mainThreadLevel.getBlockState(queuedPos);

            if (state.getBlock() instanceof LeavesBlock && state.getValue(LeavesBlock.PERSISTENT))
                return;

            if (this.shouldTriggerFor(state)) {
                this.onHit(mainThreadLevel, queuedPos, state, queuedHitPos);
            }
        });

        return new CollisionResult(JOMLConversion.ZERO, this.removesCollisionOnHit());
    }

    protected boolean removesCollisionOnHit(){
        return true;
    }

    public boolean shouldTriggerFor(final BlockState state) {
        return ((BlockStateExtension) state).sable$getProperty(PhysicsBlockPropertyTypes.FRAGILE.get());
    }

    public void onHit(final ServerLevel level, final BlockPos pos, final BlockState state, final Vector3d hitPos) {
        level.destroyBlock(pos, true);

        // Melt ice on destruction
        if (state.getBlock() instanceof IceBlock) {
            final BlockState belowState = level.getBlockState(pos.below());

            if (belowState.blocksMotion() || belowState.liquid()) {
                if(level.dimension() == ServerLevel.NETHER){
                    level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                }
                else{
                    level.setBlockAndUpdate(pos, IceBlock.meltsInto());
                }
            }
        }
    }
}
