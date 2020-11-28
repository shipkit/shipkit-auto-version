package org.shipkit.auto.version;

import com.github.zafarkhaja.semver.Version;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;

import java.util.Optional;

import static java.util.Arrays.asList;

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
    String pickNextVersion(Optional<Version> previousVersion, RequestedVersion requestedVersion, String projectVersion) {
        if (!Project.DEFAULT_VERSION.equals(projectVersion)) {
            explainVersion(log, projectVersion, "uses version already specified in the Gradle project");
            return projectVersion;
        }

        if (!requestedVersion.isWildcard()) {
            //if there is no wildcard we will use the version 'as is'
            explainVersion(log, requestedVersion.toString(), "uses verbatim version from version file");
            return requestedVersion.toString();
        }

        if (previousVersion.isPresent() && previousVersion.get().satisfies(requestedVersion.toString())) {
            Version prev = previousVersion.get();
            String gitOutput = runner.run(
                    "git", "log", "--pretty=oneline", TagConvention.tagFor(prev.toString()) + "..HEAD");
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
            String result = requestedVersion.newPatchVersion();
            explainVersion(log, result, "found no tags matching version spec: '" + requestedVersion + "'");
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
