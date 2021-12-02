package dev.lambdaurora.aurorasdeco.block.entity;

import com.google.common.base.Preconditions;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

public interface BlockEntityHelper {
    default void sync() {
        World world = ((BlockEntity) this).getWorld();
        Preconditions.checkNotNull(world); //Maintain distinct failure case from below
        if (!(world instanceof ServerWorld)) throw new IllegalStateException("Cannot call sync() on the logical client! Did you check world.isClient first?");

        ((ServerWorld) world).getChunkManager().markForUpdate(((BlockEntity) this).getPos());
    }
}
