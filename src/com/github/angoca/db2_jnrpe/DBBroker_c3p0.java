package com.github.angoca.db2_jnrpe;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class DBBroker_c3p0 implements DBBroker {
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
        try {
            cpds.setDriverClass("com.ibm.db2.jcc.DB2Driver");
        } catch (PropertyVetoException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        cpds.setJdbcUrl("jdbc:db2://localhost:50000/sample");
        cpds.setUser("db2admin");
        cpds.setPassword("AngocA81");
    }

    public Connection getConnection() throws SQLException {
        Connection connection = cpds.getConnection();
        return connection;
    }

    public void closeConnection() throws SQLException {
        cpds.close();
    }
}
