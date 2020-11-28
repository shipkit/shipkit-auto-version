package org.shipkit.auto.version;

import com.github.zafarkhaja.semver.Version;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

/**
 * Requested version object representing the version value specified in the version file.
 */
class RequestedVersion {

    private final String requestedVersion;

    /**
     * Creates requested version object based on the requestedVersion String specification.
     * Throws an exception if the supplied requestedVersion has invalid format.
     *
     * @param requestedVersion requested version specification, i.e. '1.0.*', '2.0.0'
     */
    RequestedVersion(String requestedVersion) {
        this.requestedVersion = requestedVersion;
        String test = isWildcard()?
                newPatchVersion(): //this will create a version we can validate
                requestedVersion;

        try {
            Version.valueOf(test); //validation
        } catch (Exception e) {
            throw new ShipkitAutoVersionException(
                    "Invalid version specification: '" + requestedVersion + "'\n" +
                    "  Correct examples: '1.0.*', '2.10.100'", e);
        }
    }

    String newPatchVersion() {
        return requestedVersion.replace('*', '0');
    }

    /**
     * Reads the version specification from provided file.
     * Throws exception message with actionable message when version file does not exist
     * or when it does not contain correctly formatted version value.
     *
     * @param versionFile file that has the version specification
     * @return validated requested version object
     */
    static RequestedVersion parseVersionFile(File versionFile) {
        Properties p = new Properties();
        try {
            p.load(new FileReader(versionFile));
        } catch (IOException e) {
            throw new ShipkitAutoVersionException(
                    "Please create file 'version.properties' with a valid 'version' property, " +
                            "for example 'version=1.0.*'", e);
        }

        String v = (String) p.get("version");
        if (v == null || v.trim().isEmpty()) {
            throw new ShipkitAutoVersionException(
                    "File '" + versionFile.getName() + "' is missing the 'version' property\n" +
                    "  Correct examples: 'version=1.0.*', 'version=2.10.100'");
        }
        return new RequestedVersion(v);
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

    public String toString() {
        return requestedVersion;
    }
}
