package org.shipkit.auto.version;

import com.github.zafarkhaja.semver.Version;

import java.util.Optional;
import java.util.TreeSet;
import java.util.regex.Pattern;

import static org.shipkit.auto.version.VersionSpec.isWildcardSpec;

/**
 * Identifies the nearest tag based on the current tags and the version spec.
 */
class NearestTagFinder {

    /**
     * Finds the latest tag that matches the spec.
     * For example, given tags "v1.0.0" and "v1.0.1" and spec "1.0.*", the result is "1.0.1".
     * This method throws exception if the spec does not match the standard "major.minor.*",
     * examples of invalid specs: "1.*.0", "1.0.0-beta.*".
     *
     * @param tags existing tag name, usually retrieved by running 'git tag'
     *             Only tag names starting with 'v' (standard GitHub convention) are supported at the moment.
     * @param spec the spec mask we are trying to find the matching tag, example: '1.0.*'.
     *             Expected to have single '*' in place of the patch version.
     * @return version corresponding to a tag or an empty optional if matching tag cannot be found.
     */
    Optional<Version> findTag(Iterable<String> tags, String spec) {
        if (!isWildcardSpec(spec)) {
            throw new IllegalArgumentException("Invalid spec: '" + spec + "'. Correct examples: '1.0.*', '2.30.*'");
        }
        spec = spec.replace("*", "\\d+");
        spec = spec.replaceAll("\\.", "\\\\.");
        Pattern pattern = Pattern.compile(spec);
        TreeSet<Version> candidates = new TreeSet<>(); //takes care of sorting
        for (String tag : tags) {
            if (tag.startsWith("v")) {
                tag = tag.substring(1);
            } else {
                continue; //currently we only support tags with 'v' prefix
            }
            if (pattern.matcher(tag).matches()) {
                candidates.add(Version.valueOf(tag));
            }
        }
        if (candidates.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(candidates.last());
        }
    }
}
