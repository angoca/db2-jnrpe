package com.github.angoca.db2jnrpe.database.rdbms.db2;

import java.util.Properties;

import com.github.angoca.db2jnrpe.database.AbstractDatabaseConnection;

/**
 * Description of the parameter for a DB2 connection.
 *
 * @author Andres Gomez Casanova (@AngocA)
 * @version 2014-11-03
 */
@SuppressWarnings("PMD.CommentSize")
public final class DB2Connection extends AbstractDatabaseConnection {
    /**
     * Description of the DB2 driver.
     */
    private static final String DRIVER_CLASS = "com.ibm.db2.jcc.DB2SimpleDataSource";
    /**
     * Connections counter.
     */
    @SuppressWarnings("PMD.AssignmentToNonFinalStatic")
    private static int qty;
    /**
     * Name of the database.
     */
    private final String databaseName;
    /**
     * Name of the server.
     */
    private final String hostname;

    /**
     * Instance's port.
     */
    private final int portNumber;

    /**
     * Creates an object that describes a DB2 connection.
     *
     * @param connectionsPool
     *            Associate connection pool.
     * @param defaultProperties
     *            Properties to connect to the database.
     * @param host
     *            Name of the server.
     * @param port
     *            Port of the instance.
     * @param dbName
     *            database.
     * @param username
     *            Connection user.
     * @param password
     *            Password.
     */
    public DB2Connection(final String connectionsPool,
            final Properties defaultProperties, final String host,
            final int port, final String dbName, final String username,
            final String password) {
        super(connectionsPool, defaultProperties, username, password);
        DB2Connection.qty = DB2Connection.qty + 1;
        // Changes the application name.
        this.getConnectionProperties().put("clientProgramName",
                "db2-jnrpe-" + DB2Connection.qty);
        // Shows descriptive message when errors.
        this.getConnectionProperties().put(
                "retrieveMessagesFromServerOnGetMessage", "true");

        this.setUrl("jdbc:db2://" + host + ":" + port + "/" + dbName);
        this.hostname = host;
        this.portNumber = port;
        this.databaseName = dbName;
    }

    /**
     * Returns the database.
     *
     * @return DB2Database.
     */
    public String getDatabaseName() {
        return this.databaseName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.github.angoca.db2jnrpe.database.AbstractDatabaseConnection#getDriverClass
     * ()
     */
    @Override
    @SuppressWarnings("PMD.CommentRequired")
    public String getDriverClass() {
        return DB2Connection.DRIVER_CLASS;
    }

    /**
     * Returns the hostname.
     *
     * @return Hostname.
     */
    public String getHostname() {
        return this.hostname;
    }

    /**
     * Returns the port number of the instance.
     *
     * @return Port.
     */
    public int getPortNumber() {
        return this.portNumber;
    }
}
