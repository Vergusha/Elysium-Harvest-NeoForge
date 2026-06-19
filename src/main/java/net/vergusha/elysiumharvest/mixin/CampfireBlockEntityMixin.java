package net.vergusha.elysiumharvest.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.vergusha.elysiumharvest.ElysiumHarvest;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CampfireBlockEntity.class)
public abstract class CampfireBlockEntityMixin {
    @Inject(method = "particleTick", at = @At("HEAD"), cancellable = true)
    private static void elysiumharvest$skipSmokeUnderQazan(
            Level level,
            BlockPos pos,
            BlockState state,
            CampfireBlockEntity blockEntity,
            CallbackInfo ci) {
        if (level.getBlockState(pos.above()).is(ElysiumHarvest.QAZAN.get())) {
            ci.cancel();
        }
    }
}
