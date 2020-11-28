package org.shipkit.auto.version

import spock.lang.Unroll

import static RequestedVersion.parseVersionFile
import static org.shipkit.auto.version.RequestedVersion.isSupportedVersion

class RequestedVersionTest extends TmpFolderSpecification {

    def "loads spec from file"() {
        expect:
        parseVersionFile(writeFile("version=1.0.*")).toString() == "1.0.*"
    }

    def "no file"() {
        when:
        parseVersionFile(new File("missing file"))

        then:
        def e = thrown(ShipkitAutoVersionException)
        e.message == "[shipkit-auto-version] Please create file 'version.properties' with a valid 'version' property, " +
                "for example 'version=1.0.*'"
        e.cause != null
    }

    def "missing 'version' property"() {
        def f = writeFile("noversion=missing")

        when:
        parseVersionFile(f)

        then:
        def e = thrown(ShipkitAutoVersionException)
        e.message == "[shipkit-auto-version] File '" + f.name + "' is missing the 'version' property\n" +
                "  Correct examples: 'version=1.0.*', 'version=2.10.100'"
    }

    def "supports select types of versions"() {
        expect:
        isSupportedVersion(spec) == result

        where:
        spec        | result

        '1.0.0'     | true
        '2.33.444'  | true

        'x'         | false
        '1.0'       | false
        '1.0.0-rc'  | false
    }

    @Unroll
    def "bad version format: #spec"() {
        when:
        new RequestedVersion(spec)

        then:
        def e = thrown(ShipkitAutoVersionException)
        e.message == "[shipkit-auto-version] Invalid version specification: '$spec'\n" +
                "  Correct examples: '1.0.*', '2.10.100'"
        e.cause != null

        where:
        spec << ["foo.version", "1.2", "1.2.**", "1.*.*", "1.0.0-beta.*", "1.12*"]
    }
}
