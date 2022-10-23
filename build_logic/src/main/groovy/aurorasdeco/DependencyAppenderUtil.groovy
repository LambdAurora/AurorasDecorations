package aurorasdeco

import org.gradle.api.Project

class DependencyAppenderUtil {
	private final String name
	private final String dep

	DependencyAppenderUtil(String name, String dep) {
		this.name = name
		this.dep = dep
	}

	void apply(Project project) {
		project.dependencies {
			modImplementation(this.dep) {
				exclude group: 'net.fabricmc'
				exclude group: 'net.fabricmc.fabric-api'
				exclude module: 'modmenu'
			}
		}

		project.sourceSets {
			actualmod {
				compileClasspath += project.sourceSets.getByName(this.name).compileClasspath
				runtimeClasspath += project.sourceSets.getByName(this.name).runtimeClasspath
			}

			testmod {
				compileClasspath += project.sourceSets.getByName(this.name).compileClasspath
				runtimeClasspath += project.sourceSets.getByName(this.name).runtimeClasspath
			}
		}

		project.tasks.jar {
			from project.sourceSets.getByName(this.name).output
		}

		project.tasks.sourcesJar {
			from project.sourceSets.getByName(this.name).java
		}
	}
}
