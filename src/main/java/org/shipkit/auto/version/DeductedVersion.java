package org.shipkit.auto.version;

import javax.annotation.Nullable;

/**
 * Value object representing the version deducted by the auto plugin.
 */
class DeductedVersion {

    private final String version;
    private final String previousVersion;

    public DeductedVersion(String version, String previousVersion) {
        this.version = version;
        this.previousVersion = previousVersion;
    }

    public String getVersion() {
        return version;
    }

    /**
     * The returned value can be null when there is no previous tag
     * or when it was not possible to identify the previous tag.
     */
    @Nullable
    public String getPreviousVersion() {
        return previousVersion;
    }
}