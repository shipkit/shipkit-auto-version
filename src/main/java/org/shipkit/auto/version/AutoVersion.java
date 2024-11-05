package org.shipkit.auto.version;

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.File;
import java.util.Collection;
import java.util.Optional;

import static org.shipkit.auto.version.NextVersionPicker.explainVersion;

/**
 * Main functionality, should never depend on any of Gradle APIs.
 */
class AutoVersion {

    private final static Logger LOG = Logging.getLogger(AutoVersion.class);

    private final GitValueSourceProviderFactory gitValueSourceProviderFactory;
    private final File versionFile;

    AutoVersion(GitValueSourceProviderFactory gitValueSourceProviderFactory, File versionFile) {
        this.versionFile = versionFile;
        this.gitValueSourceProviderFactory = gitValueSourceProviderFactory;
    }

    /**
     * Deduce version based on existing tags (will run 'git tag'), and the version spec from versionFile field.
     *
     * @param projectVersion the version of the gradle project before running the plugin
     */
    DeducedVersion deduceVersion(String projectVersion) {
        return deduceVersion(LOG, projectVersion);
    }

    //Exposed for testing so that 'log' can be mocked
    DeducedVersion deduceVersion(Logger log, String projectVersion) {
        Optional<VersionNumber> previousVersion = Optional.empty();
        VersionConfig config = VersionConfig.parseVersionFile(versionFile);

        try {
            VersionsProvider versionsProvider = new VersionsProvider(gitValueSourceProviderFactory);
            Collection<VersionNumber> versions = versionsProvider.getAllVersions(config.getTagPrefix());
            PreviousVersionFinder previousVersionFinder = new PreviousVersionFinder();

            if (config.getVersionSpec().isPresent()) {
                previousVersion = previousVersionFinder.findPreviousVersion(versions, config);
            }

            NextVersionPicker nextVersionPicker = new NextVersionPicker(gitValueSourceProviderFactory, log);
            String nextVersion = nextVersionPicker.pickNextVersion(previousVersion,
                    config, projectVersion);

            if (!config.getVersionSpec().isPresent()) {
                previousVersion = previousVersionFinder.findPreviousVersion(versions, new VersionConfig(nextVersion, config.getTagPrefix()));
            }

            logPreviousVersion(log, previousVersion);

            return new DeducedVersion(nextVersion, previousVersion, config.getTagPrefix());
        } catch (Exception e) {
            String message = "caught an exception, falling back to reasonable default";
            log.debug("shipkit-auto-version " + message, e);
            String v = config.getVersionSpec().orElse("0.0.1-SNAPSHOT").replace("*", "unspecified");
            explainVersion(log, v, message + "\n  - run with --debug for more info");
            return new DeducedVersion(v, previousVersion, config.getTagPrefix());
        }
    }

    private void logPreviousVersion(Logger log, Optional<VersionNumber> previousVersion) {
        log.info("[shipkit-auto-version] " + previousVersion
                .map(version -> "Previous version: " + version)
                .orElse("No previous version"));
    }
}
