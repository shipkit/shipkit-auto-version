package org.shipkit.auto.version;

import com.github.zafarkhaja.semver.Version;

import java.io.File;
import java.util.Optional;

import static java.lang.Integer.parseInt;
import static java.util.Arrays.asList;

/**
 * Main functionality, should never depend on any of Gradle APIs.
 */
class AutoVersion {

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
        String spec = VersionSpec.readVersionSpec(versionFile);
        if (!spec.endsWith("*")) {
            //if there is no wildcard we will use the version 'as is'
            return spec;
        }

        String gitOutput = runner.run("git", "tag");
        String[] tags = gitOutput.split(System.lineSeparator());
        Optional<Version> nearest = new NearestTagFinder().findTag(asList(tags), spec);
        if (!nearest.isPresent()) {
            //if there is no nearest matching tag (same major, same minor) we can just use '0' for the wildcard
            return spec.replace("*", "0");
        }

        //since there is a matching nearest tag we will count the commits to resolve wildcard
        Version tag = nearest.get();

        gitOutput = runner.run("git", "log", "--pretty=oneline", "v" + tag + "..master");
        int commitCount = new CommitCounter().countCommitDelta(gitOutput);

        Version result = Version.forIntegers(
                tag.getMajorVersion(), tag.getMinorVersion(), tag.getPatchVersion() + commitCount);

        return result.toString();
    }
}