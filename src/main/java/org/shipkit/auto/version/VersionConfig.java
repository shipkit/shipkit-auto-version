package org.shipkit.auto.version;


import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Optional;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * Version configuration representing the version file.
 */
class VersionConfig {

    private final String versionSpec;
    private final String tagPrefix;

    /**
     * Creates configuration object.
     * Throws an exception if the supplied versionSpec has invalid format.
     *
     * @param versionSpec requested version specification, i.e. '1.0.*', '2.0.0'
     * @param tagPrefix tag prefix, typically "v" or "release-" or an empty String
     */
    VersionConfig(String versionSpec, String tagPrefix) {
        this.versionSpec = versionSpec;
        this.tagPrefix = tagPrefix;

        if (versionSpec != null) {
            String test = isWildcard() ?
                    newPatchVersion() : //this will create a version we can validate
                    versionSpec;

            try {
                new VersionNumber(test);
            } catch (Exception e) {
                throw new ShipkitAutoVersionException(
                        "Invalid version specification: '" + versionSpec + "'\n" +
                                "  Correct examples: '1.0.*', '1.0.0.*', '2.10.100', '1.2.3.4", e);
            }
        }
    }

    String newPatchVersion() {
        return versionSpec.replace('*', '0');
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
        return versionSpec.matches("^\\d+\\.\\d+\\.(\\d+\\.)?\\*$");
    }

    /**
     * Informs if given tag and version taken from this tag are supported by our system
     *
     * @return true for simple versions like '1.0.0', '2.33.444'
     *          (provided the tag's prefix is equal to the 'tagPrefix' property),
     *          false for versions like '1.0.0-beta', '1.0', '1.foo'
     *
     */
    static boolean isSupportedVersion(String tag, String tagPrefix) {
        return tag.startsWith(tagPrefix) && isSupportedVersion(tag.substring(tagPrefix.length()));
    }

    /**
     * Informs if provided version is supported (e.g. 1.2.3 or 1.2.3.4).
     * It's a simple check, not really supporting any interesting semver variants.
     * It supports 1.2.3.4 variant that is _not_ semver compatible.
     *
     * @see #isSupportedVersion(String, String)
     * @param version version to test
     * @return true if version is supported
     */
    static boolean isSupportedVersion(String version) {
        return version.matches("^\\d+\\.\\d+\\.\\d+(\\.\\d+)?$");
    }

    /**
     * Informs if given tag is supported and if snapshot can be deduced
     * as the tag is not an annotated tag
     *
     * @return true for tags like 'v1.0.0-1-sha1234', '2.33.444-15-dgo4d29u'
     *          (provided the tag's prefix is equal to the 'tagPrefix' property),
     *          false for tags like 'v1.0.0-beta', '1.0', '1.foo'
     */
    static boolean isSnapshot(String tag, String tagPrefix) {
        return tag.startsWith(tagPrefix)
                && tag.substring(tagPrefix.length()).matches("\\d+\\.\\d+\\.\\d+(\\.\\d+)?-\\d+-\\w+");
    }

    public String toString() {
        return versionSpec;
    }

    public String getTagPrefix() {
        return tagPrefix;
    }

    public Optional<String> getVersionSpec() {
        if (versionSpec == null) {
            return Optional.empty();
        }
        return Optional.of(versionSpec);
    }
}
