package dev.lambdaurora.aurorasdeco.block.big_flower_pot;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import org.jetbrains.annotations.Nullable;

public class PottedPlantType {
    private final String id;
    private final @Nullable Block plant;
    private final @Nullable Item item;

    public PottedPlantType(String id, @Nullable Block plant, @Nullable Item item) {
        this.id = id;
        this.plant = plant;
        this.item = item;
    }

    public String getId() {
        return this.id;
    }

    public @Nullable Block getPlant() {
        return this.plant;
    }

    public @Nullable Item getItem() {
        return this.item;
    }
}
