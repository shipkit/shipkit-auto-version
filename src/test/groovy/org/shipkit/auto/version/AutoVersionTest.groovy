package org.shipkit.auto.version

import org.gradle.api.logging.Logger

class AutoVersionTest extends TmpFolderSpecification {

    ProcessRunner runner = Mock()
    File versionFile
    AutoVersion autoVersion
    def log = Mock(Logger)

    def setup() {
        versionFile = writeFile("")
        autoVersion = new AutoVersion(runner, versionFile)
    }

    def "uses raw version if no wildcard in version file"() {
        versionFile << "version=1.5.0-SNAPSHOT"

        when:
        def v = autoVersion.deductVersion(log)

        then:
        v.version == "1.5.0-SNAPSHOT"
        v.previousVersion == null
        1 * log.lifecycle("Building version '1.5.0-SNAPSHOT'\n" +
                "  - reason: shipkit-auto-version uses verbatim version from '$versionFile.name' file")
    }

    def "uses '0' if no tag found"() {
        versionFile << "version=1.1.*"
        runner.run("git", "tag") >> """
v1.0.0
v1.0.1
"""

        when:
        def v = autoVersion.deductVersion(log)

        then:
        v.version == "1.1.0"
        v.previousVersion == null
        1 * log.lifecycle("Building version '1.1.0'\n" +
                "  - reason: shipkit-auto-version found no tags")
    }

    def "increments version"() {
        versionFile << "version=2.0.*"
        runner.run("git", "tag") >> "v2.0.0"
        runner.run("git", "log", "--pretty=oneline", "v2.0.0..HEAD") >> """
some commit #1
some commit #2
"""

        when:
        def v = autoVersion.deductVersion(log)

        then:
        v.version == "2.0.2"
        v.previousVersion == "2.0.0"
        1 * log.lifecycle("Building version '2.0.2'\n" +
                "  - reason: shipkit-auto-version deducted version based on previous tag: '2.0.0'")
    }

    def "no build failure when deducting versions fails"() {
        versionFile << "version=1.0.*"

        when:
        def v = autoVersion.deductVersion(log)

        then:
        v.version == "1.0.unspecified"
        v.previousVersion == null
        1 * log.debug("shipkit-auto-version caught an exception, falling back to reasonable default", _ as Exception)
        1 * log.lifecycle("Building version '1.0.unspecified'\n" +
                "  - reason: shipkit-auto-version caught an exception, falling back to reasonable default\n" +
                "  - run with --debug for more info")
    }
}
