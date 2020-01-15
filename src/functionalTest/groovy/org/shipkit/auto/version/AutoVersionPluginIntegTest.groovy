package org.shipkit.auto.version

import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class AutoVersionPluginIntegTest extends Specification {

    @Rule TemporaryFolder tmp = new TemporaryFolder()

    def setup() {
        file("settings.gradle")
        file("build.gradle") << """
            plugins {  id('org.shipkit.auto.version')}
        """
    }

    def "runs task"() {
        file("version.properties") << "version=1.0.*"
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
