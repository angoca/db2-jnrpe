package com.github.angoca.db2_jnrpe.database.pools.db2direct;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import com.github.angoca.db2_jnrpe.database.DatabaseConnection;
import com.github.angoca.db2_jnrpe.database.DatabaseConnectionException;
import com.github.angoca.db2_jnrpe.database.pools.ConnectionPool;
import com.github.angoca.db2_jnrpe.database.rdbms.db2.DB2Connection;

/**
 * Connection pool using direct DB2 driver.
 *
 * @author Andres Gomez Casanova (@AngocA)
 * @version 2014-11-04
 */
public class DBCP_db2Direct extends ConnectionPool {

    /**
     * Connection properties.
     */
    private DB2Connection db2Conn;

    /*
     * (non-Javadoc)
     *
     * @see
     * com.github.angoca.db2_jnrpe.database.pools.ConnectionPool#closeConnection
     * (java.sql.Connection)
     */
    @Override
    public void closeConnection(final Connection connection)
            throws DatabaseConnectionException {
        if (connection != null) {
            try {
                connection.close();
            } catch (final SQLException e) {
                throw new DatabaseConnectionException(e);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.github.angoca.db2_jnrpe.database.pools.ConnectionPool#getConnection()
     */
    @Override
    public Connection getConnection() throws DatabaseConnectionException {
        Connection connection = null;
        final Properties props = this.db2Conn.getConnectionProperties();
        props.put("user", this.db2Conn.getUsername());
        props.put("password", this.db2Conn.getPassword());
        try {
            connection = DriverManager.getConnection(this.db2Conn.getURL(),
                    props);
        } catch (final SQLException e) {
            throw new DatabaseConnectionException(e);
        }
        return connection;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.github.angoca.db2_jnrpe.database.pools.ConnectionPool#initialize(
     * com.github.angoca.db2_jnrpe.database.DatabaseConnection)
     */
    @Override
    public ConnectionPool initialize(DatabaseConnection dbConn)
            throws DatabaseConnectionException {
        try {
            Class.forName(dbConn.getDriverClass());
            if (dbConn instanceof DB2Connection) {
                this.db2Conn = (DB2Connection) dbConn;
            } else {
                throw new DatabaseConnectionException(new Exception(
                        "Invalid connection properties (DatabaseConnection)"));
            }
        } catch (final ClassNotFoundException e) {
            throw new DatabaseConnectionException(e);
        }
        return this;
    }
}
