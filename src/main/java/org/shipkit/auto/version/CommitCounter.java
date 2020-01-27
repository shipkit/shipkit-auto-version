package org.shipkit.auto.version;

import java.util.Scanner;

import static java.lang.Integer.max;

class CommitCounter {

    /**
     * Counts merge commits based on git output.
     * If no merge commits, counts every commit.
     * If no commits returns 1 (it's easier for consumers this way).
     *
     * @param gitOutput - output from "git log --pretty=oneline" command
     */
    int commitDelta(String gitOutput) {
        gitOutput = gitOutput.trim();
        int result = (int) new Scanner(gitOutput).findAll(" Merge pull request #\\d+ from ").count();
        if (result != 0) {
            return result;
        }

        result = gitOutput.split(System.lineSeparator()).length;
        return max(result, 1);
    }
}
