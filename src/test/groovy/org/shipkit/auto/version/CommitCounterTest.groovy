package org.shipkit.auto.version

import spock.lang.Specification

class CommitCounterTest extends Specification {

    def "counts merge commits"() {
        expect:
        4 == new CommitCounter().commitDelta("""
2994de4df (HEAD -> release/3.x, origin/release/3.x) Merge pull request #1863 from bric3/gradle-wrapper-validation-action
67bd4e96c Adds the Official Gradle Wrapper Validation GitHub Action
64e7eb517 Merge pull request #1859 from mockito/ongoing-stubbing-not-extensible
dd8b07887 Add NotExtensible to OngoingStubbing
692b12677 Fixes #1853: Allow @MockitoSettings to be inherited (#1854)
084e8af18 Merge pull request #1849 from dreis2211/gh-1848
ce1632ddd Merge pull request #1847 from andreisilviudragnea/fix-unused-stubbing-with-implicit-eq-matchers
b0d15d114 Improved the test coverage
""")
    }

    def "counts commits even if there are no merge commits"() {
        expect:
        2 == new CommitCounter().commitDelta("""
67bd4e96c Adds the Official Gradle Wrapper Validation GitHub Action
dd8b07887 Add NotExtensible to OngoingStubbing
""")
    }

    def "returns 1 if no commits"() {
        expect:
        1 == new CommitCounter().commitDelta("")
    }
}
