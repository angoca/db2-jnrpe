package com.github.angoca.db2_jnrpe;

import java.sql.SQLException;

public class Example {

    public static void main(String[] args) throws SQLException {
        String hostname = "localhost";
        int portNumber = 50000;
        String databaseName = "sample";
        String username = "db2admin";
        String password = "AngocA81";

        DatabaseConnection dbConn = DatabaseConnectionsPool.getInstance()
                .getDatabaseConnection(hostname, portNumber, databaseName,
                        username, password);
        CheckBufferPoolHitRatio.check(dbConn);
    }
}

