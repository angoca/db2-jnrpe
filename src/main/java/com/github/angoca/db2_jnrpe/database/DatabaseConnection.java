package com.github.angoca.db2_jnrpe.database;

import java.util.Properties;

public abstract class DatabaseConnection {

    static String getId(final String hostname, final int portNumber,
            final String databaseName) {
        String ret = hostname + ':' + portNumber + '/' + databaseName;
        return ret;
    }

    final private String url;
    final private Properties connectionProperties;
    final private String id;
    final private String username;
    final private String password;

    protected DatabaseConnection(final Properties defaultProperties,
            final String hostname, final int portNumber,
            final String databaseName, final String username,
            final String password) {
        this.url = "jdbc:db2://" + hostname + ":" + portNumber + "/"
                + databaseName;
        this.connectionProperties = defaultProperties;
        this.username = username;
        this.password = password;
        this.id = getId(hostname, portNumber, databaseName);
    }

    public String getURL() {
        return this.url;
    }

    public Properties getConnectionProperties() {
        return this.connectionProperties;
    }

    String getId() {
        return this.id;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public abstract String getDriverClass();
}

