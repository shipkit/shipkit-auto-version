package org.shipkit.auto.version

import spock.lang.Unroll

import static VersionConfig.parseVersionFile
import static VersionConfig.isSupportedVersion
import static org.shipkit.auto.version.VersionConfig.isSnapshot

class VersionConfigTest extends TmpFolderSpecification {

    def "loads spec from file"() {
        expect:
        parseVersionFile(writeFile("version=1.0.*")).toString() == "1.0.*"
    }

    def "no file"() {
        when:
        def configResult = parseVersionFile(new File("missing file"))

        then:
        !configResult.getVersionSpec().isPresent()
        configResult.getTagPrefix() == "v"
    }

    def "missing version property"() {
        when:
        def f = writeFile("noversion=missing")
        def configResult = parseVersionFile(f)

        then:
        !configResult.getVersionSpec().isPresent()
        configResult.getTagPrefix() == "v"
    }

    def "version property empty"() {
        when:
        def f = writeFile("version=")
        def configResult = parseVersionFile(f)

        then:
        !configResult.getVersionSpec().isPresent()
        configResult.getTagPrefix() == "v"
    }

    def "knows if has wildcard"() {
        expect:
        new VersionConfig(versionSpec, "").isWildcard() == isWildcard

        where:
        versionSpec     | isWildcard
        "1.2.3"         | false
        "1.22.3.44"     | false
        "1.22.3.*"      | true
        "1.22.*"        | true
    }

    def "supports select types of versions"() {
        expect:
        isSupportedVersion(tag, tagPrefix) == result

        where:
        tag         | tagPrefix  | result

        'v1.0.0'    | 'v'        | true
        'v2.33.444' | 'v'        | true
        '1.2.3'     | ''         | true
        'ver-1.2.3' | 'ver-'     | true
        'v1.2.3.44' | 'v'        | true

        '1.2.3'     | 'v'        | false
        'v1.2.3.4.5'| 'v'        | false
        'v1.1.0'    | ''         | false
        'ver-1.2.3' | 'v'        | false
        'x'         | ''         | false
        'v1.0'      | 'v'        | false
        'v1.0.0-rc' | 'v'        | false
    }

    def "tags are not annotated and snapshots can be deducted"() {
        expect:
        isSnapshot(tag, tagPrefix) == result

        where:
        tag                     | tagPrefix  | result

        'v1.0.0-1-sha123'       | 'v'        | true
        '2.33.444-12-fw6i89op'  | ''         | true
        '1.2.3.4-12-fw6i89op'   | ''         | true

        'v1.0.0-1-sha123'       | 'ver-'     | false
        'v1.0.0'                | 'v'        | false
        'v1.0-2-sha123'         | 'v'        | false
        'v1.0.0-rc'             | 'v'        | false
    }

    @Unroll
    def "bad version format: #spec"() {
        when:
        new VersionConfig(spec, "v")

        then:
        def e = thrown(ShipkitAutoVersionException)
        e.message.startsWith "[shipkit-auto-version] Invalid version specification: '$spec'\n"
        e.message.contains "Correct examples: '1.0.*'"
        e.cause != null

        where:
        spec << ["foo.version", "1.2", "1.2.**", "1.*.*", "1.0.0-beta.*", "1.12*"]
    }
}
