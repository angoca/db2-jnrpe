package com.github.angoca.db2jnrpe.database.pools.hikari;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import com.github.angoca.db2jnrpe.database.DatabaseConnection;
import com.github.angoca.db2jnrpe.database.DatabaseConnectionException;
import com.github.angoca.db2jnrpe.database.pools.ConnectionPool;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class Dbcp_Hikari extends ConnectionPool {

    /**
     * Tester.
     *
     * @param args
     *            Arguments.
     * @throws SQLException
     *             If any error occurs.
     */
    public static final void main(final String[] args) throws Exception {
        System.out.println("Test: DatabaseConnection Hikari");
        final DatabaseConnection dc1 = new DatabaseConnection(
                Dbcp_Hikari.class.getName(), new Properties(), "db2inst1",
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
        final Connection conn = new Dbcp_Hikari().initialize(dc1)
                .getConnection(dc1);
        System.out.println("Client Information: " + conn.getClientInfo());
    }

    private final HikariConfig config;

    public Dbcp_Hikari() {
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
        try {
            return new HikariDataSource(this.config).getConnection();
        } catch (final SQLException e) {
            throw new DatabaseConnectionException(e);
        }
    }

    @Override
    public ConnectionPool initialize(DatabaseConnection dbConn)
            throws DatabaseConnectionException {
        this.config.setJdbcUrl(dbConn.getUrl());
        this.config.setUsername(dbConn.getUsername());
        this.config.setPassword(dbConn.getPassword());
        this.config.setDataSourceProperties(dbConn.getConnectionProperties());
        return this;
    }
}
