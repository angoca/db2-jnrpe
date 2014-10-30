package com.github.angoca.db2_jnrpe.database.rdbms.db2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import com.github.angoca.db2_jnrpe.database.DatabaseConnection;

public class TestDB2Connection {
    public static void main(String[] args) throws Exception {
        System.out.println("Test: Connection");
        DatabaseConnection dbConn = new DB2Connection("", new Properties(),
                "localhost", 50000, "sample", "db2inst1", "db2inst1");
        Class.forName(dbConn.getDriverClass());
        Connection conn = DriverManager.getConnection(dbConn.getURL(),
                dbConn.getUsername(), dbConn.getPassword());
        System.out.println("Client Information: " + conn.getClientInfo());
    }

}
