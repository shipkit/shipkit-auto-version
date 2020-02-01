package org.shipkit.auto.version

import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

/**
 * Only smoke test, forever! Don't add more tests here, instead cover the complexity in lower level unit tests.
 */
class AutoVersionPluginIntegTest extends Specification {

    @Rule TemporaryFolder tmp = new TemporaryFolder()

    def setup() {
        file("settings.gradle")
        file("build.gradle") << """
            plugins {  id('org.shipkit.shipkit-auto-version')}
        """
    }

    def "uses explicit version declared in the version file"() {
        file("version.properties") << "version=1.0.1"
        file("build.gradle") << """
            task showVersion { doLast { println "version: " + project.version }}
        """

        given:
        def result = run("showVersion")

        expect:
        result.output.contains("version: 1.0.1")
    }

    File file(String path) {
        def f = new File(rootDir, path)
        if (!f.exists()) {
            f.parentFile.mkdirs()
            f.createNewFile()
            assert f.exists()
        }
        return f
    }

    File getRootDir() {
        return tmp.root
    }

    def run(String ...args) {
        def runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments(args)
        runner.withProjectDir(rootDir)
        return runner.build()
    }
}
