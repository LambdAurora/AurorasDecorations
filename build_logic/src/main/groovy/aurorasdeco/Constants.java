package aurorasdeco;

import java.util.List;

public final class Constants {
	public static final String NAMESPACE = "aurorasdeco";
	public static final String NAME = "Aurora's Decorations";
	public static final String DESCRIPTION = "A decorations-focused mod which tries to have a twist for its different features.";
	public static final List<Contributor> CONTRIBUTORS = List.of(
			new Contributor("LambdAurora", "Author"),
			new Contributor("april", "Artist"),
			new Contributor("harpsi", "Artist"),
			new Contributor("Kat", "Artist"),
			new Contributor("unascribed", "Artist")
	);
	public static final String LICENSE = "LGPL-3.0-only";
	public static final String ICON_PATH = "assets/" + NAMESPACE + "/icon.png";

	/* VERSIONS */

	public static final MinecraftVersions MINECRAFT_VERSION = new MinecraftVersions("1.20.1", "1.20");
	public static final int MAPPINGS = 19;
	public static final String LOADER_VERSION = "0.19.1";
	public static final String QFAPI_VERSION = "7.1.1+0.86.1";
	public static final String QSL_VERSION = "6.1.1";
	public static final int JAVA_VERSION = 17;
	// Dependencies
	public static final String TERRAFORM_WOOD_API_VERSION = "7.0.1";
	public static final String TRINKETS_VERSION = "3.7.1";
	public static final String EMI_VERSION = "1.0.18+1.20.1";

	public record Contributor(String name, String role) {}

	public static final class Links {
		public static final String WEBSITE = "https://lambdaurora.dev/AurorasDecorations/";
		public static final String SOURCES = "https://github.com/LambdAurora/AurorasDecorations";
		public static final String ISSUES = SOURCES + "/issues";
		public static final String CURSEFORGE = "https://www.curseforge.com/minecraft/mc-mods/aurorasdecorations";
		public static final String DISCORD = "https://discord.lambdaurora.dev/";
		public static final String GITHUB_RELEASES = SOURCES + "/releases";
		public static final String MODRINTH = "https://modrinth.com/mod/aurorasdecorations";
		public static final String TWITTER = "https://twitter.com/LambdAurora";
	}
}
