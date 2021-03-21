package org.shipkit.auto.version;

import com.github.zafarkhaja.semver.Version;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

/**
 * Value object representing the deducted version
 */
class DeductedVersion {

    private final String version;
    private final String previousVersion;
    private final String previousTag;
    private final String tagPrefix;

    DeductedVersion(String version, Optional<Version> previousVersion, String tagPrefix) {
        this.tagPrefix = tagPrefix;
        Objects.requireNonNull(version, "version cannot be null");
        this.version = version;
        this.previousVersion = previousVersion.map(Version::toString).orElse(null);
        this.previousTag = previousVersion.map(v -> TagConvention.tagFor(v.toString(), tagPrefix)).orElse(null);
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
     * Not using Optional here because it makes the client logic simpler.
     */
    @Nullable
    String getPreviousVersion() {
        return previousVersion;
    }

    /**
     * Previous tag.
     * Returned value is the previous version with optional tag prefix.
     * Shipkit and Mockito projects use 'v1.2.3' naming convention for tags
     * so tag for version 1.0.0 would be v1.0.0.
     * The returned previous tag value can be null when previous version is null:
     * {@link #getPreviousVersion()}
     */
    @Nullable
    String getPreviousTag() {
        return previousTag;
    }
}