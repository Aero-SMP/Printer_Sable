package dev.ryanhcode.sable.mixin.block_decal_render;

import dev.ryanhcode.sable.companion.SableCompanion;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {

    @Inject(
            method = "destroyBlockProgress",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V"
            ),
            cancellable = true
    )
    private void sable$destroyBlockProgressSubLevelAware(int breakerId, BlockPos pos, int progress, CallbackInfo ci) {
        ServerLevel level = (ServerLevel) (Object) this;
        ci.cancel();
        sable$broadcastBlockBreakProgressSubLevelAware(level, breakerId, pos, progress);
    }

    @Unique
    private static void sable$broadcastBlockBreakProgressSubLevelAware(
            ServerLevel level,
            int breakerId,
            BlockPos pos,
            int progress
    ) {
        for (ServerPlayer serverplayer : level.getServer().getPlayerList().getPlayers()) {
            if (serverplayer != null && serverplayer.level() == level && serverplayer.getId() != breakerId) {
                Vec3 playerPos = serverplayer.position();
                Vec3 blockPos = Vec3.atCenterOf(pos);

                double distSqr = SableCompanion.INSTANCE.distanceSquaredWithSubLevels(level, playerPos, blockPos);
                if (distSqr < 1024.0) {
                    serverplayer.connection.send(new ClientboundBlockDestructionPacket(breakerId, pos, progress));
                }
            }
        }
    }
}