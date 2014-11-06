package com.github.angoca.db2_jnrpe.database.rdbms.db2;

import java.util.Properties;

import com.github.angoca.db2_jnrpe.database.DatabaseConnection;

/**
 * Description of the parameter for a DB2 connection.
 *
 * @author Andres Gomez Casanova (@AngocA)
 * @version 2014-11-03
 */
public final class DB2Connection extends DatabaseConnection {
    /**
     * Connections counter.
     */
    private static int qty = 0;
    /**
     * Name of the database
     */
    private final String databaseName;
    /**
     * Description of the DB2 driver.
     */
    protected final String driverClass = "com.ibm.db2.jcc.DB2SimpleDataSource";
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
     * @param hostname
     *            Name of the server.
     * @param portNumber
     *            Port of the instance.
     * @param databaseName
     *            database.
     * @param username
     *            Connection user.
     * @param password
     *            Password.
     */
    public DB2Connection(final String connectionsPool,
            final Properties defaultProperties, final String hostname,
            final int portNumber, final String databaseName,
            final String username, final String password) {
        super(connectionsPool, defaultProperties, username, password);
        DB2Connection.qty = DB2Connection.qty + 1;
        // Changes the application name.
        this.connectionProperties.put("clientProgramName", "db2-jnrpe-"
                + DB2Connection.qty);
        // Shows descriptive message when errors.
        this.connectionProperties.put("retrieveMessagesFromServerOnGetMessage",
                "true");

        this.setURL("jdbc:db2://" + hostname + ":" + portNumber + "/"
                + databaseName);
        this.hostname = hostname;
        this.portNumber = portNumber;
        this.databaseName = databaseName;
    }

    /**
     * Returns the database.
     *
     * @return Database.
     */
    public final String getDatabaseName() {
        return this.databaseName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.github.angoca.db2_jnrpe.database.DatabaseConnection#getDriverClass()
     */
    @Override
    public final String getDriverClass() {
        return this.driverClass;
    }

    /**
     * Returns the hostname.
     *
     * @return Hostname.
     */
    public final String getHostname() {
        return this.hostname;
    }

    /**
     * Returns the port number of the instance.
     *
     * @return Port.
     */
    public final int getPortNumber() {
        return this.portNumber;
    }
}
