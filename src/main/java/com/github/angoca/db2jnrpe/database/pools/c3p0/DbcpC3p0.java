package com.github.angoca.db2jnrpe.database.pools.c3p0;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.github.angoca.db2jnrpe.database.DatabaseConnection;
import com.github.angoca.db2jnrpe.database.DatabaseConnectionException;
import com.github.angoca.db2jnrpe.database.pools.ConnectionPool;
import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * Configuration of the c3p0 connection pool manager.
 *
 * @author Andres Gomez Casanova (@AngocA)
 * @version 2014-11-03
 */
public final class DbcpC3p0 extends ConnectionPool {

    /**
     * Map of URL and its associated pool.
     */
    private static Map<String, ComboPooledDataSource> pools = null;

    /**
     * Tester.
     *
     * @param args
     *            Arguments.
     * @throws Exception
     *             If any error occurs.
     */
    public static void main(final String[] args) throws Exception {
        System.out.println("Test: DatabaseConnection c3p0");
        final DatabaseConnection dc1 = new DatabaseConnection(
                DbcpC3p0.class.getName(), new Properties(), "db2inst1",
                "db2inst1") {

            {
                this.setUrl("jdbc:db2://localhost:50000/sample");
            }

            /*
             * (non-Javadoc)
             * 
             * @see com.github.angoca.db2jnrpe.database.DatabaseConnection
             * #getDriverClass()
             */
            @Override
            public String getDriverClass() {
                return "com.ibm.db2.jcc.DB2Driver";
            }
        };
        Connection conn = new DbcpC3p0().initialize(dc1).getConnection(dc1);
        System.out.println("Client Information: " + conn.getClientInfo());
        final DatabaseConnection dc2 = new DatabaseConnection(
                DbcpC3p0.class.getName(), new Properties(), "db2inst2",
                "db2inst2") {

            {
                this.setUrl("jdbc:db2://127.0.0.1:50000/sample2");
            }

            /*
             * (non-Javadoc)
             * 
             * @see com.github.angoca.db2jnrpe.database.DatabaseConnection
             * #getDriverClass()
             */
            @Override
            public String getDriverClass() {
                return "com.ibm.db2.jcc.DB2Driver";
            }
        };
        conn = new DbcpC3p0().initialize(dc2).getConnection(dc2);
        System.out.println("Client Information: " + conn.getClientInfo());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.github.angoca.db2jnrpe.database.pools.ConnectionPool#closeConnection
     * (com.github.angoca.db2jnrpe.database.DatabaseConnection,
     * java.sql.Connection)
     */
    @Override
    public void closeConnection(final DatabaseConnection dbConn,
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
     * @see
     * com.github.angoca.db2jnrpe.database.pools.ConnectionPool#getConnection
     * (com.github.angoca.db2jnrpe.database.DatabaseConnection)
     */
    @Override
    public Connection getConnection(final DatabaseConnection dbConn)
            throws DatabaseConnectionException {
        if (DbcpC3p0.pools == null) {
            throw new DatabaseConnectionException(new Exception(
                    "Pool not initialized"));
        }
        final String username = dbConn.getUsername();
        final String password = dbConn.getPassword();
        Connection connection;
        try {
            ComboPooledDataSource pool = DbcpC3p0.pools.get(dbConn.getUrl());
            if (pool == null) {
                pool = new ComboPooledDataSource();
                try {
                    pool.setDriverClass(dbConn.getDriverClass());
                } catch (final PropertyVetoException e) {
                    pool.close();
                    throw new DatabaseConnectionException(e);
                }
                pool.setJdbcUrl(dbConn.getUrl());
                pool.setMinPoolSize(3);
                pool.setAcquireIncrement(5);
                pool.setMaxPoolSize(20);
                pool.setProperties(dbConn.getConnectionProperties());
                try {
                    pool.setDriverClass(dbConn.getDriverClass());
                } catch (final PropertyVetoException e) {
                    pool.close();
                    throw new DatabaseConnectionException(e);
                }
                DbcpC3p0.pools.put(dbConn.getUrl(), pool);
            }
            connection = pool.getConnection(username, password);
        } catch (final SQLException e) {
            throw new DatabaseConnectionException(e);
        }
        return connection;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.github.angoca.db2jnrpe.database.pools.ConnectionPool#initialize(
     * com.github.angoca.db2jnrpe.database.DatabaseConnection)
     */
    @Override
    public ConnectionPool initialize(final DatabaseConnection dbConn)
            throws DatabaseConnectionException {
        if (DbcpC3p0.pools == null) {
            DbcpC3p0.pools = new HashMap<String, ComboPooledDataSource>();
        }
        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final String ret = "[c3p0-" + DbcpC3p0.pools.size() + ']';
        return ret;
    }
}
