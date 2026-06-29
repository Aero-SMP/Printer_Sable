package dev.ryanhcode.sable.api.physics.callback;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.joml.Vector3d;

public interface LevelAwareBlockSubLevelCollisionCallback extends BlockSubLevelCollisionCallback {
    @Override
    default CollisionResult sable$onCollision(final BlockPos pos, final Vector3d hitPos, final double impactVelocity){
        throw new IllegalStateException("Level aware callback called without ServerLevel");
    }

    CollisionResult sable$onCollision(ServerLevel level, BlockPos pos, Vector3d hitPos, double impactVelocity);
}
