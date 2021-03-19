package org.shipkit.auto.version

import com.github.zafarkhaja.semver.Version
import spock.lang.Specification

class DeductedVersionTest extends Specification {

    def "previous version"() {
        expect:
        "0.0.9" == new DeductedVersion("1.0.0", Optional.of(Version.valueOf("0.0.9")), "v").previousVersion
        null == new DeductedVersion("1.0.0", Optional.empty(), "v").previousVersion
    }

    def "previous tag"() {
        expect:
        "v0.0.9" == new DeductedVersion("1.0.0", Optional.of(Version.valueOf("0.0.9")), "v").previousTag
        "0.0.9" == new DeductedVersion("1.0.0", Optional.of(Version.valueOf("0.0.9")), "").previousTag
        null == new DeductedVersion("1.0.0", Optional.empty(), "v").previousTag
    }
}
