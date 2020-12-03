package org.shipkit.auto.version;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * The plugin, ideally with zero business logic, but only the Gradle integration code
 */
public class AutoVersionPlugin implements Plugin<Project> {

    public void apply(Project project) {
        DeductedVersion version = new AutoVersion(project.getProjectDir()).deductVersion(project.getVersion().toString());
        project.allprojects(p -> p.setVersion(version.getVersion()));
        project.getExtensions().getExtraProperties().set("shipkit-auto-version.previous-version", version.getPreviousVersion());

        if (version.getPreviousVersion() != null) {
            project.getExtensions().getExtraProperties().set("shipkit-auto-version.previous-tag", "v"+version.getPreviousVersion());
        } else {
            project.getExtensions().getExtraProperties().set("shipkit-auto-version.previous-tag", null);
        }
    }
}
