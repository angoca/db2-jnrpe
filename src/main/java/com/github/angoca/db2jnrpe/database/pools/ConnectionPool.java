package com.github.angoca.db2jnrpe.database.pools;

import java.sql.Connection;

import com.github.angoca.db2jnrpe.database.DatabaseConnection;
import com.github.angoca.db2jnrpe.database.DatabaseConnectionException;

/**
 * Structure of a connection pool.
 *
 * @author Andres Gomez Casanova (@AngocA)
 * @version 2014-11-03
 */
public abstract class ConnectionPool {

    /**
     * Maximum size of the pool.
     */
    protected static final int MAX_POOL_SIZE = 5;
    /**
     * Minimum size of the pool.
     */
    protected static final int MIN_POOL_SIZE = 1;

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
    public abstract void closeConnection(final DatabaseConnection dbConn,
            final Connection connection) throws DatabaseConnectionException;

    /**
     * Retrieves a connection.
     *
     * @param dbConn
     *            Configuration parameters.
     * @return An established connection to the database.
     * @throws DatabaseConnectionException
     *             If any error occurs.
     */
    public abstract Connection getConnection(final DatabaseConnection dbConn)
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
    public abstract ConnectionPool initialize(final DatabaseConnection dbConn)
            throws DatabaseConnectionException;
}
