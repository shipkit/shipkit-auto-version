package org.shipkit.auto.version;

import java.io.File;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import org.gradle.api.logging.Logger;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;

import static org.shipkit.auto.version.VersionConfig.isSupportedVersion;

/**
 * Finds versions based on tags, finds tags by running Git command
 */
class VersionsProvider {

    private final GitValueSourceProviderFactory gitValueSourceProviderFactory;

    public VersionsProvider(GitValueSourceProviderFactory gitValueSourceProviderFactory) {
        this.gitValueSourceProviderFactory = gitValueSourceProviderFactory;
    }

    /**
     * Finds all versions based on Git tags.
     *
     * @param tagPrefix tag prefix
     */
    Collection<VersionNumber> getAllVersions(String tagPrefix) {

        Provider<String> gitTagProvider = gitValueSourceProviderFactory.getProvider(new String[]{"tag"});

        String gitOutput = gitTagProvider.get();
        String[] tagOutput = gitOutput.split("\\R");

        Set<VersionNumber> result = new TreeSet<>();
        for (String line : tagOutput) {
            String tag = line.trim();
            if (isSupportedVersion(tag, tagPrefix)) {
                String v = tag.substring(tagPrefix.length());
                result.add(new VersionNumber(v));
            }
        }

        return result;
    }
}
