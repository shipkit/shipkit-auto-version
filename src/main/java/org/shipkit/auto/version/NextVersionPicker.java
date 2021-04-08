package org.shipkit.auto.version;

import com.github.zafarkhaja.semver.Version;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;

import java.util.Optional;

/**
 * Picks the next version to use.
 */
class NextVersionPicker {
    private final ProcessRunner runner;
    private final Logger log;

    NextVersionPicker(ProcessRunner runner, Logger log) {
        this.runner = runner;
        this.log = log;
    }

    /**
     * Picks the next version to use based on the input parameters
     */
    String pickNextVersion(Optional<Version> previousVersion, VersionConfig config, String projectVersion) {
        if (!Project.DEFAULT_VERSION.equals(projectVersion)) {
            explainVersion(log, projectVersion, "uses version already specified in the Gradle project");
            return projectVersion;
        }

        if (!config.isWildcard()) {
            //if there is no wildcard we will use the version 'as is'
            explainVersion(log, config.toString(), "uses verbatim version from version file");
            return config.toString();
        }

        if (previousVersion.isPresent() && previousVersion.get().satisfies(config.toString())) {
            Version prev = previousVersion.get();
            String gitOutput = runner.run(
                    "git", "log", "--pretty=oneline", TagConvention.tagFor(prev.toString(), config.getTagPrefix()) + "..HEAD");
            int commitCount = new CommitCounter().countCommitDelta(gitOutput);
            String result = Version
                    .forIntegers(
                            prev.getMajorVersion(),
                            prev.getMinorVersion(),
                            prev.getPatchVersion() + commitCount)
                    .toString();
            explainVersion(log, result, "deducted version based on previous tag: '" + prev + "'");
            return result;
        } else {
            String result = config.newPatchVersion();
            explainVersion(log, result, "found no tags matching version spec: '" + config + "'");
            return result;
        }
    }

    /**
     * Explain version in a consistent, human-readable way
     */
    static void explainVersion(Logger log, String version, String reason) {
        log.lifecycle("Building version '"+ version + "'\n" +
                "  - reason: shipkit-auto-version " + reason);
    }
}
