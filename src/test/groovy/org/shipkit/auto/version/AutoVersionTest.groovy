package org.shipkit.auto.version

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
        runner.run("git", "log", "--pretty=oneline", "v2.0.0..master") >> """
some commit #1
some commit #2
"""

        expect:
        autoVersion.deductVersion() == "2.0.2"
    }
}
