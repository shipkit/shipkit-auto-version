package org.shipkit.auto.version

import spock.lang.Specification

class DeducedVersionTest extends Specification {

    def "previous version"() {
        expect:
        "0.0.9" == new DeducedVersion("1.0.0", Optional.of(new VersionNumber("0.0.9")), "v").previousVersion
        null == new DeducedVersion("1.0.0", Optional.empty(), "v").previousVersion
    }

    def "previous tag"() {
        expect:
        "v0.0.9" == new DeducedVersion("1.0.0", Optional.of(new VersionNumber("0.0.9")), "v").previousTag
        "0.0.9" == new DeducedVersion("1.0.0", Optional.of(new VersionNumber("0.0.9")), "").previousTag
        null == new DeducedVersion("1.0.0", Optional.empty(), "v").previousTag
    }
}
