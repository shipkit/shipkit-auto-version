package org.shipkit.auto.version


import spock.lang.Specification

class VersionsProviderTest extends Specification {

    ProcessRunner runner = Mock()
    VersionsProvider provider

    def setup() {
        provider = new VersionsProvider(runner)
    }

    def "returns empty collection when no tags"() {
        runner.run("git", "tag") >> ""

        expect:
        provider.getAllVersions("v").isEmpty()
    }

    def "gets all versions from tags"() {
        runner.run("git", "tag") >> """
            v1.0.1
            v1.0.2
            v2.0.0
            v22.333.4444
            
            foo
            1.0.0
            v1.0
            v1.0.0.0
            v1.0.0-beta
        """

        expect:
        provider.getAllVersions("v").toString() == "[1.0.1, 1.0.2, 2.0.0, 22.333.4444]"
    }

    def "gets all versions when no tag prefix"() {
        runner.run("git", "tag") >> """
            1.0.1
            1.0.2
            foo
            1.0.0-beta
        """

        expect:
        provider.getAllVersions("").toString() == "[1.0.1, 1.0.2]"
    }
}
