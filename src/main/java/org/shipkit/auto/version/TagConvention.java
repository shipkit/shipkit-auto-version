package org.shipkit.auto.version;

/**
 * Currently supported tag naming convention.
 */
class TagConvention {

    /**
     * Informs if given git tag is supported
     */
    static boolean isVersionTag(String gitTag, String tagPrefix) {
        return gitTag.startsWith(tagPrefix);
    }

    /**
     * Creates Git tag name based on supported convention
     */
    static String tagFor(String version, String tagPrefix) {
        return tagPrefix + version;
    }
}
