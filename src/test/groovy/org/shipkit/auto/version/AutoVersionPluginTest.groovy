package org.shipkit.auto.version

import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class AutoVersionPluginTest extends Specification {

    def project = ProjectBuilder.builder().build()

    def "no wildcard in version file"() {
        project.file("version.properties") << "version=1.5.0-SNAPSHOT"

        when:
        project.plugins.apply(AutoVersionPlugin)

        then:
        project.version == "1.5.0-SNAPSHOT"
    }
}
