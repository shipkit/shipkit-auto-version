package org.shipkit.auto.version;

import com.github.zafarkhaja.semver.Version;

import java.util.Optional;
import java.util.TreeSet;
import java.util.regex.Pattern;

class NearestTagFinder {

    Optional<Version> findTag(Iterable<String> tags, String spec) {
        //TODO validate spec, must have 1 wildcard at the end of the spec
        Pattern pattern = Pattern.compile(spec.replaceAll("\\*", "\\\\d+"));
        TreeSet<Version> candidates = new TreeSet<>();
        for (String tag : tags) {
            if (tag.startsWith("v")) {
                tag = tag.substring(1);
            } else {
                continue; //only support tags with 'v' prefix
            }
            if (pattern.matcher(tag).matches()) {
                //TODO: error handling when version is bad
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
