package dev.lambdaurora.aurorasdeco.block;

import com.google.common.collect.ImmutableList;
import net.minecraft.util.StringIdentifiable;

import java.util.List;

/**
 * Represents a part type.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public enum PartType implements StringIdentifiable {
    BOTTOM("bottom"),
    TOP("top"),
    DOUBLE("double");

    private static final List<PartType> VALUES = ImmutableList.copyOf(values());

    private final String name;

    PartType(String name) {
        this.name = name;
    }

    @Override
    public String asString() {
        return this.name;
    }

    public static List<PartType> getValues() {
        return VALUES;
    }
}
