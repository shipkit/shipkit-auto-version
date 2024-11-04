package org.shipkit.auto.version

import org.gradle.api.provider.Provider
import spock.lang.Specification

class VersionsProviderTest extends Specification {

    GitValueSourceProviderFactory gitValueSourceProviderFactory = Mock(GitValueSourceProviderFactory)
    Provider<String> provider = Mock()
    VersionsProvider versionsProvider

    def setup() {
        versionsProvider = new VersionsProvider(gitValueSourceProviderFactory)
    }

    def "returns empty collection when no tags"() {
        gitValueSourceProviderFactory.getProvider(["tag"]) >> provider
        provider.get() >> ""

        expect:
        versionsProvider.getAllVersions("v").isEmpty()
    }

    def "gets all versions from tags"() {
        gitValueSourceProviderFactory.getProvider(["tag"]) >> provider
        provider.get() >> """
            v1.0.1
            v1.0.2
            v2.0.0
            v22.333.4444
            v1.0.0.0
            
            foo
            1.0.0
            v1.0            
            v1.0.0-beta
        """

        expect:
        versionsProvider.getAllVersions("v").toString() == "[1.0.0.0, 1.0.1, 1.0.2, 2.0.0, 22.333.4444]"
    }

    def "gets all versions when no tag prefix"() {
        gitValueSourceProviderFactory.getProvider(["tag"]) >> provider
        provider.get() >> """
            1.0.1
            1.0.2
            foo
            1.0.0-beta
        """

        expect:
        versionsProvider.getAllVersions("").toString() == "[1.0.1, 1.0.2]"
    }
}
