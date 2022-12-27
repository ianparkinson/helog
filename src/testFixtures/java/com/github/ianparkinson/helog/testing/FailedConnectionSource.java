package com.github.ianparkinson.helog.testing;

import com.github.ianparkinson.helog.ErrorMessage;
import com.github.ianparkinson.helog.Source;

/**
 * A {@link Source} that simulates a failure to connect.
 */
public class FailedConnectionSource implements Source {
    private final ErrorMessage message;

    public FailedConnectionSource(ErrorMessage message) {
        this.message = message;
    }

    @Override
    public Connection connect() throws ConnectionFailedException {
        throw new ConnectionFailedException(message);
    }
}
