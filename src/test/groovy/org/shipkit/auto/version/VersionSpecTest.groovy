package org.shipkit.auto.version


import static VersionSpec.readVersionSpec

class VersionSpecTest extends TmpFolderSpecification {

    def "loads spec from file"() {
        expect:
        readVersionSpec(writeFile("version=1.0.*")) == "1.0.*"
    }

    def "no file"() {
        when:
        readVersionSpec(new File("missing file"))

        then:
        def e = thrown(RuntimeException)
        e.message == "Problems deducting the version automatically. Missing file: missing file"
        e.cause != null
    }

    def "bad format"() {
        def f = writeFile("noversion=missing")

        when:
        readVersionSpec(f)

        then:
        def e = thrown(VersionSpec.MissingVersionKey)
        e.message == "Problems deducting the version automatically. Expected correct 'version' property in file: " + f + "\n" +
                "Correct examples: 'version=1.0.*', 'version=2.10.100'"
    }

    def "bad version format"() {
        def f = writeFile("version=" + spec)

        when:
        readVersionSpec(f)

        then:
        def e = thrown(VersionSpec.IncorrectVersionFormat)
        e.message == "Problems deducting the version automatically. Expected correct 'version' property in file: " + f + "\n" +
                "Correct examples: 'version=1.0.*', 'version=2.10.100'"
        e.cause != null

        where:
        spec << ["foo.version", "1.2", "1.2.**", "1.*.*", "1.0.0-beta.*"]
    }
}
