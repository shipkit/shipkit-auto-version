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
    int countCommitDelta(String gitOutput) {
        gitOutput = gitOutput.trim();
        //need to subtract 1 because we are matching substring (e.g. there is always text before and after the
        int result = countOccurrences(gitOutput, " Merge pull request #\\d+ from ") - 1;
        if (result != 0) {
            return result;
        }

        result = countOccurrences(gitOutput, System.lineSeparator());
        return max(result, 1);
    }

    private static int countOccurrences(String input, String pattern) {
        return input.split(pattern).length;
    }
}
