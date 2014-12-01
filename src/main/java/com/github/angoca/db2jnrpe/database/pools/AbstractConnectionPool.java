package com.github.angoca.db2jnrpe.database.pools;

import java.sql.Connection;

import com.github.angoca.db2jnrpe.database.AbstractDatabaseConnection;
import com.github.angoca.db2jnrpe.database.DatabaseConnectionException;

/**
 * Structure of a connection pool.
 *
 * @author Andres Gomez Casanova (@AngocA)
 * @version 2014-11-03
 */
@SuppressWarnings("PMD.CommentSize")
public abstract class AbstractConnectionPool {

    /**
     * Maximum size of the pool.
     */
    protected static final int MAX_POOL_SIZE = 5;
    /**
     * Minimum size of the pool.
     */
    protected static final int MIN_POOL_SIZE = 1;

    /**
     * Empty constructor.
     */
    protected AbstractConnectionPool() {
        // Nothing.
    }

    /**
     * Closes a established connection.
     *
     * @param dbConn
     *            Configuration parameters.
     * @param connection
     *            Connection to the database.
     * @throws DatabaseConnectionException
     *             If any error occurs.
     */
    public abstract void closeConnection(
            final AbstractDatabaseConnection dbConn, final Connection connection)
            throws DatabaseConnectionException;

    /**
     * Retrieves a connection.
     *
     * @param dbConn
     *            Configuration parameters.
     * @return An established connection to the database.
     * @throws DatabaseConnectionException
     *             If any error occurs.
     */
    public abstract Connection getConnection(
            final AbstractDatabaseConnection dbConn)
            throws DatabaseConnectionException;

    /**
     * Initialize the Connection Pool.
     *
     * @param dbConn
     *            Configuration parameters.
     * @throws DatabaseConnectionException
     *             If any error occurs.
     * @return Return the associated connection pool.
     */
    public abstract AbstractConnectionPool initialize(
            final AbstractDatabaseConnection dbConn)
            throws DatabaseConnectionException;
}
