package com.github.angoca.db2jnrpe.database.pools.hikari;

import java.sql.Connection;
import java.sql.SQLException;
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
                return "com.ibm.db2.jcc.DB2SimpleDataSource";
            }
        };
        final Connection conn = new DbcpHikari().initialize(dc1).getConnection(
                dc1);
        System.out.println("Client Information: " + conn.getClientInfo());
    }

    /**
     * Configuration for Hikari.
     */
    private final HikariConfig config;

    /**
     * Constructor for Hikari implementation.
     */
    public DbcpHikari() {
        this.config = new HikariConfig();
        this.config.addDataSourceProperty("cachePrepStmts", "true");
        this.config.addDataSourceProperty("prepStmtCacheSize", "250");
        this.config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        this.config.addDataSourceProperty("useServerPrepStmts", "true");
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
        try {
            return new HikariDataSource(this.config).getConnection();
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
        this.config.setJdbcUrl(dbConn.getUrl());
        this.config.setUsername(dbConn.getUsername());
        this.config.setPassword(dbConn.getPassword());
        this.config.setDataSourceProperties(dbConn.getConnectionProperties());
        return this;
    }
}
