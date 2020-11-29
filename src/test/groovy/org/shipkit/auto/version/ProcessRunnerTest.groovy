package org.shipkit.auto.version


import spock.lang.IgnoreIf

//ignore the test when there is no 'ls' utility
@IgnoreIf({ !commandAvailable("ls") })
class ProcessRunnerTest extends TmpFolderSpecification {

    def "runs processes and returns output"() {
        File dir = tmp.newFolder()
        new File(dir, "xyz.txt").createNewFile()
        new File(dir, "hey joe.jar").createNewFile()

        when:
        String output = new ProcessRunner(dir).run("ls")

        then:
        output.contains("xyz.txt")
        output.contains("hey joe.jar")
    }

    def "process start failure"() {
        when:
        new ProcessRunner(tmp.root).run("kabooom")

        then:
        def e = thrown(ShipkitAutoVersionException)
        e.message == "[shipkit-auto-version] Problems executing command:\n  kabooom"
    }

    def "process non zero exit"() {
        when:
        new ProcessRunner(tmp.root).run("ls", "--kaboom")

        then:
        def e = thrown(ShipkitAutoVersionException)
        e.message.startsWith """[shipkit-auto-version] Problems executing command (exit code: 1): ls --kaboom
Output:
ls: illegal option"""
    }

    static boolean commandAvailable(String command) {
        try {
            return command.execute().waitFor() == 0
        } catch (ignored) {
            return false
        }
    }
}
