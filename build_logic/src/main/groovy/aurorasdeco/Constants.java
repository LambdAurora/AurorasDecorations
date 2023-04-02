package aurorasdeco;

import java.util.List;

public final class Constants {
	public static final String NAMESPACE = "aurorasdeco";
	public static final String NAME = "Aurora's Decorations";
	public static final String DESCRIPTION = "A decorations-focused mod which tries to have a twist for its different features.";
	public static final List<Contributor> CONTRIBUTORS = List.of(
			new Contributor("Aurora (Lambda Foxes)", "Author"),
			new Contributor("Lavender (Lambda Foxes)", "Author"),
			new Contributor("april", "Artist"),
			new Contributor("harpsi", "Artist"),
			new Contributor("Kat", "Artist"),
			new Contributor("unascribed", "Artist")
	);
	public static final String WEBSITE = "https://lambdaurora.dev/AurorasDecorations/";
	public static final String SOURCES = "https://github.com/LambdAurora/AurorasDecorations";
	public static final String ISSUES = SOURCES + "/issues";
	public static final String LICENSE = "LGPL-3.0-only";
	public static final String ICON_PATH = "assets/" + NAMESPACE + "/icon.png";

	/* VERSIONS */

	public static final String MINECRAFT_VERSION = "1.19.4";
	public static final int MAPPINGS = 10;
	public static final String LOADER_VERSION = "0.18.6";
	public static final String QFAPI_VERSION = "6.0.0-beta.3+0.76.0";
	public static final String QSL_VERSION = "5.0.0-beta.2";
	public static final int JAVA_VERSION = 17;
	// Dependencies
	public static final String TERRAFORM_WOOD_API_VERSION = "6.1.0-alpha.4";
	public static final String TRINKETS_VERSION = "3.6.0";
	public static final String EMI_VERSION = "0.7.3+1.19.4";

	public record Contributor(String name, String role) {}
}
