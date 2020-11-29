package org.shipkit.auto.version

import com.github.zafarkhaja.semver.Version
import spock.lang.Specification

class DeductedVersionTest extends Specification {

    def "nullable previous version"() {
        expect:
        "0.0.9" == new DeductedVersion("1.0.0", Optional.of(Version.valueOf("0.0.9"))).previousVersion
        null == new DeductedVersion("1.0.0", Optional.empty()).previousVersion
    }
}
