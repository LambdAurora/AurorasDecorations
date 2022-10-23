package dev.lambdaurora.aurorasdeco.block;

import net.minecraft.block.AbstractBlock;

/**
 * Represents a way to inject properties from other blocks into a specific block.
 *
 * @author LambdAurora
 * @version 1.0.0-beta.9
 * @since 1.0.0-beta.9
 */
public final class BlockPropertiesInjector {
	private static final ThreadLocal<InjectData> DATA = new ThreadLocal<>();

	public static <S extends AbstractBlock.Settings> S inject(S settings, InjectData data) {
		DATA.set(data);
		return settings;
	}

	public static <D extends InjectData> D getInjectedData(Class<? extends D> dataClass) {
		return dataClass.cast(DATA.get());
	}

	public interface InjectData {
	}
}
