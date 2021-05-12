package org.shipkit.auto.version;

/**
 * Currently supported tag naming convention.
 */
class TagConvention {

    /**
     * Creates Git tag name based on supported convention
     */
    static String tagFor(String version, String tagPrefix) {
        return tagPrefix + version;
    }
}
