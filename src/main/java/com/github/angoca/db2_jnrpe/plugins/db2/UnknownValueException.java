package com.github.angoca.db2_jnrpe.plugins.db2;

/**
 * Exception that represents when values in cache have not been read. Normally,
 * values will be ready the next time.
 * 
 * @author Andres Gomez Casanova (@AngocA)
 * @version 2014-11-10
 */
public class UnknownValueException extends Exception {

    /**
     * Generated ID.
     */
    private static final long serialVersionUID = 4991367778799736792L;

    /**
     * Creates the exception with an example.
     * 
     * @param reason
     *            Reason of the exception.
     */
    public UnknownValueException(final String reason) {
        super(reason);
    }

}
