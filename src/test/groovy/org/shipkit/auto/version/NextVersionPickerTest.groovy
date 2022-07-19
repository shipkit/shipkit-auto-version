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
                Optional.empty(), new VersionConfig("1.0.*", "v"), "1.1.0")

        then:
        v == "1.1.0"
        1 * log.lifecycle("Building version '1.1.0'\n" +
                "  - reason: shipkit-auto-version uses version already specified in the Gradle project")
    }

    def "picks version 'as is' because the wildcard is not used"() {
        when:
        def v = picker.pickNextVersion(
                Optional.empty(), new VersionConfig("1.0.0", "v"), Project.DEFAULT_VERSION)

        then:
        v == "1.0.0"
        1 * log.lifecycle("Building version '1.0.0'\n" +
                "  - reason: shipkit-auto-version uses verbatim version from version file")
    }

    def "picks new patch version when no previous version"() {
        when:
        def v = picker.pickNextVersion(
                Optional.empty(), new VersionConfig("1.0.*", "v"), Project.DEFAULT_VERSION)

        then:
        v == "1.0.0"
        1 * log.lifecycle("Building version '1.0.0'\n" +
                "  - reason: shipkit-auto-version found no tags matching version spec: '1.0.*'")
    }

    def "picks new patch version when no matching previous versions"() {
        when:
        def v = picker.pickNextVersion(
                Optional.of(new VersionNumber("0.0.9")),
                new VersionConfig("1.0.*", "v"),
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
                Optional.of(new VersionNumber("1.0.0")),
                new VersionConfig("1.0.*", "v"),
                Project.DEFAULT_VERSION)

        then:
        v == "1.0.2"
        1 * log.lifecycle("Building version '1.0.2'\n" +
                "  - reason: shipkit-auto-version deduced version based on previous tag: '1.0.0'")
    }

    def "picks version when no tag prefix"() {
        runner.run("git", "log", "--pretty=oneline", "1.0.0..HEAD") >> """
some commit
"""

        when:
        def v = picker.pickNextVersion(
                Optional.of(new VersionNumber("1.0.0")),
                new VersionConfig("1.0.*", ""),
                Project.DEFAULT_VERSION)

        then:
        v == "1.0.1"
    }

    def "picks version when no config file and not checked out on tag"() {
        runner.run("git", "describe", "--tags") >> "v1.1.0-1-sha12345"

        when:
        def v = picker.pickNextVersion(Optional.empty(),
                new VersionConfig(null,"v"),
                Project.DEFAULT_VERSION)

        then:
        v == "1.1.1-SNAPSHOT"
    }

    def "picks 4-part version when no config file and not checked out on tag"() {
        runner.run("git", "describe", "--tags") >> "v1.2.3.4-1-sha12345"

        when:
        def v = picker.pickNextVersion(Optional.empty(),
                new VersionConfig(null,"v"),
                Project.DEFAULT_VERSION)

        then:
        v == "1.2.3.5-SNAPSHOT"
    }

    def "picks version when no config file and checked out on tag"() {
        runner.run("git", "describe", "--tags") >> "v1.1.0"

        when:
        def v = picker.pickNextVersion(Optional.empty(),
                new VersionConfig(null,"v"),
                Project.DEFAULT_VERSION)

        then:
        v == "1.1.0"
    }

    def "picks version when no config file and checked out on not valid tag"() {
        runner.run("git", "describe", "--tags") >> "ver-1.1.0"

        when:
        def v = picker.pickNextVersion(Optional.empty(),
                new VersionConfig(null,"v"),
                Project.DEFAULT_VERSION)

        then:
        v == "0.0.1-SNAPSHOT"
    }

    def "picks version when no tags found in project"() {
        runner.run("git", "describe", "--tags") >> {
            throw new ShipkitAutoVersionException("Problems executing command")
        }

        when:
        def v = picker.pickNextVersion(Optional.empty(),
                new VersionConfig(null,"v"),
                Project.DEFAULT_VERSION)

        then:
        v == "0.0.1-SNAPSHOT"

    }
}
