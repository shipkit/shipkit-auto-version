package org.shipkit.auto.version;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class AutoVersionPlugin implements Plugin<Project> {

    public void apply(Project project) {
        VersionSpec spec = new VersionSpec(project.file("version.properties"));
        if (!spec.isWildcard()) {
            project.setVersion(spec.getSpec());
            return;
        }

        throw new RuntimeException("not implemented yet");
    }
}
