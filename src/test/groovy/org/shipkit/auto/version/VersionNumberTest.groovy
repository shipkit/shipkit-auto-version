package org.shipkit.auto.version


import spock.lang.Specification

import static org.shipkit.auto.version.VersionNumber.*

class VersionNumberTest extends Specification {

    def "validates version"() {
        expect:
        try {
            new VersionNumber(version)
        } catch (UnsupportedVersionException e) {
            assert e.message.contains(version)
        }

        where:
        version << ["", "foo", "1", "1.2", "1.2.3.4.5"]
    }

    def "string representation"() {
        expect:
        new VersionNumber(version).toString() == text

        where:
        version        | text
        "1.2.3"        | "1.2.3"
        "1.2.3-build1" | "1.2.3-build1"
        "1.2.3.4"      | "1.2.3.4"
    }

    def "increments by specific count"() {
        expect:
        new VersionNumber(version).incrementBy(count).toString() == result

        where:
        version        | count  | result
        "1.2.3"        | 0      | "1.2.3"
        "1.2.3"        | 1      | "1.2.4"
        "1.2.3"        | 10     | "1.2.13"

        "1.2.3.4"      | 0      | "1.2.3.4"
        "1.2.3.4"      | 1      | "1.2.3.5"
        "1.2.3.4"      | 10     | "1.2.3.14"
    }

    def "different numbers comparison"() {
        expect:
        new VersionNumber(left) > new VersionNumber(right)
        new VersionNumber(right) < new VersionNumber(left)

        where:
        left        |right
        "1.2.3"     |"1.2.2"
        "5.0.0"     |"4.9.9"
        "1.2.3"     |"1.2.2.4"
        "1.2.3.4"   |"1.2.2.3"
    }

    def "equal numbers comparison"() {
        expect:
        new VersionNumber(left) == new VersionNumber(right)
        new VersionNumber(right) == new VersionNumber(left)

        where:
        left        |right
        "1.2.3"     |"1.2.3"
        "1.2.3.4"   |"1.2.3.4"
    }

    def "satisfies spec"() {
        expect:
        new VersionNumber(version).satisfies(spec) == result

        where:
        version     | spec       | result
        '1.0.0'     | '1.0.0'    | false
        '1.0.0'     | '1.0.*'    | true
        '1.0.4'     | '1.0.*'    | true
        '1.0.0.0'   | '1.0.0.*'  | true
        '1.0.0.5'   | '1.0.0.*'  | true
        '1.0.0'     | '1.0.0.*'  | false
        '1.0.0.0'   | '1.0.*'    | false
    }
}
