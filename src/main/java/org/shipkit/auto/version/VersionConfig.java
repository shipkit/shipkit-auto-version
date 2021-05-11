package org.shipkit.auto.version;

import com.github.zafarkhaja.semver.Version;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

/**
 * Version configuration representing the version file.
 */
class VersionConfig {

    private final String requestedVersion;
    private final String tagPrefix;

    /**
     * Creates configuration object.
     * Throws an exception if the supplied requestedVersion has invalid format.
     *
     * @param requestedVersion requested version specification, i.e. '1.0.*', '2.0.0'
     * @param tagPrefix tag prefix, typically "v" or "release-" or an empty String
     */
    VersionConfig(String requestedVersion, String tagPrefix) {
        this.requestedVersion = requestedVersion;
        this.tagPrefix = tagPrefix;

        if (requestedVersion != null) {
            String test = isWildcard() ?
                    newPatchVersion() : //this will create a version we can validate
                    requestedVersion;

            try {
                Version.valueOf(test); //validation
            } catch (Exception e) {
                throw new ShipkitAutoVersionException(
                        "Invalid version specification: '" + requestedVersion + "'\n" +
                                "  Correct examples: '1.0.*', '2.10.100'", e);
            }
        }
    }

    String newPatchVersion() {
        return requestedVersion.replace('*', '0');
    }

    /**
     * Reads the configuration from provided file.
     * Throws exception message with actionable message when version file does not exist
     * or when it does not contain correctly formatted content.
     *
     * @param versionFile file that has the version configuration
     * @return validated version configuration
     */
    static VersionConfig parseVersionFile(File versionFile) {
        Properties p = new Properties();

        if (!versionFile.exists()) {
            return new VersionConfig(null, "v");
        }

        try {
            p.load(new FileReader(versionFile));
        } catch (IOException e) {
            System.out.println("[shipkit-auto-version] Ignoring file '" + versionFile.getName()
                    + "' because it is not readable");
        }

        String v;
        if (p.containsKey("version") && p.get("version").toString().trim().equals("")) {
            v = null;
        } else if (!p.containsKey("version")) {
            v = null;
        } else {
            v = (String) p.get("version");
        }

        String tagPrefix = (String) p.getOrDefault("tagPrefix", "v");

        return new VersionConfig(v, tagPrefix);
    }

    /**
     * Returns true when the version spec is valid and uses '*' wildcard.
     */
    boolean isWildcard() {
        return requestedVersion.matches("\\d+\\.\\d+\\.\\*");
    }

    /**
     * Informs if given version is supported by our system.
     *
     * @return true for simple versions like '1.0.0', '2.33.444',
     *          false for versions like '1.0.0-beta', '1.0', '1.foo'
     */
    static boolean isSupportedVersion(String version) {
        return version.matches("\\d+\\.\\d+\\.\\d+");
    }

    static boolean isSnapshot(String tag, String tagPrefix) {
        return tag.substring(tagPrefix.length()).matches("\\d+\\.\\d+\\.\\d+\\-\\d+\\-\\w+");
    }

    public String toString() {
        return requestedVersion;
    }

    public String getTagPrefix() {
        return tagPrefix;
    }

    public Optional<String> getRequestedVersion() {
        if (requestedVersion == null) {
            return Optional.empty();
        }
        return Optional.of(requestedVersion);
    }
}
