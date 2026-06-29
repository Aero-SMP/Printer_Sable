package dev.ryanhcode.sable.api.physics.callback;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.joml.Vector3d;

import java.util.Objects;

public final class LevelBoundCollisionCallback implements BlockSubLevelCollisionCallback {
    private final ServerLevel level;
    private final LevelAwareBlockSubLevelCollisionCallback delegate;

    public LevelBoundCollisionCallback(final ServerLevel level, final LevelAwareBlockSubLevelCollisionCallback delegate) {
        this.level = Objects.requireNonNull(level, "level");
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    @Override
    public CollisionResult sable$onCollision(final BlockPos pos, final Vector3d hitPos, final double impactVelocity){
        return this.delegate.sable$onCollision(this.level, pos, hitPos, impactVelocity);
    }
}
