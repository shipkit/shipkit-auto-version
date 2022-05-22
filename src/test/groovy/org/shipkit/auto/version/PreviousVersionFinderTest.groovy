package org.shipkit.auto.version


import spock.lang.Specification

class PreviousVersionFinderTest extends Specification {

    def "finds previous of concrete version"() {
        expect:
        prev(["0.0.1", "0.0.2", "0.0.3", "0.0.4"], "0.0.3") == "0.0.2"
        prev(["0.0.1", "0.1.0", "0.1.1", "1.0.0"], "1.0.0") == "0.1.1"
        prev(["1.0.0"], "1.0.0") == null
        prev([], "1.0.0") == null
    }

    def "finds previous of wildcard version"() {
        expect:
        prev(["0.0.1", "0.0.2", "0.1.0"], "0.0.*") == "0.0.2"
        prev(["0.0.1", "0.0.2", "0.2.0"], "0.1.*") == "0.0.2"
        prev(["0.1.0"], "0.0.*") == null
    }

    String prev(Collection<String> versions, String target) {
        new PreviousVersionFinder().findPreviousVersion(
                versions.collect { new VersionNumber(it) },
                new VersionConfig(target, "v")).orElse(null)
    }
}
