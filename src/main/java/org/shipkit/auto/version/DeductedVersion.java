package org.shipkit.auto.version;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Value object representing the version deducted by the auto plugin.
 */
class DeductedVersion {

    private final String version;
    private final String previousVersion;

    DeductedVersion(String version, String previousVersion) {
        Objects.requireNonNull(version, "version cannot be null");
        this.version = version;
        this.previousVersion = previousVersion;
    }

    /**
     * Deducted version, never null.
     */
    String getVersion() {
        return version;
    }

    /**
     * Previous version.
     * The returned value can be null when there is no previous tag
     * or when it was not possible to identify the previous tag.
     * Not using Optional because it makes the client logic simpler.
     */
    @Nullable
    String getPreviousVersion() {
        return previousVersion;
    }
}