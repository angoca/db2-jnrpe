package com.github.angoca.db2_jnrpe.database;

import java.util.Properties;

public abstract class DatabaseConnection {
    final protected Properties connectionProperties;

    final private String connectionsPool;
    final private String password;
    protected String url;
    final private String username;

    protected DatabaseConnection(final String connectionsPool,
            final Properties defaultProperties, final String username,
            final String password) {
        this.connectionsPool = connectionsPool;

        this.connectionProperties = defaultProperties;
        this.username = username;
        this.password = password;
    }

    public Properties getConnectionProperties() {
        return this.connectionProperties;
    }

    public String getConnectionsPool() {
        return this.connectionsPool;
    }

    public abstract String getDriverClass();

    public String getPassword() {
        return this.password;
    }

    public String getURL() {
        return this.url;
    }

    public String getUsername() {
        return this.username;
    }

    public void setURL(final String url) {
        this.url = url;
    }

}
