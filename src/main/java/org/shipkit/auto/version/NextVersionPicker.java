package org.shipkit.auto.version;

import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.provider.Provider;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.shipkit.auto.version.VersionConfig.isSupportedVersion;
import static org.shipkit.auto.version.VersionConfig.isSnapshot;

/**
 * Picks the next version to use.
 */
class NextVersionPicker {

    private final Logger log;
    private final GitValueSourceProviderFactory gitValueSourceProviderFactory;

    public NextVersionPicker(GitValueSourceProviderFactory gitValueSourceProviderFactory, Logger log) {
        this.gitValueSourceProviderFactory = gitValueSourceProviderFactory;
        this.log = log;
    }

    /**
     * Picks the next version to use based on the input parameters
     */
    String pickNextVersion(Optional<VersionNumber> previousVersion, VersionConfig config, String projectVersion) {
        if (!Project.DEFAULT_VERSION.equals(projectVersion)) {
            explainVersion(log, projectVersion, "uses version already specified in the Gradle project");
            return projectVersion;
        }

        if (!config.getVersionSpec().isPresent()) {
            String tag;
            String result;

            try {
                org.gradle.api.provider.Provider<String> tagDescriptionProvider = gitValueSourceProviderFactory.getProvider(new String[]{"describe", "--tags"});
                tag = tagDescriptionProvider.get().trim();
            } catch (Exception e) {
                result = "0.0.1-SNAPSHOT";
                log.info("Process 'git describe --tags' exited with non-zero exit value. Assuming there are no tags. " +
                        "Run with --debug for more.");
                log.debug("Ignored exception from 'git describe --tags'. Assuming there are no tags.", e);
                explainVersion(log, result, "couldn't run 'git describe --tags' (assuming there are no tags)");
                return result;
            }

            if (isSupportedVersion(tag, config.getTagPrefix())) {
                result = tag.substring(config.getTagPrefix().length());
                explainVersion(log, result, "deduced version based on tag: '" + tag + "'");
            } else if (isSnapshot(tag, config.getTagPrefix())) {
                Pattern pattern = Pattern.compile("\\d+\\.\\d+\\.\\d+(\\.\\d+)?");
                Matcher matcher = pattern.matcher(tag);
                matcher.find();
                result = new VersionNumber(matcher.group()).incrementBy(1).toString() + "-SNAPSHOT";
                explainVersion(log, result,
                        "deduced snapshot based on tag: '" + config.getTagPrefix() + matcher.group() + "'");
            } else {
                result = "0.0.1-SNAPSHOT";
                explainVersion(log, result, "found no version property and the code is not checked out on a valid tag");
            }

            return result;
        }

        if (!config.isWildcard()) {
            // if there is no wildcard we will use the version 'as is'
            explainVersion(log, config.getVersionSpec().get(), "uses verbatim version from version file");
            return config.getVersionSpec().get();
        }

        if (previousVersion.isPresent() && previousVersion.get().satisfies(config.getVersionSpec().get())) {
            VersionNumber prev = previousVersion.get();
            String[] params = { "log", "--pretty=oneline", TagConvention.tagFor(prev.toString(), config.getTagPrefix()) + "..HEAD" };
            Provider<String> prettyLogProvider = gitValueSourceProviderFactory.getProvider(params);
            String gitOutput = prettyLogProvider.get();
            int commitCount = new CommitCounter().countCommitDelta(gitOutput);
            String result = prev.incrementBy(commitCount).toString();
            explainVersion(log, result, "deduced version based on previous tag: '" + prev + "'");
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
        log.lifecycle("Building version '" + version + "'\n" +
                "  - reason: shipkit-auto-version " + reason);
    }
}
