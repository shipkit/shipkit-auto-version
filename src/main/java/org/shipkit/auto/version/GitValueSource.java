package org.shipkit.auto.version;

import static java.lang.String.join;
import java.io.ByteArrayOutputStream;
import java.io.File;

import javax.inject.Inject;

import org.gradle.api.provider.ValueSource;
import org.gradle.process.ExecOperations;
import org.gradle.process.ExecResult;

public abstract class GitValueSource implements ValueSource<String, GitValueSourceParameters> {

    private final ExecOperations execOperations;

    @Inject
    public GitValueSource(ExecOperations execOperations) {
        this.execOperations = execOperations;
    }

    @Override
    public String obtain() {
        File workDir = getParameters().getWorkingDirectory().get();

        String[] commands = getParameters().getCommands().get();
        String[] commandsArray = new String[commands.length + 1];
        commandsArray[0] = "git";
        System.arraycopy(commands, 0, commandsArray, 1, commands.length);

        int exitValue;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ExecResult result;

        try {
            result = execOperations.exec(execSpec -> {
                execSpec.commandLine((Object[]) commandsArray);
                execSpec.workingDir(workDir);
                execSpec.setStandardOutput(outputStream);
                execSpec.setErrorOutput(outputStream);
            });
        } catch (Exception e) {
            String cmdLine = join(" ", commandsArray);
            throw new ShipkitAutoVersionException("Problems executing command:\n  " + cmdLine, e);
        }

        String output = outputStream.toString();
        exitValue = result.getExitValue();

        if (exitValue != 0) {
            String cmdLine = join(" ", commandsArray);
            throw new ShipkitAutoVersionException(
                    "Problems executing command (exit code: " + exitValue + "): " + cmdLine + "\n" +
                    "Output:\n" + output);
        }

        return output;
    }
}