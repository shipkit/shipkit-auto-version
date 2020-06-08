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

    String deductVersion() {
        return deductVersion(LOG);
    }

    private static void explainVersion(Logger log, String version, String reason) {
        log.lifecycle("Building version '"+ version + "'\n" +
                "  - reason: shipkit-auto-version " + reason);
    }

    String deductVersion(Logger log) {
        String spec = VersionSpec.readVersionSpec(versionFile);
        if (!spec.endsWith("*")) {
            //if there is no wildcard we will use the version 'as is'
            explainVersion(log, spec, "uses verbatim version from '" + versionFile.getName() + "' file");
            return spec;
        }

        try {
            return deductVersion(spec, log);
        } catch (Exception e) {
            String message = "caught an exception, falling back to reasonable default";
            log.debug("shipkit-auto-version " + message, e);
            String v = spec.replace("*", "unspecified");
            explainVersion(log, v, message + "\n  - run with --debug for more info");
            return v;
        }
    }

    String deductVersion(String spec, Logger log) {
        String gitOutput = runner.run("git", "tag");
        String[] tags = gitOutput.split("\\R");
        Optional<Version> nearest = new NearestTagFinder().findTag(asList(tags), spec);
        if (!nearest.isPresent()) {
            //if there is no nearest matching tag (same major, same minor) we can just use '0' for the wildcard
            String version = spec.replace("*", "0");
            //TODO add information 'found no tags matching ...'
            explainVersion(log, version, "found no tags");
            return version;
        }

        //since there is a matching nearest tag we will count the commits to resolve wildcard
        Version tag = nearest.get();

        gitOutput = runner.run("git", "log", "--pretty=oneline", "v" + tag + "..HEAD");
        int commitCount = new CommitCounter().countCommitDelta(gitOutput);

        Version result = Version.forIntegers(
                tag.getMajorVersion(), tag.getMinorVersion(), tag.getPatchVersion() + commitCount);

        String v = result.toString();
        explainVersion(log, v, "deducted version based on previous tag: '" + tag + "'");
        return v;
    }
}