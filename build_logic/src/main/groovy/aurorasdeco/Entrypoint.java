package aurorasdeco;

import org.gradle.api.Named;
import org.gradle.api.tasks.Input;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.json5.JsonWriter;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;

public record Entrypoint(String target, boolean enabled) implements Named, Serializable {
	@Serial
	private static final long serialVersionUID = 1L;

	@Input
	@Override
	public @NotNull String getName() {
		return this.target;
	}

	@Input
	public boolean enabled() {
		return this.enabled;
	}

	public void write(JsonWriter writer) throws IOException {
		if (this.enabled) {
			writer.value(this.target);
		}
	}
}
