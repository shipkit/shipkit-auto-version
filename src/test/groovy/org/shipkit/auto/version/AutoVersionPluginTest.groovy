package org.shipkit.auto.version

import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class AutoVersionPluginTest extends Specification {

    def project = ProjectBuilder.builder().build()

    def "plugin gets applied"() {
        when:
        project.plugins.apply(AutoVersionPlugin)

        then:
        project.version == "1.0.1"
    }
}
