package org.shipkit.auto.version;

import java.util.Collection;
import java.util.Optional;

/**
 * Finds previous version
 */
class PreviousVersionFinder {

    /**
     * Finds previous version based on the version specification
     */
    Optional<VersionNumber> findPreviousVersion(Collection<VersionNumber> versions, VersionConfig config) {
        if (config.getVersionSpec().isPresent() && !config.isWildcard()) {
            //Requested version is a concrete version like 1.0.0 (no wildcard).
            //We just find the previous version
            return findPrevious(versions, new VersionNumber(config.getVersionSpec().get()));
        }

        Optional<VersionNumber> max = versions.stream()
                .filter(v -> v.satisfies(config.getVersionSpec().get()))
                .max(VersionNumber::compareTo);

        if (max.isPresent()) {
            return max; //we found it! just return.
        }

        //We did not find it. This happens in example scenario:
        // versions are 0.0.1, 0.0.2 and the requested version is 0.1.*
        String newPatchVersion = config.newPatchVersion();
        return findPrevious(versions, new VersionNumber(newPatchVersion));
    }

    private Optional<VersionNumber> findPrevious(Collection<VersionNumber> versions, VersionNumber version) {
        return versions.stream()
                .filter(v -> v.compareTo(version) < 0)
                .max(VersionNumber::compareTo);
    }
}
