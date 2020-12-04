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

    DeductedVersion(String version, Optional<Version> previousVersion) {
        Objects.requireNonNull(version, "version cannot be null");
        this.version = version;
        this.previousVersion = previousVersion.map(Version::toString).orElse(null);
        this.previousTag = previousVersion.map(v -> TagConvention.tagFor(v.toString())).orElse(null);
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

    @Nullable
    String getPreviousTag() {
        return previousTag;
    }
}