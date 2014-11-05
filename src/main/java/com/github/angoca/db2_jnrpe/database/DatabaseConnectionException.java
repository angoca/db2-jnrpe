package com.github.angoca.db2_jnrpe.database;

/**
 * Wraps any exception generated while using the database.
 * 
 * @author Andres Gomez Casanova (@AngocA)
 * @version 2014-11-03
 */
public final class DatabaseConnectionException extends Exception {

    /**
     * Generated Id.
     */
    private final static long serialVersionUID = 2299386533338530974L;

    /**
     * Creates an exception by wrapping the cause exception.
     * 
     * @param exception
     *            Wrapped exception.
     */
    public DatabaseConnectionException(final Exception exception) {
        super(exception);
    }
}
