package org.shipkit.auto.version

import spock.lang.Unroll

import static VersionConfig.parseVersionFile
import static VersionConfig.isSupportedVersion

class VersionConfigTest extends TmpFolderSpecification {

    def "loads spec from file"() {
        expect:
        parseVersionFile(writeFile("version=1.0.*")).toString() == "1.0.*"
    }

    def "no file"() {
        when:
        def configResult = parseVersionFile(new File("missing file"))

        then:
        !configResult.getRequestedVersion().isPresent()
        configResult.getTagPrefix() == "v"
    }

    def "missing 'version' property"() {
        when:
        def f = writeFile("noversion=missing")
        def configResult = parseVersionFile(f)

        then:
        !configResult.getRequestedVersion().isPresent()
        configResult.getTagPrefix() == "v"
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
        new VersionConfig(spec, "v")

        then:
        def e = thrown(ShipkitAutoVersionException)
        e.message == "[shipkit-auto-version] Invalid version specification: '$spec'\n" +
                "  Correct examples: '1.0.*', '2.10.100'"
        e.cause != null

        where:
        spec << ["foo.version", "1.2", "1.2.**", "1.*.*", "1.0.0-beta.*", "1.12*"]
    }
}
