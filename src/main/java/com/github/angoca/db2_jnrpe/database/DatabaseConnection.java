package com.github.angoca.db2_jnrpe.database;

import java.util.Properties;

/**
 * Object that contains all properties to establish a connection.
 *
 * @author Andres Gomez Casanova (@AngocA)
 * @version 2014-11-03
 */
public abstract class DatabaseConnection {
    final protected Properties connectionProperties;

    /**
     * Associated connection pool.
     */
    private final String connectionsPool;
    /**
     * Password.
     */
    private final String password;
    /**
     * URL to connect to the database.
     */
    protected String url;
    /**
     * Username to access the database.
     */
    private final String username;

    /**
     * Creates an object with all properties to establish a connection to the
     * database.
     *
     * @param connectionsPool
     *            Associated connection pool.
     * @param defaultProperties
     *            Properties to establish the connection.
     * @param username
     *            Username to connect to the database.
     * @param password
     *            Password of the username.
     */
    protected DatabaseConnection(final String connectionsPool,
            final Properties defaultProperties, final String username,
            final String password) {
        this.connectionsPool = connectionsPool;

        this.connectionProperties = defaultProperties;
        this.username = username;
        this.password = password;
    }

    /**
     * Returns the connection properties.
     *
     * @return Connection properties.
     */
    public final Properties getConnectionProperties() {
        return this.connectionProperties;
    }

    /**
     * Returns the associated connection pool.
     *
     * @return Associated connection pool.
     */
    public final String getConnectionsPoolName() {
        return this.connectionsPool;
    }

    /**
     * Returns the class to load the driver.
     *
     * @return Name of the class of the driver.
     */
    public abstract String getDriverClass();

    /**
     * Returns the passwords.
     *
     * @return Password of the user.
     */
    public final String getPassword() {
        return this.password;
    }

    /**
     * Returns the URL to connect to the database.
     *
     * @return URL to access the database.
     */
    public final String getURL() {
        return this.url;
    }

    /**
     * Returns the username to access the database.
     *
     * @return Username.
     */
    public final String getUsername() {
        return this.username;
    }

    /**
     * Establishes the complete URL to connect to the database.
     *
     * @param url
     *            Complete URL to access the database.
     */
    public final void setURL(final String url) {
        this.url = url;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final String ret = '{' + this.username + '@' + this.url
                + this.connectionProperties.toString() + '}';
        return ret;
    }
}
