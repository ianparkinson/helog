package com.github.ianparkinson.helog;

import com.github.ianparkinson.helog.Source.ConnectionFailedException;
import picocli.CommandLine.Help.Ansi;

import java.io.IOException;
import java.io.Reader;

import static com.github.ianparkinson.helog.ErrorMessage.errorMessage;

/**
 * Writes text from a {@link Source} out to stdout.
 */
public final class RawPrinter {

    private final Ansi ansi;

    public RawPrinter(Ansi ansi) {
        this.ansi = ansi;
    }

    public void run(Source source) {
        Source.Connection connection;
        try {
            connection = source.connect();
        } catch (InterruptedException e) {
            // Nothing should interrupt us, so treat this as unexpected.
            throw new RuntimeException("Unexpected interrupt", e);
        } catch (ConnectionFailedException e) {
            e.errorMessage.writeToStderr(ansi);
            return;
        }

        char[] buffer = new char[1024];
        try (Reader reader = connection.getReader()) {
            while (true) {
                int count = reader.read(buffer);
                if (count >= 0) {
                    System.out.print(new String(buffer, 0, count));
                } else {
                    if (connection.getError() != null) {
                        connection.getError().writeToStderr(ansi);
                    } else {
                        errorMessage("Stream closed").writeToStderr(ansi);
                    }
                    return;
                }
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
