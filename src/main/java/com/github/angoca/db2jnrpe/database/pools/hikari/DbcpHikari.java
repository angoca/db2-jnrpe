package com.github.angoca.db2jnrpe.database.pools.hikari;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.github.angoca.db2jnrpe.database.DatabaseConnection;
import com.github.angoca.db2jnrpe.database.DatabaseConnectionException;
import com.github.angoca.db2jnrpe.database.pools.ConnectionPool;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Implementation of the connection pool with Hikari.
 *
 * @author Andres Gomez Casanova (@AngocA)
 * @version 2014-11-03
 */
public class DbcpHikari extends ConnectionPool {

    /**
     * Map of URL and its associated pool.
     */
    private static Map<String, HikariDataSource> pools = null;

    /**
     * Tester.
     *
     * @param args
     *            Arguments.
     * @throws Exception
     *             If any error occurs.
     */
    public static final void main(final String[] args) throws Exception {
        System.out.println("Test: DatabaseConnection Hikari");
        final DatabaseConnection dc1 = new DatabaseConnection(
                DbcpHikari.class.getName(), new Properties(), "db2inst1",
                "db2inst1") {

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
                return "com.ibm.db2.jcc.DB2SimpleDataSource";
            }
        };
        final Connection conn1 = new DbcpHikari().initialize(dc1)
                .getConnection(dc1);
        System.out.println("Client Information: " + conn1.getClientInfo());
        final DatabaseConnection dc2 = new DatabaseConnection(
                DbcpHikari.class.getName(), new Properties(), "db2inst1",
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
        final Connection conn2 = new DbcpHikari().initialize(dc2)
                .getConnection(dc2);
        System.out.println("Client Information: " + conn2.getClientInfo());
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
    public final void closeConnection(final DatabaseConnection dbConn,
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
    public final Connection getConnection(final DatabaseConnection dbConn)
            throws DatabaseConnectionException {
        HikariDataSource ds = DbcpHikari.pools.get(dbConn.getUrl());
        if (ds == null) {
            final HikariConfig config = new HikariConfig();
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");
            config.addDataSourceProperty("blockingReadConnectionTimeout ",
                    "1000");
            config.setJdbcUrl(dbConn.getUrl());
            config.setUsername(dbConn.getUsername());
            config.setPassword(dbConn.getPassword());
            config.setMinimumIdle(ConnectionPool.MIN_POOL_SIZE);
            config.setMaximumPoolSize(ConnectionPool.MAX_POOL_SIZE);
            config.setDataSourceProperties(dbConn.getConnectionProperties());
            ds = new HikariDataSource(config);
            DbcpHikari.pools.put(dbConn.getUrl(), ds);
        }

        try {
            return ds.getConnection();
        } catch (final SQLException e) {
            throw new DatabaseConnectionException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.github.angoca.db2jnrpe.database.pools.ConnectionPool#initialize(com
     * .github.angoca.db2jnrpe.database.DatabaseConnection)
     */
    @Override
    public final ConnectionPool initialize(final DatabaseConnection dbConn)
            throws DatabaseConnectionException {
        if (DbcpHikari.pools == null) {
            DbcpHikari.pools = new HashMap<String, HikariDataSource>();
        }
        return this;
    }
}
