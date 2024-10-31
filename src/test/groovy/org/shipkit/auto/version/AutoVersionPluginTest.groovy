package org.shipkit.auto.version


import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class AutoVersionPluginTest extends Specification {

    def project = ProjectBuilder.builder().build()

    //This test should remain super simple to e2e test the happy path, and *not* depend on Gradle API.
    //Don't add new e2e test methods or complicate the setup/test data.
    //New functionalities should be unit tested in other, lower-level tests.
    def "e2e test"() {
        project.file("version.properties") << "version=1.0.*"
        def runner = new ProcessRunner(project.projectDir)

        //prepare repo
        runner.run("git", "init", "--initial-branch=main")
        runner.run("git", "config", "user.email", "dummy@testing.com")
        runner.run("git", "config", "user.name", "Dummy For Testing")

        runner.run("git", "add", "*")
        runner.run("git", "commit", "-a", "-m", "initial")
        runner.run("git", "tag", "v1.0.0")

        //simulate pull request merge
        runner.run("git", "checkout", "-b", "PR-10")
        runner.run("git", "commit", "--allow-empty", "-m", "PR-10 - 1")
        runner.run("git", "commit", "--allow-empty", "-m", "PR-10 - 2")

        runner.run("git", "checkout", "main")
        runner.run("git", "merge", "PR-10", "--no-ff", "-m", "Merge pull request #10 from ...")

        when:
        project.getPlugins().apply(AutoVersionPlugin)

        then:
        project.version == "1.0.1"
        project.ext.'shipkit-auto-version.previous-version' == "1.0.0"
        project.ext.'shipkit-auto-version.previous-tag' == "v1.0.0"
    }

    def "with empty tag prefix"() {
        project.file("version.properties") << """
version=1.0.*
tagPrefix=
"""
        def runner = new ProcessRunner(project.projectDir)

        //prepare repo
        runner.run("git", "init")
        runner.run("git", "config", "user.email", "dummy@testing.com")
        runner.run("git", "config", "user.name", "Dummy For Testing")

        runner.run("git", "add", "*")
        runner.run("git", "commit", "-a", "-m", "initial")
        runner.run("git", "tag", "1.0.0")

        runner.run("git", "commit", "--allow-empty", "-m", "some change")

        when:
        project.getPlugins().apply(AutoVersionPlugin)

        then:
        project.version == "1.0.1"
        project.ext.'shipkit-auto-version.previous-version' == "1.0.0"
        project.ext.'shipkit-auto-version.previous-tag' == "1.0.0"
    }
}
