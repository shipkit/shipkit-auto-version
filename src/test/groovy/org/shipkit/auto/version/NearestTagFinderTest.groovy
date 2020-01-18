package org.shipkit.auto.version

import spock.lang.Specification

class NearestTagFinderTest extends Specification {

    def "finds nearest tag"() {
        def tags = [
            "foo",
            "v1.0.0",
            "v1.0.1",
            "x1.0.2",
            "v1.0.2x",
            "v1.0.1-rc.1",
            "v2.0.0-beta.1",
            "v2.0.0-beta.2",
            "v2.0.0-RC.1",
            "v2.0.0",
            "v2.1.0",
            "v2.2.0",
            "v2.2.1"
        ]

        expect:
        new NearestTagFinder().findTag(tags, "1.0.*").get().toString() == "1.0.1"
        new NearestTagFinder().findTag(tags, "2.0.0-beta.*").get().toString() == "2.0.0-beta.2"
        new NearestTagFinder().findTag(tags, "2.0.*").get().toString() == "2.0.0"
        new NearestTagFinder().findTag(tags, "2.3.*").isPresent() == false
    }
}