package com.github.angoca.db2_jnrpe;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class DBBroker_c3p0 extends DBBroker {
    private ComboPooledDataSource cpds;

    private static DBBroker_c3p0 instance;

    public static DBBroker_c3p0 getInstance() {
        if (instance == null) {
            instance = new DBBroker_c3p0();
        }
        return instance;
    }

    public DBBroker_c3p0() {
        cpds = new ComboPooledDataSource();
        cpds.setMinPoolSize(3);
        cpds.setAcquireIncrement(5);
        cpds.setMaxPoolSize(20);
        try {
            cpds.setDriverClass(this.driverClass);
        } catch (PropertyVetoException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public Connection getConnection(final DatabaseConnection dbConn)
            throws SQLException {
        cpds.setJdbcUrl(dbConn.getURL());
        cpds.setProperties(dbConn.getConnectionProperties());

        Connection connection = cpds.getConnection(dbConn.getUsername(),
                dbConn.getPassword());
        System.out.println(cpds.getNumConnectionsAllUsers());
        return connection;
    }

    public void closeConnection(final DatabaseConnection dbConn)
            throws SQLException {
        Connection connection = this.getConnection(dbConn);
        if (connection != null) {
            connection.close();
        }
    }
}
