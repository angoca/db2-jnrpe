package com.github.angoca.db2jnrpe.database.pools.hikari;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.github.angoca.db2jnrpe.database.AbstractDatabaseConnection;
import com.github.angoca.db2jnrpe.database.DatabaseConnectionException;
import com.github.angoca.db2jnrpe.database.pools.AbstractConnectionPool;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Implementation of the connection pool with Hikari.
 *
 * @author Andres Gomez Casanova (@AngocA)
 * @version 2014-11-03
 */
public final class DbcpHikari extends AbstractConnectionPool {

    /**
     * Map of URL and its associated pool.
     */
    private static Map<String, HikariDataSource> pools;

    /**
     * Tester.
     *
     * @param args
     *            Arguments.
     * @throws Exception
     *             If any error occurs.
     */
    @SuppressWarnings({ "PMD", "resource" })
    public static void main(final String[] args) throws Exception {
        System.out.println("Test: AbstractDatabaseConnection Hikari");
        final AbstractDatabaseConnection dc1 = new AbstractDatabaseConnection(
                DbcpHikari.class.getName(), new Properties(), "db2inst1",
                "db2inst1") {

            {
                this.setUrl("jdbc:db2://127.0.0.1:50000/sample2");
            }

            /*
             * (non-Javadoc)
             * 
             * @see
             * com.github.angoca.db2jnrpe.database.AbstractDatabaseConnection
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
        final AbstractDatabaseConnection dc2 = new AbstractDatabaseConnection(
                DbcpHikari.class.getName(), new Properties(), "db2inst1",
                "db2inst1") {

            {
                this.setUrl("jdbc:db2://localhost:50000/sample");
            }

            /*
             * (non-Javadoc)
             * 
             * @see
             * com.github.angoca.db2jnrpe.database.AbstractDatabaseConnection
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

    /**
     * Empty constructor.
     */
    private DbcpHikari() {
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
    public void closeConnection(final AbstractDatabaseConnection dbConn,
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
    public Connection getConnection(final AbstractDatabaseConnection dbConn)
            throws DatabaseConnectionException {
        HikariDataSource datasource = DbcpHikari.pools.get(dbConn.getUrl());
        if (datasource == null) {
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
            config.setMinimumIdle(AbstractConnectionPool.MIN_POOL_SIZE);
            config.setMaximumPoolSize(AbstractConnectionPool.MAX_POOL_SIZE);
            config.setDataSourceProperties(dbConn.getConnectionProperties());
            datasource = new HikariDataSource(config);
            DbcpHikari.pools.put(dbConn.getUrl(), datasource);
        }

        try {
            return datasource.getConnection();
        } catch (final SQLException e) {
            throw new DatabaseConnectionException(e);
        } finally {
            datasource.close();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.github.angoca.db2jnrpe.database.pools.AbstractConnectionPool#initialize
     * (com .github.angoca.db2jnrpe.database.DatabaseConnection)
     */
    @Override
    @SuppressWarnings("PMD.CommentRequired")
    public AbstractConnectionPool initialize(
            final AbstractDatabaseConnection dbConn)
            throws DatabaseConnectionException {
        if (DbcpHikari.pools == null) {
            DbcpHikari.pools = new HashMap<String, HikariDataSource>();
        }
        return this;
    }
}
