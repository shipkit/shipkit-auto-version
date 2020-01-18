package org.shipkit.auto.version;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Models the content of version file.
 */
class VersionSpec {

    private final String versionSpec;
    private final boolean wildcard;

    VersionSpec(File versionFile) {
        Properties p = new Properties();
        try {
            p.load(new FileReader(versionFile));
        } catch (IOException e) {
            throw new RuntimeException("'org.shipkit.auto.version' plugin requires this file: " + versionFile, e);
        }

        Object v = p.get("version");
        if (!(v instanceof String)) {
            throw new IncorrectVersionFile(versionFile);
        }
        this.versionSpec = (String) v;
        Matcher matcher = validVersion(versionSpec);
        boolean matches = matcher.matches();
        if (!matches) {
            throw new IncorrectVersionFile(versionFile);
        }
        String patchVersion = matcher.group(1);
        wildcard = patchVersion.equals("*");
    }

    static Matcher validVersion(String versionSpec) {
        Pattern pattern = Pattern.compile("\\d+\\.\\d+\\.((\\*)|((\\d)+[-\\.\\w]*))");
        return pattern.matcher(versionSpec);
    }

    static class IncorrectVersionFile extends RuntimeException {
        IncorrectVersionFile(File versionFile) {
            super("'org.shipkit.auto.version' expects correct 'version' property in file: " + versionFile + "\n" +
                    "Correct examples: 'version=1.0.*', 'version=2.10.100'");
        }
    }

    /**
     * whether there is wildcard version in the spec, e.g. '1.0.*'
     */
    boolean isWildcard() {
        return wildcard;
    }

    /**
     * the exact spec, as in version file.
     */
    String getSpec() {
        return versionSpec;
    }
}
