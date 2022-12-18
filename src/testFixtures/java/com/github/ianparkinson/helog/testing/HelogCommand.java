package com.github.ianparkinson.helog.testing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Integration test utility, which runs the helog command to completion, returning the output from the command in an
 * easy-to-consume form.
 */
public final class HelogCommand {
    private HelogCommand() {}

    /**
     * Runs the helog command with the given arguments.
     *
     * <p>Blocks until the command exits.
     */
    public static Result run(String... args) throws InterruptedException, IOException {
        ArrayList<String> allArgs = new ArrayList<>();
        allArgs.add(command());
        allArgs.addAll(Arrays.asList(args));
        Process process = Runtime.getRuntime().exec(allArgs.toArray(new String[] {}));
        process.waitFor(10, TimeUnit.SECONDS);

        return new Result(
                process.exitValue(),
                new String(process.getInputStream().readAllBytes()),
                new String(process.getErrorStream().readAllBytes()));
    }

    /**
     * Encapsulates the result of running a command-line command to completion.
     */
    public static class Result {
        /** The exit code with which the command terminated. */
        public final int exitCode;

        /** Text written to stdout. */
        public final String stdOut;

        /** Text written to stderr. */
        public final String stdErr;

        public Result(int exitCode, String stdOut, String stdErr) {
            this.exitCode = exitCode;
            this.stdOut = stdOut;
            this.stdErr = stdErr;
        }
    }

    private static String command() {
        if (System.getProperty("os.name").startsWith("Windows")) {
            return "build\\install\\helog\\bin\\helog.bat";
        } else {
            return "build/install/helog/bin/helog";
        }
    }
}
