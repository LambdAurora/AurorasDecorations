package aurorasdeco;

import java.util.ArrayList;
import java.util.List;

public record MinecraftVersions(String version, List<String> supported) {
	public MinecraftVersions(String version, String... supported) {
		this(version, List.of(supported));
	}

	public List<String> all() {
		var versions = new ArrayList<String>();
		versions.add(this.version);
		versions.addAll(this.supported);
		return versions;
	}
}
