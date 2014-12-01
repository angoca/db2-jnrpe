package com.github.angoca.db2jnrpe.database;

import java.util.Properties;

/**
 * Object that contains all properties to establish a connection.
 *
 * @author Andres Gomez Casanova (@AngocA)
 * @version 2014-11-03
 */
@SuppressWarnings("PMD.CommentSize")
public abstract class AbstractDatabaseConnection {
    /**
     * Associated connection pool.
     */
    private final transient String connectionsPool;

    /**
     * Connection properties.
     */
    private final transient Properties connProperties;
    /**
     * Password.
     */
    private final String password;
    /**
     * URL to connect to the database.
     */
    private String url;
    /**
     * Username to access the database.
     */
    private final String username;

    /**
     * Creates an object with all properties to establish a connection to the
     * database.
     *
     * @param connsPool
     *            Associated connection pool.
     * @param defaultProperties
     *            Properties to establish the connection.
     * @param user
     *            Username to connect to the database.
     * @param passwd
     *            Password of the username.
     */
    protected AbstractDatabaseConnection(final String connsPool,
            final Properties defaultProperties, final String user,
            final String passwd) {
        this.connectionsPool = connsPool;

        this.connProperties = defaultProperties;
        this.username = user;
        this.password = passwd;
    }

    /**
     * Returns the connection properties.
     *
     * @return Connection properties.
     */
    public final Properties getConnectionProperties() {
        return this.connProperties;
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
    public final String getUrl() {
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
     * @param dbUrl
     *            Complete URL to access the database.
     */
    public final void setUrl(final String dbUrl) {
        this.url = dbUrl;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    @SuppressWarnings("PMD.CommentRequired")
    public final String toString() {
        final String ret = '{' + this.username + '@' + this.url
                + this.connProperties.toString() + '}';
        return ret;
    }
}
