package aurorasdeco.task;

import aurorasdeco.Constants;
import aurorasdeco.extension.AurorasDecoExtension;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.quiltmc.json5.JsonWriter;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class GenerateQmjTask extends DefaultTask {
	@OutputDirectory
	public abstract DirectoryProperty getOutputDir();

	@Nested
	public abstract Property<AurorasDecoExtension> getAurorasDecoModule();

	@Inject
	public GenerateQmjTask() {
		this.setGroup("generation");
	}

	@TaskAction
	public void generateQmj() throws IOException {
		Path output = this.getOutputDir().getAsFile().get().toPath().resolve("quilt.mod.json");
		this.getProject().getLogger().lifecycle(output.toAbsolutePath().toString());

		if (Files.exists(output)) {
			Files.delete(output);
		}

		JsonWriter writer = JsonWriter.json(output);

		writer.beginObject()
				.name("schema_version").value(1);
		{
			writer.name("quilt_loader").beginObject()
					.name("group").value(this.getProject().getGroup().toString())
					.name("id").value(Constants.NAMESPACE)
					.name("version").value(this.getProject().getVersion().toString());
			{
				writer.name("metadata").beginObject()
						.name("name").value(Constants.NAME)
						.name("description").value(Constants.DESCRIPTION)
						.name("contributors").beginObject();

				for (var entry : Constants.CONTRIBUTORS) {
					writer.name(entry.name()).value(entry.role());
				}

				writer.endObject()
						.name("contact").beginObject();

				{
					writer.name("homepage").value(Constants.Links.WEBSITE)
							.name("sources").value(Constants.Links.SOURCES)
							.name("issues").value(Constants.Links.ISSUES);
				}

				writer.endObject()
						.name("license").value(Constants.LICENSE)
						.name("icon").value(Constants.ICON_PATH);
			}
			writer.endObject()
					.name("intermediate_mappings").value("net.fabricmc:intermediary");

			if (!this.getAurorasDecoModule().get().getEntrypoints().isEmpty()) {
				writer.name("entrypoints").beginObject();

				for (var entrypoint : this.getAurorasDecoModule().get().getEntrypoints()) {
					if (!entrypoint.getEnabled().get()) continue;

					writer.name(entrypoint.getName());
					writer.beginArray();
					for (var target : entrypoint.getValues().get()) {
						target.write(writer);
					}
					writer.endArray();
				}

				writer.endObject();
			}

			writer.name("depends").beginArray();
			{
				writer.beginObject()
						.name("id").value("minecraft")
						.name("versions");
				if (Constants.MINECRAFT_VERSION.supported().isEmpty()) {
					writer.value(Constants.MINECRAFT_VERSION.version());
				} else {
					writer.beginObject()
							.name("any").beginArray();
					for (var version : Constants.MINECRAFT_VERSION.all()) {
						writer.value("=" + version);
					}
					writer.endArray()
							.endObject();
				}
				writer.endObject();

				writer.beginObject()
						.name("id").value("quilt_loader")
						.name("versions").value(">=" + Constants.LOADER_VERSION)
						.endObject();
				writer.beginObject()
						.name("id").value("quilted_fabric_api")
						.name("versions").value(">=" + Constants.QFAPI_VERSION)
						.endObject();
				writer.beginObject()
						.name("id").value("java")
						.name("versions").value(">=" + Constants.JAVA_VERSION)
						.endObject();
				writer.beginObject()
						.name("id").value("terraform-wood-api-v1")
						.name("versions").value(">=" + Constants.TERRAFORM_WOOD_API_VERSION)
						.endObject();

				if (this.getAurorasDecoModule().get().getHasEmi().get()) {
					writer.beginObject()
							.name("id").value("emi")
							.name("optional").value(true)
							.endObject();
				}

				if (this.getAurorasDecoModule().get().getHasTrinkets().get()) {
					writer.beginObject()
							.name("id").value("trinkets")
							.name("versions").value(">=" + Constants.TRINKETS_VERSION)
							.name("optional").value(true)
							.endObject();
				}
			}
			writer.endArray();
		}
		writer.endObject();

		writer.name("mixin").value("aurorasdeco.mixins.json");

		writer.name("modmenu").beginObject();
		{
			writer.name("links").beginObject();
			{
				writer.name("modmenu.curseforge").value(Constants.Links.CURSEFORGE)
						.name("modmenu.discord").value(Constants.Links.DISCORD)
						.name("modmenu.github_releases").value(Constants.Links.GITHUB_RELEASES)
						.name("modmenu.modrinth").value(Constants.Links.MODRINTH)
						.name("modmenu.twitter").value(Constants.Links.TWITTER);
			}
			writer.endObject();
		}
		writer.endObject();

		writer.endObject();
		writer.flush();
		writer.close();
	}
}
