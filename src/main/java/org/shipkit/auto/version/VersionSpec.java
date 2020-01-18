package org.shipkit.auto.version;

import com.github.zafarkhaja.semver.Version;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

/**
 * Loads the version spec from 'version.properties' file, implements error handling.
 */
class VersionSpec {

    private static String ERROR = "Problems deducting the version automatically.";

    static String readVersionSpec(File versionFile) {
        Properties p = new Properties();
        try {
            p.load(new FileReader(versionFile));
        } catch (IOException e) {
            throw new RuntimeException(ERROR + " Missing file: " + versionFile, e);
        }

        Object v = p.get("version");
        if (!(v instanceof String)) {
            throw new MissingVersionKey(versionFile);
        }
        String versionSpec = (String) v;

        if (versionSpec.matches("\\d+.\\d+.(\\*)")) {
            return versionSpec;
        }

        try {
            Version.valueOf(versionSpec);
        } catch (Exception e) {
            throw new IncorrectVersionFormat(versionFile, e);
        }

        return versionSpec;
    }

    private static String exceptionMessage(File versionFile) {
        return "Problems deducting the version automatically. Expected correct 'version' property in file: " + versionFile + "\n" +
                "Correct examples: 'version=1.0.*', 'version=2.10.100'";
    }

    static class MissingVersionKey extends RuntimeException {
        MissingVersionKey(File versionFile) {
            super(exceptionMessage(versionFile));
        }
    }

    static class IncorrectVersionFormat extends RuntimeException {
        IncorrectVersionFormat(File versionFile, Exception e) {
            super(exceptionMessage(versionFile), e);
        }
    }
}
