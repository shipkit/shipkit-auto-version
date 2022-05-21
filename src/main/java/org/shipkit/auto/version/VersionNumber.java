package org.shipkit.auto.version;

import com.github.zafarkhaja.semver.Version;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class VersionNumber implements Comparable<VersionNumber> {

    private int fourthDigit = -1;
    private Version semver;

    public VersionNumber(String version) {
        try {
            semver = Version.valueOf(version);
            return;
        } catch (Exception e) {
            //fall back to 4-part number
        }

        Matcher matcher = Pattern.compile("^(\\d+\\.\\d+\\.\\d+)\\.(\\d+)$").matcher(version);
        if (!matcher.matches()) {
            throw new UnsupportedVersionException(version);
        }
        String g1 = matcher.group(1);
        semver = Version.valueOf(g1);

        String g2 = matcher.group(2);
        try {
            this.fourthDigit = Integer.parseInt(g2);
        } catch (NumberFormatException e) {
            throw new UnsupportedVersionException(version);
        }
    }

    @Override
    public int compareTo(VersionNumber v) {
        int result = this.semver.compareTo(v.semver);
        if (result != 0) {
            return result;
        }

        return Integer.compare(fourthDigit, v.fourthDigit);
    }

    /**
     * Examples: 1.2.3 satisfies 1.2.*, 1.2.3.4 satisfies 1.2.3.*
     *
     * @param versionSpec - for example, 1.2.* or 1.2.3.*
     */
    public boolean satisfies(String versionSpec) {
        if (fourthDigit == -1) {
            return semver.satisfies(versionSpec);
        }
        String version = versionSpec.replaceAll("\\.\\*$", "");
        return semver.satisfies(version);
    }

    public VersionNumber incrementBy(int count) {
        if (fourthDigit == -1) {
            return new VersionNumber(
                            semver.getMajorVersion() + "." +
                            semver.getMinorVersion() + "." +
                                    (semver.getPatchVersion() + count));
        }
        return new VersionNumber(semver.toString() + "." + (fourthDigit + count));
    }

    public String toString() {
        if (fourthDigit == -1) {
            return semver.toString();
        }

        return semver.toString() + "." + fourthDigit;
    }

    public static class UnsupportedVersionException extends RuntimeException {
        public UnsupportedVersionException(String version) {
            super("Unsupported version: " + version + ". Expected semver OR 4-part number like 1.2.3.4");
        }
    }
}
