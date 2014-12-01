package com.github.angoca.db2jnrpe.database.pools.db2direct;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import com.github.angoca.db2jnrpe.database.AbstractDatabaseConnection;
import com.github.angoca.db2jnrpe.database.DatabaseConnectionException;
import com.github.angoca.db2jnrpe.database.pools.AbstractConnectionPool;
import com.github.angoca.db2jnrpe.database.rdbms.db2.DB2Connection;

/**
 * Connection pool using direct DB2 driver.
 *
 * @author Andres Gomez Casanova (@AngocA)
 * @version 2014-11-04
 */
public class DbcpDb2Direct extends AbstractConnectionPool {

    /**
     * Connection properties.
     */
    private transient DB2Connection db2Conn;

    /**
     * Empty constructor.
     */
    protected DbcpDb2Direct() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.github.angoca.db2jnrpe.database.pools.AbstractConnectionPool#
     * closeConnection
     * (com.github.angoca.db2jnrpe.database.AbstractDatabaseConnection,
     * java.sql.Connection)
     */
    @Override
    @SuppressWarnings("PMD.CommentRequired")
    public final void closeConnection(final AbstractDatabaseConnection dbConn,
            final Connection connection) throws DatabaseConnectionException {
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
     * @see com.github.angoca.db2jnrpe.database.pools.AbstractConnectionPool#
     * getConnection
     * (com.github.angoca.db2jnrpe.database.AbstractDatabaseConnection)
     */
    @Override
    @SuppressWarnings("PMD.CommentRequired")
    public final Connection getConnection(
            final AbstractDatabaseConnection dbConn)
            throws DatabaseConnectionException {
        Connection connection = null;
        final Properties props = this.db2Conn.getConnectionProperties();
        props.put("user", this.db2Conn.getUsername());
        props.put("password", this.db2Conn.getPassword());
        try {
            connection = DriverManager.getConnection(this.db2Conn.getUrl(),
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
     * com.github.angoca.db2jnrpe.database.pools.AbstractConnectionPool#initialize
     * ( com.github.angoca.db2jnrpe.database.AbstractDatabaseConnection)
     */
    @Override
    @SuppressWarnings("PMD.CommentRequired")
    public final AbstractConnectionPool initialize(
            final AbstractDatabaseConnection dbConn)
            throws DatabaseConnectionException {
        try {
            Class.forName(dbConn.getDriverClass());
            if (dbConn instanceof DB2Connection) {
                this.db2Conn = (DB2Connection) dbConn;
            } else {
                throw new DatabaseConnectionException(
                        new Exception(
                                "Invalid connection properties (AbstractDatabaseConnection)"));
            }
        } catch (final ClassNotFoundException e) {
            throw new DatabaseConnectionException(e);
        }
        return this;
    }
}
