package org.shipkit.auto.version

import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class AutoVersionPluginTest extends Specification {

    def project = ProjectBuilder.builder().build()

    //This test should remain super simple to e2e test the happy path.
    //Don't add new e2e test methods or complicate the setup/test data.
    //New functionalities should be unit tested in other, lower-level tests.
    def "e2e test"() {
        project.file("version.properties") << "version=1.0.*"
        def runner = new ProcessRunner(project.projectDir)

        //prepare repo
        runner.run("git", "init")
        runner.run("git", "add", "*")
        runner.run("git", "commit", "-a", "-m", "initial")
        runner.run("git", "tag", "v1.0.0")

        //simulate pull request merge
        runner.run("git", "checkout", "-b", "PR-10")
        runner.run("git", "commit", "--allow-empty", "-m", "PR-10 - 1")
        runner.run("git", "commit", "--allow-empty", "-m", "PR-10 - 2")

        runner.run("git", "checkout", "master")
        runner.run("git", "merge", "PR-10", "--no-ff", "-m", "Merge pull request #10 from ...")

        expect:
        new AutoVersion(project.projectDir).deductVersion() == "1.0.3"
        //TODO, implement smarter commit counting so that version above can be 1.0.1
    }
}
