package org.shipkit.auto.version;

import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;

import java.io.File;

public class GitValueSourceProviderFactory {

    private final File workingDirectory;
    private final ProviderFactory providerFactory;

    public GitValueSourceProviderFactory(File workingDirectory, ProviderFactory providerFactory) {
        this.workingDirectory = workingDirectory;
        this.providerFactory = providerFactory;
    }

    Provider<String> getProvider(String[] params) {
        return providerFactory.of(GitValueSource.class, spec -> {
            spec.parameters(parameters -> {
                parameters.getCommands().set(params);
                parameters.getWorkingDirectory().set(workingDirectory);
            });
        });
    }

}
