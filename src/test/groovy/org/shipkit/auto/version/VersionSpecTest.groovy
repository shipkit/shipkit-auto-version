package org.shipkit.auto.version

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class VersionSpecTest extends Specification {

    @Rule TemporaryFolder tmp = new TemporaryFolder()

    File file(String content) {
        def f = tmp.newFile()
        f << content
        f
    }

    def "loads spec from file"() {
        expect:
        new VersionSpec(file(spec)).wildcard == wildcard

        where:
        spec                        | wildcard
        "version=1.0.0"             | false
        "version=1.0.*"             | true
    }

    def "supported formats"() {
        expect:
        VersionSpec.validVersion(spec).matches() == ok

        where:
        spec                | ok
        "1.0.0"             | true
        "1.0.0-SNAPSHOT"    | true
        "1.0.0-beta.1"      | true
        "1.0.0-RC.1"        | true
        "1.0.0.x.y.z"       | true
        "1.0.*"             | true

        "1.2"               | false
        "1.2.foo"           | false
        "1.0.*-SNAPSHOT"    | false
        "1.0.*-beta.1"      | false
        "1.0.0-beta.*"      | false //TODO: potentially support in the future
    }

    def "no file"() {
        when:
        new VersionSpec(new File("missing file"))

        then:
        def e = thrown(RuntimeException)
        e.message == "'org.shipkit.auto.version' plugin requires this file: missing file"
    }

    def "bad format"() {
        def f = file("noversion=missing")

        when:
        new VersionSpec(f)

        then:
        def e = thrown(VersionSpec.IncorrectVersionFile)
        e.message == "'org.shipkit.auto.version' expects correct 'version' property in file: " + f + "\n" +
                "Correct examples: 'version=1.0.*', 'version=2.10.100'"
    }

    def "bad version format"() {
        when:
        new VersionSpec(file('1.2.foo'))

        then:
        thrown(VersionSpec.IncorrectVersionFile)
    }
}
