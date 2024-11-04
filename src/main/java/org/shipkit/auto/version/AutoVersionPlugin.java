package org.shipkit.auto.version;

import java.io.File;

import javax.inject.Inject;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.provider.ProviderFactory;

/**
 * The plugin, ideally with zero business logic, but only the Gradle integration
 * code
 */
public class AutoVersionPlugin implements Plugin<Project> {
    
    private final ProviderFactory providerFactory;

    @Inject
    public AutoVersionPlugin(ProviderFactory providerFactory) {
        this.providerFactory = providerFactory;
    }

    public void apply(Project project) {
        File versionFile = new File(project.getProjectDir(), "version.properties");
        GitValueSourceProviderFactory gitValueSourceProviderFactory = new GitValueSourceProviderFactory(project.getProjectDir(), providerFactory);
        AutoVersion autoVersion = new AutoVersion(gitValueSourceProviderFactory, versionFile);
        DeducedVersion version = autoVersion.deduceVersion(project.getVersion().toString());

        project.allprojects(p -> p.setVersion(version.getVersion()));
        project.getExtensions().getExtraProperties().set("shipkit-auto-version.previous-version",
                version.getPreviousVersion());
        project.getExtensions().getExtraProperties().set("shipkit-auto-version.previous-tag", version.getPreviousTag());
    }
}