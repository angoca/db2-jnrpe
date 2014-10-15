package com.github.angoca.db2_jnrpe.database.pools.c3p0;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import com.github.angoca.db2_jnrpe.database.DatabaseConnection;
import com.github.angoca.db2_jnrpe.database.pools.DBBroker;
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
    }

    public Connection getConnection(final DatabaseConnection dbConn)
            throws SQLException {
        try {
            cpds.setDriverClass(dbConn.getDriverClass());
        } catch (PropertyVetoException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        cpds.setJdbcUrl(dbConn.getURL());
        cpds.setProperties(dbConn.getConnectionProperties());

        String username = dbConn.getUsername();
        String password = dbConn.getPassword();
        Connection connection = cpds.getConnection(username, password);
        return connection;
    }

    public void closeConnection(final DatabaseConnection dbConn)
            throws SQLException {
        Connection connection = this.getConnection(dbConn);
        if (connection != null) {
            connection.close();
        }
    }

    public static void main(String[] args) throws SQLException {
        Connection conn = DBBroker_c3p0.getInstance().getConnection(
                new DatabaseConnection(new Properties(), "localhost", 50000,
                        "sample", "db2admin", "AngocA81") {

                    @Override
                    public String getDriverClass() {
                        return "com.ibm.db2.jcc.DB2Driver";
                    }
                });
        System.out.println("Client Information: " + conn.getClientInfo());
    }
}

