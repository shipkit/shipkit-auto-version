package org.shipkit.auto.version

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class TmpFolderSpecification extends Specification {

    @Rule TemporaryFolder tmp = new TemporaryFolder()

    protected File writeFile(String content) {
        def f = tmp.newFile()
        f << content
        f
    }
}
