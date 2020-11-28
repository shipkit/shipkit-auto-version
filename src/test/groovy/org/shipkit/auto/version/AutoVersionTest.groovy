package org.shipkit.auto.version


import org.gradle.api.Project
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

    def "happy path smoke test"() {
        //others scenarios are covered in other test classes
        versionFile << "version=1.1.*"
        runner.run("git", "tag") >> "v1.0.1"

        when:
        def v = autoVersion.deductVersion(log, Project.DEFAULT_VERSION)

        then:
        v.version == "1.1.0"
        v.previousVersion == "1.0.1"
    }

    def "no build failure when deducting versions fails"() {
        versionFile << "version=1.0.*"
        runner.run("git", "tag") >> "v1.0.1"

        when:
        def v = autoVersion.deductVersion(log, Project.DEFAULT_VERSION)

        then:
        v.version == "1.0.unspecified"
        v.previousVersion == "1.0.1"
        1 * log.debug("shipkit-auto-version caught an exception, falling back to reasonable default", _ as Exception)
        1 * log.lifecycle("Building version '1.0.unspecified'\n" +
                "  - reason: shipkit-auto-version caught an exception, falling back to reasonable default\n" +
                "  - run with --debug for more info")
    }
}
