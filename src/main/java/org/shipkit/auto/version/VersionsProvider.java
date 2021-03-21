package org.shipkit.auto.version;

import com.github.zafarkhaja.semver.Version;

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
    Collection<Version> getAllVersions(String tagPrefix) {
        String gitOutput = runner.run("git", "tag");
        String[] tagOutput = gitOutput.split("\\R");

        Set<Version> result = new TreeSet<>();
        for (String line : tagOutput) {
            String tag = line.trim();
            if (TagConvention.isVersionTag(tag, tagPrefix) && isSupportedVersion(tag.substring(tagPrefix.length()))) {
                String v = tag.substring(tagPrefix.length());
                Version version = Version.valueOf(v);
                result.add(version);
            }
        }

        return result;
    }
}
