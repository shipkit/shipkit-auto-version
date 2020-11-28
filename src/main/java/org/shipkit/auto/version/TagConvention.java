package org.shipkit.auto.version;

/**
 * Currently supported tag naming convention.
 * Encapsulated here so that we can support different conventions in the future.
 */
class TagConvention {

    /**
     * Informs if given git tag is supported
     */
    static boolean isVersionTag(String gitTag) {
        return gitTag.startsWith("v");
    }

    /**
     * Creates Git tag name based on supported convention
     */
    static String tagFor(String version) {
        return "v" + version;
    }
}
