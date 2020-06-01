package org.shipkit.auto.version

import org.gradle.api.logging.Logger

class AutoVersionTest extends TmpFolderSpecification {

    ProcessRunner runner = Mock()
    File versionFile
    AutoVersion autoVersion

    def setup() {
        versionFile = writeFile("")
        autoVersion = new AutoVersion(runner, versionFile)
    }

    def "uses raw version if no wildcard in version file"() {
        versionFile << "version=1.5.0-SNAPSHOT"

        expect:
        autoVersion.deductVersion() == "1.5.0-SNAPSHOT"
    }

    def "uses '0' if no tag found"() {
        versionFile << "version=1.1.*"
        runner.run("git", "tag") >> """
v1.0.0
v1.0.1
"""

        expect:
        autoVersion.deductVersion() == "1.1.0"
    }

    def "increments version"() {
        versionFile << "version=2.0.*"
        runner.run("git", "tag") >> "v2.0.0"
        runner.run("git", "log", "--pretty=oneline", "v2.0.0..HEAD") >> """
some commit #1
some commit #2
"""

        expect:
        autoVersion.deductVersion() == "2.0.2"
    }

    def "no build failure when deducting versions fails"() {
        def log = Mock(Logger)
        versionFile << "version=1.0.*"

        when:
        def v = autoVersion.deductVersion(log)

        then:
        v == "1.0.unspecified"
        1 * log.debug("shipkit-auto-version was unable to deduct the version due to an exception", _ as Exception)
        1 * log.lifecycle("shipkit-auto-version was unable to deduct the version due to an exception.\n" +
                "  - setting version to '1.0.unspecified' (run with --debug for more info)")
    }
}
