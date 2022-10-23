package aurorasdeco.extension;

import aurorasdeco.Constants;
import aurorasdeco.Entrypoint;
import net.fabricmc.loom.api.LoomGradleExtensionAPI;
import org.gradle.api.Action;
import org.gradle.api.Named;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.SourceSetContainer;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import java.io.Serial;
import java.io.Serializable;

public class AurorasDecoExtension implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;

	private final Project project;
	private final Property<Boolean> hasEmi;
	private final Property<Boolean> hasTrinkets;
	private final NamedDomainObjectContainer<NamedWriteOnlyList> entrypoints;

	@Inject
	public AurorasDecoExtension(ObjectFactory factory, Project project) {
		this.project = project;
		this.hasEmi = factory.property(Boolean.class).convention(false);
		this.hasEmi.finalizeValueOnRead();
		this.hasTrinkets = factory.property(Boolean.class).convention(false);
		this.hasTrinkets.finalizeValueOnRead();
		this.entrypoints = factory.domainObjectContainer(NamedWriteOnlyList.class, n -> new NamedWriteOnlyList(factory, n));
	}

	@Input
	public Property<Boolean> getHasEmi() {
		return this.hasEmi;
	}

	@Input
	public Property<Boolean> getHasTrinkets() {
		return this.hasTrinkets;
	}

	@Nested
	public NamedDomainObjectContainer<NamedWriteOnlyList> getEntrypoints() {
		return this.entrypoints;
	}

	public void entrypoints(Action<NamedDomainObjectContainer<NamedWriteOnlyList>> configure) {
		configure.execute(this.entrypoints);
	}

	public Entrypoint entrypoint(String target) {
		return new Entrypoint(target, true);
	}

	public Entrypoint entrypoint(String target, boolean enabled) {
		return new Entrypoint(target, enabled);
	}

	public void finalizeConfiguration() {
		if (this.hasEmi.get()) {
			this.project.getPlugins().withType(JavaBasePlugin.class).configureEach(plugin -> {
				var sourceSets = (SourceSetContainer) this.project.getExtensions().getByName("sourceSets");
				this.registerSourceSet(sourceSets, "emi");

				new aurorasdeco.DependencyAppenderUtil("emi", "dev.emi:emi:" + Constants.EMI_VERSION).apply(this.project);

				this.project.getDependencies().add("emiImplementation", sourceSets.getByName("main").getOutput());
			});
		}

		if (this.hasTrinkets.get()) {
			this.project.getPlugins().withType(JavaBasePlugin.class).configureEach(plugin -> {
				var sourceSets = (SourceSetContainer) this.project.getExtensions().getByName("sourceSets");
				this.registerSourceSet(sourceSets, "trinkets");

				new aurorasdeco.DependencyAppenderUtil("trinkets", "dev.emi:trinkets:" + Constants.TRINKETS_VERSION).apply(this.project);

				this.project.getDependencies().add("trinketsImplementation", sourceSets.getByName("main").getOutput());
			});
		}
	}

	private void registerSourceSet(SourceSetContainer sourceSets, String name) {
		sourceSets.register(name, sourceSet -> {
			sourceSet.setCompileClasspath(sourceSet.getCompileClasspath().plus(sourceSets.getByName("main").getCompileClasspath()));
			sourceSet.setRuntimeClasspath(sourceSet.getRuntimeClasspath().plus(sourceSets.getByName("main").getRuntimeClasspath()));

			var loom = this.project.getExtensions().getByType(LoomGradleExtensionAPI.class);
			loom.createRemapConfigurations(sourceSet);
		}).get();
	}

	public static class NamedWriteOnlyList implements Named {
		private final String name;
		private final ListProperty<Entrypoint> values;
		private final Property<Boolean> enabled;

		public NamedWriteOnlyList(ObjectFactory factory, String name) {
			this.values = factory.listProperty(Entrypoint.class);
			this.name = name;
			this.enabled = factory.property(Boolean.class).convention(true);
		}

		@Override
		@Input
		public @NotNull String getName() {
			return this.name;
		}

		@Input
		public ListProperty<Entrypoint> getValues() {
			return this.values;
		}

		public void setValues(Iterable<Entrypoint> list) {
			this.values.set(list);
		}

		@Input
		public Property<Boolean> getEnabled() {
			return this.enabled;
		}
	}
}
