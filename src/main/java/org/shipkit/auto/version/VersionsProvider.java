package org.shipkit.auto.version;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import static org.shipkit.auto.version.VersionConfig.isSupportedVersion;

/**
 * Finds versions based on tags, finds tags by running Git command
 */
class VersionsProvider {
    private final ProcessRunner runner;

    VersionsProvider(ProcessRunner runner) {
        this.runner = runner;
    }

    /**
     * Finds all versions based on Git tags.
     *
     * @param tagPrefix tag prefix
     */
    Collection<VersionNumber> getAllVersions(String tagPrefix) {
        String gitOutput = runner.run("git", "tag");
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
