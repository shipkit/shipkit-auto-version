package org.shipkit.auto.version;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * The plugin, ideally with zero business logic, but only the Gradle integration code
 */
public class AutoVersionPlugin implements Plugin<Project> {

    public void apply(Project project) {
        DeducedVersion version = new AutoVersion(project.getProjectDir()).deduceVersion(project.getVersion().toString());
        project.allprojects(p -> p.setVersion(version.getVersion()));
        project.getExtensions().getExtraProperties().set("shipkit-auto-version.previous-version", version.getPreviousVersion());
        project.getExtensions().getExtraProperties().set("shipkit-auto-version.previous-tag", version.getPreviousTag());
    }
}
