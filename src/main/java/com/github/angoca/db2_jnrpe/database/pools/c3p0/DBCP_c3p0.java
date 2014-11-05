package com.github.angoca.db2_jnrpe.database.pools.c3p0;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import com.github.angoca.db2_jnrpe.database.DatabaseConnection;
import com.github.angoca.db2_jnrpe.database.DatabaseConnectionException;
import com.github.angoca.db2_jnrpe.database.pools.ConnectionPool;
import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * Configuration of the c3p0 connection pool manager.
 * 
 * @author Andres Gomez Casanova (@AngocA)
 * @version 2014-11-03
 */
public final class DBCP_c3p0 extends ConnectionPool {
    /**
     * Tester.
     * 
     * @param args
     *            Arguments.
     * @throws SQLException
     *             If any error occurs.
     */
    public final static void main(final String[] args) throws Exception {
        System.out.println("Test: DatabaseConnection c3p0");
        final Connection conn = new DBCP_c3p0().initialize(
                new DatabaseConnection(DBCP_c3p0.class.getName(),
                        new Properties(), "db2inst1", "db2inst1") {

                    {
                        this.setURL("jdbc:db2://localhost:50000/sample");
                    }

                    /*
                     * (non-Javadoc)
                     * 
                     * @see
                     * com.github.angoca.db2_jnrpe.database.DatabaseConnection
                     * #getDriverClass()
                     */
                    @Override
                    public String getDriverClass() {
                        return "com.ibm.db2.jcc.DB2Driver";
                    }
                }).getConnection();
        System.out.println("Client Information: " + conn.getClientInfo());
    }

    /**
     * Connection pool.
     */
    private static ComboPooledDataSource cpds;
    /**
     * Connection properties.
     */
    private DatabaseConnection dbConn;

    /**
     * Instantiate the singleton by initializing the connection pool.
     */
    public DBCP_c3p0() {
        cpds = new ComboPooledDataSource();
        cpds.setMinPoolSize(3);
        cpds.setAcquireIncrement(5);
        cpds.setMaxPoolSize(20);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.github.angoca.db2_jnrpe.database.pools.ConnectionPool#closeConnection
     * (java.sql.Connection)
     */
    @Override
    public final void closeConnection(final Connection connection)
            throws DatabaseConnectionException {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
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
    public final Connection getConnection() throws DatabaseConnectionException {
        try {
            cpds.setDriverClass(dbConn.getDriverClass());
        } catch (PropertyVetoException e) {
            throw new DatabaseConnectionException(e);
        }
        cpds.setJdbcUrl(dbConn.getURL());
        cpds.setProperties(dbConn.getConnectionProperties());

        final String username = dbConn.getUsername();
        final String password = dbConn.getPassword();
        Connection connection;
        try {
            connection = cpds.getConnection(username, password);
        } catch (SQLException e) {
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
        this.dbConn = dbConn;
        return this;
    }
}
