package com.github.angoca.db2_jnrpe;

import java.util.Properties;

public final class DatabaseConnection {

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

    DatabaseConnection(final Properties defaultProperties,
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

    String getURL() {
        return this.url;
    }

    Properties getConnectionProperties() {
        return this.connectionProperties;
    }

    String getId() {
        return this.id;
    }

    String getUsername() {
        return this.username;
    }

    String getPassword() {
        return this.password;
    }
}
