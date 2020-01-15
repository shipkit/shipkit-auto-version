package org.shipkit.auto.version;

import org.gradle.api.Project;
import org.gradle.api.Plugin;

public class AutoVersionPlugin implements Plugin<Project> {
    public void apply(Project project) {
        //dummy implementation
        project.setVersion("1.0.1");
    }
}
