package org.shipkit.auto.version;

import com.github.zafarkhaja.semver.Version;

import java.util.Collection;
import java.util.Optional;

/**
 * Finds previous version
 */
class PreviousVersionFinder {

    /**
     * Finds previous version based on the version specification
     */
    Optional<Version> findPreviousVersion(Collection<Version> versions, VersionConfig config) {
        if (!config.isWildcard()) {
            //Requested version is a concrete version like 1.0.0 (no wildcard).
            //We just find the previous version
            return findPrevious(versions, Version.valueOf(config.toString()));
        }

        Optional<Version> max = versions.stream()
                .filter(v -> v.satisfies(config.toString()))
                .max(Version::compareTo);

        if (max.isPresent()) {
            return max; //we found it! just return.
        }

        //We did not find it. This happens in example scenario:
        // versions are 0.0.1, 0.0.2 and the requested version is 0.1.*
        String newPatchVersion = config.newPatchVersion();
        return findPrevious(versions, Version.valueOf(newPatchVersion));
    }

    private Optional<Version> findPrevious(Collection<Version> versions, Version version) {
        return versions.stream()
                .filter(v -> v.lessThan(version))
                .max(Version::compareTo);
    }
}
