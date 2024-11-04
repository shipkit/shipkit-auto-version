package org.shipkit.auto.version


import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.provider.Provider

class AutoVersionTest extends TmpFolderSpecification {

    File versionFile
    AutoVersion autoVersion
    def log = Mock(Logger)
    GitValueSourceProviderFactory gitValueSourceProviderFactory = Mock(GitValueSourceProviderFactory)

    def setup() {
        versionFile = writeFile("")
        autoVersion = new AutoVersion(gitValueSourceProviderFactory, versionFile)
    }

    def "happy path smoke test"() {
        //others scenarios are covered in other test classes
        versionFile << "version=1.1.*"
        Provider<String> provider = Mock()
        gitValueSourceProviderFactory.getProvider(["tag"] as String[]) >> provider
        provider.get() >> "v1.0.1"

        when:
        def v = autoVersion.deduceVersion(log, Project.DEFAULT_VERSION)

        then:
        v.version == "1.1.0"
        v.previousVersion == "1.0.1"
    }

    def "happy path smoke test when no version property"() {
        //others scenarios are covered in other test classes
        versionFile << "version="
        Provider<String> tagProvider = Mock()
        gitValueSourceProviderFactory.getProvider(["tag"] as String[]) >> tagProvider
        tagProvider.get() >> "v1.0.4"
        Provider<String> describeTagsProvider = Mock()
        gitValueSourceProviderFactory.getProvider(["describe", "--tags"] as String[]) >> describeTagsProvider
        describeTagsProvider.get() >> "v1.0.5"


        when:
        def v = autoVersion.deduceVersion(log, Project.DEFAULT_VERSION)

        then:
        v.version == "1.0.5"
        v.previousVersion == "1.0.4"
    }

    def "no build failure when deducing versions fails"() {
        versionFile << "version=1.0.*"
        Provider<String> tagProvider = Mock()
        gitValueSourceProviderFactory.getProvider(["tag"] as String[]) >> tagProvider
        tagProvider.get() >> "v1.0.1"

        when:
        def v = autoVersion.deduceVersion(log, Project.DEFAULT_VERSION)

        then:
        v.version == "1.0.unspecified"
        v.previousVersion == "1.0.1"
        1 * log.debug("shipkit-auto-version caught an exception, falling back to reasonable default", _ as Exception)
        1 * log.lifecycle("Building version '1.0.unspecified'\n" +
                "  - reason: shipkit-auto-version caught an exception, falling back to reasonable default\n" +
                "  - run with --debug for more info")
    }

    def "no build failure when no version config present and deducing versions fails"() {
        Provider<String> tagProvider = Mock()
        gitValueSourceProviderFactory.getProvider(["tag"] as String[]) >> tagProvider
        tagProvider.get() >> {
            throw new Exception()
        }

        when:
        def v = autoVersion.deduceVersion(log, Project.DEFAULT_VERSION)

        then:
        v.version == "0.0.1-SNAPSHOT"
        v.previousVersion == null
        1 * log.debug("shipkit-auto-version caught an exception, falling back to reasonable default", _ as Exception)
        1 * log.lifecycle("Building version '0.0.1-SNAPSHOT'\n" +
                "  - reason: shipkit-auto-version caught an exception, falling back to reasonable default\n" +
                "  - run with --debug for more info")
    }
}
