package org.shipkit.auto.version

import com.github.zafarkhaja.semver.Version
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import spock.lang.Specification

class NextVersionPickerTest extends Specification {

    ProcessRunner runner = Mock()
    NextVersionPicker picker
    Logger log = Mock()

    def setup() {
        picker = new NextVersionPicker(runner, log)
    }

    def "picks version as configured on the Gradle project"() {
        when:
        def v = picker.pickNextVersion(
                Optional.empty(), new RequestedVersion("1.0.*"), "1.1.0")

        then:
        v == "1.1.0"
        1 * log.lifecycle("Building version '1.1.0'\n" +
                "  - reason: shipkit-auto-version uses version already specified in the Gradle project")
    }

    def "picks version 'as is' because the wildcard is not used"() {
        when:
        def v = picker.pickNextVersion(
                Optional.empty(), new RequestedVersion("1.0.0"), Project.DEFAULT_VERSION)

        then:
        v == "1.0.0"
        1 * log.lifecycle("Building version '1.0.0'\n" +
                "  - reason: shipkit-auto-version uses verbatim version from version file")
    }

    def "picks new patch version when no previous version"() {
        when:
        def v = picker.pickNextVersion(
                Optional.empty(), new RequestedVersion("1.0.*"), Project.DEFAULT_VERSION)

        then:
        v == "1.0.0"
        1 * log.lifecycle("Building version '1.0.0'\n" +
                "  - reason: shipkit-auto-version found no tags matching version spec: '1.0.*'")
    }

    def "picks new patch version when no matching previous versions"() {
        when:
        def v = picker.pickNextVersion(
                Optional.of(Version.valueOf("0.0.9")),
                new RequestedVersion("1.0.*"),
                Project.DEFAULT_VERSION)

        then:
        v == "1.0.0"
        1 * log.lifecycle("Building version '1.0.0'\n" +
                "  - reason: shipkit-auto-version found no tags matching version spec: '1.0.*'")
    }

    def "picks incremented version"() {
        runner.run("git", "log", "--pretty=oneline", "v1.0.0..HEAD") >> """
some commit #1
some commit #2
"""

        when:
        def v = picker.pickNextVersion(
                    Optional.of(Version.valueOf("1.0.0")),
                    new RequestedVersion("1.0.*"),
                    Project.DEFAULT_VERSION)

        then:
        v == "1.0.2"
        1 * log.lifecycle("Building version '1.0.2'\n" +
                "  - reason: shipkit-auto-version deducted version based on previous tag: '1.0.0'")
    }
}
