package org.shipkit.auto.version;

import com.github.zafarkhaja.semver.Version;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.File;
import java.util.Optional;

import static java.util.Arrays.asList;

/**
 * Main functionality, should never depend on any of Gradle APIs.
 */
class AutoVersion {

    //depends on Gradle API but it is easy to refactor if needed
    private final static Logger LOG = Logging.getLogger(AutoVersion.class);

    private final ProcessRunner runner;
    private final File versionFile;

    AutoVersion(ProcessRunner runner, File versionFile) {
        this.runner = runner;
        this.versionFile = versionFile;
    }

    AutoVersion(File projectDir) {
        this(new ProcessRunner(projectDir), new File(projectDir, "version.properties"));
    }

    /**
     * Deduct version based on existing tags (will run 'git tag'), and the version spec from versionFile field.
     */
    DeductedVersion deductVersion() {
        return deductVersion(LOG);
    }

    private static void explainVersion(Logger log, String version, String reason) {
        log.lifecycle("Building version '"+ version + "'\n" +
                "  - reason: shipkit-auto-version " + reason);
    }

    //Exposed for testing so that 'log' can be mocked
    DeductedVersion deductVersion(Logger log) {
        String spec = VersionSpec.readVersionSpec(versionFile);
        if (!spec.endsWith("*")) {
            //if there is no wildcard we will use the version 'as is'
            explainVersion(log, spec, "uses verbatim version from '" + versionFile.getName() + "' file");
            return new DeductedVersion(spec, null);
        }

        try {
            return deductVersion(spec, log);
        } catch (Exception e) {
            String message = "caught an exception, falling back to reasonable default";
            log.debug("shipkit-auto-version " + message, e);
            String v = spec.replace("*", "unspecified");
            explainVersion(log, v, message + "\n  - run with --debug for more info");
            return new DeductedVersion(v, null);
        }
    }

    //Exposed for testing so that 'log' can be mocked and we can pass arbitrary spec
    DeductedVersion deductVersion(String spec, Logger log) {
        String gitOutput = runner.run("git", "tag");
        String[] tags = gitOutput.split("\\R");
        Optional<Version> nearest = new NearestTagFinder().findTag(asList(tags), spec);
        if (!nearest.isPresent()) {
            //if there is no nearest matching tag (same major, same minor) we can just use '0' for the wildcard
            String version = spec.replace("*", "0");
            explainVersion(log, version, "found no tags matching version spec: '" + spec + "'");
            return new DeductedVersion(version, null);
        }

        //since there is a matching nearest tag we will count the commits to resolve wildcard
        Version previousVersion = nearest.get();

        gitOutput = runner.run("git", "log", "--pretty=oneline", "v" + previousVersion + "..HEAD");
        int commitCount = new CommitCounter().countCommitDelta(gitOutput);

        Version result = Version.forIntegers(
                previousVersion.getMajorVersion(), previousVersion.getMinorVersion(), previousVersion.getPatchVersion() + commitCount);

        String v = result.toString();
        explainVersion(log, v, "deducted version based on previous tag: '" + previousVersion.toString() + "'");
        return new DeductedVersion(v, previousVersion.toString());
    }
}