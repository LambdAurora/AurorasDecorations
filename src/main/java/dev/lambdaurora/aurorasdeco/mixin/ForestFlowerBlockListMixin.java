package dev.lambdaurora.aurorasdeco.mixin;

import dev.lambdaurora.aurorasdeco.registry.AurorasDecoRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.world.gen.feature.VegetationConfiguredFeatures;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Arrays;

@Mixin(VegetationConfiguredFeatures.class)
public class ForestFlowerBlockListMixin {
    @ModifyArg(method = "method_39726", at = @At(value = "INVOKE", target = "Ljava/util/List;of([Ljava/lang/Object;)Ljava/util/List;"), remap = false)
    private static Object[] addAurorasDecoFlowers(Object[] states) {
        var newStates = Arrays.copyOf((BlockState[]) states, states.length+1);
        newStates[states.length] = AurorasDecoRegistry.DAFFODIL.getDefaultState();
        return newStates;
    }
}
