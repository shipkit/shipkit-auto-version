package org.shipkit.auto.version;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import static java.lang.String.join;
import static java.util.Arrays.asList;

/**
 * Runs command in provided working dir.
 */
class ProcessRunner {

    private final File workDir;

    ProcessRunner(File workDir) {
        this.workDir = workDir;
    }

    String run(String... commandLine) {
        int exitValue;
        String output;
        try {
            Process process = new ProcessBuilder(commandLine).directory(workDir).redirectErrorStream(true).start();
            output = readFully(new BufferedReader(new InputStreamReader(process.getInputStream())));
            exitValue = process.waitFor();
        } catch (Exception e) {
            String cmdLine = join(" ", asList(commandLine));
            throw new ShipkitAutoVersionException("Problems executing command:\n  " + cmdLine, e);
        }

        if (exitValue != 0) {
            String cmdLine = join(" ", asList(commandLine));
            throw new ShipkitAutoVersionException(
                    "Problems executing command (exit code: " + exitValue + "): " + cmdLine + "\n" +
                    "Output:\n" + output);
        }

        return output;
    }

    private static String readFully(BufferedReader reader) throws IOException {
        try {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        } finally {
            reader.close();
        }
    }
}
