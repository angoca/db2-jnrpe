package com.github.angoca.db2_jnrpe;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class DatabaseConnectionsPool {
    private static DatabaseConnectionsPool instance;

    public static DatabaseConnectionsPool getInstance() {
        if (instance == null) {
            instance = new DatabaseConnectionsPool();
        }
        return instance;
    }

    final private Map<String, DatabaseConnection> connectionProps;

    public DatabaseConnectionsPool() {
        this.connectionProps = new HashMap<>();
        this.defaultProperties = new Properties();
        // Changes the application name.
        this.defaultProperties.put("clientProgramName", "db2-jnrpe");
        // Shows descriptive message when errors.
        this.defaultProperties.put("retrieveMessagesFromServerOnGetMessage",
                "true");
    }

    final protected Properties defaultProperties;

    DatabaseConnection getDatabaseConnection(final String hostname,
            final int portNumber, final String databaseName,
            final String username, final String password) {
        String connKey = DatabaseConnection.getId(hostname, portNumber,
                databaseName);
        DatabaseConnection dbConn = this.connectionProps.get(connKey);
        if (dbConn == null) {
            dbConn = new DatabaseConnection(defaultProperties, hostname,
                    portNumber, databaseName, username, password);
            this.connectionProps.put(connKey, dbConn);
        }
        return dbConn;
    }

}

