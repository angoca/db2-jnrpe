package com.github.angoca.db2_jnrpe.database.pools;

import java.sql.Connection;

import com.github.angoca.db2_jnrpe.database.DatabaseConnection;
import com.github.angoca.db2_jnrpe.database.DatabaseConnectionException;

/**
 * Structure of a connection pool.
 * 
 * @author Andres Gomez Casanova (@AngocA)
 * @version 2014-11-03
 */
public abstract class ConnectionPool {

    /**
     * Closes a established connection.
     * 
     * @param dbConn
     *            Connection to the database.
     * @throws DatabaseConnectionException
     *             If any error occurs.
     */
    public abstract void closeConnection(final DatabaseConnection dbConn)
            throws DatabaseConnectionException;

    /**
     * Retrieves a connection.
     * 
     * @param dbConn
     *            Connection properties.
     * @return An established connection to the database.
     * @throws DatabaseConnectionException
     *             If any error occurs.
     */
    public abstract Connection getConnection(final DatabaseConnection dbConn)
            throws DatabaseConnectionException;
}
