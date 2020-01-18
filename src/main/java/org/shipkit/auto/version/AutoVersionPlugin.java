package org.shipkit.auto.version;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * The plugin, ideally with zero business logic, but only the Gradle integration code
 */
public class AutoVersionPlugin implements Plugin<Project> {

    public void apply(Project project) {
        String version = new AutoVersion(project.getProjectDir()).deductVersion();
        project.setVersion(version);
    }
}
