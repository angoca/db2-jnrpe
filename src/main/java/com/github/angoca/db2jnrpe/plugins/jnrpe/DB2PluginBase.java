package com.github.angoca.db2jnrpe.plugins.jnrpe;

import com.github.angoca.db2jnrpe.database.DatabaseConnection;
import com.github.angoca.db2jnrpe.database.DatabaseConnectionException;
import com.github.angoca.db2jnrpe.database.DatabaseConnectionsManager;
import com.github.angoca.db2jnrpe.database.rdbms.db2.DB2Connection;

import it.jnrpe.ICommandLine;
import it.jnrpe.Status;
import it.jnrpe.plugins.MetricGatheringException;
import it.jnrpe.plugins.PluginBase;

/**
 * This class contains the common methods for all DB2 plugins.
 * 
 * @author Andres Gomez Casanova (@AngocA)
 * @version 2014-11-21
 */
public abstract class DB2PluginBase extends PluginBase {

    /**
     * Calls the parent constructor.
     */
    public DB2PluginBase() {
        super();
    }

    /**
     * Given the connection parameters, it returns an object that wraps the
     * pooled connection.
     *
     * @param cl
     *            Handler that contains the parameters.
     * @return Connection wrapper.
     * @throws MetricGatheringException
     *             If there is any error gathering the metrics.
     */
    protected final DatabaseConnection getConnection(final ICommandLine cl)
            throws MetricGatheringException {
        assert cl != null;

        final String[] values = this.getUrlValues(cl);
        final String hostname = values[0];
        int portNumber;
        final String portNumberString = values[1];
        try {
            portNumber = Integer.valueOf(portNumberString);
        } catch (final NumberFormatException ne) {
            this.log.error("Invalid port number " + portNumberString);
            throw new MetricGatheringException(
                    "Invalid format for port number", Status.UNKNOWN, ne);
        }
        final String databaseName = values[2];
        final String username = cl.getOptionValue("username");
        final String password = cl.getOptionValue("password");
        this.log.debug("Hostname:" + hostname + ";Port:" + portNumber + ";DB:"
                + databaseName + ";User:" + username);

        final String databaseConnection = DB2Connection.class.getName();
        String connectionPool;
        // connectionPool =
        // com.github.angoca.db2jnrpe.database.pools.c3p0.DbcpC3p0.class
        // .getName();
        // connectionPool =
        // com.github.angoca.db2jnrpe.database.pools.db2direct.DbcpDb2Direct.class
        // .getName();
        connectionPool = com.github.angoca.db2jnrpe.database.pools.hikari.DbcpHikari.class
                .getName();
        this.log.debug("Connection pool: " + connectionPool);
        DatabaseConnection dbConn = null;
        try {
            dbConn = DatabaseConnectionsManager.getInstance()
                    .getDatabaseConnection(connectionPool, databaseConnection,
                            hostname, portNumber, databaseName, username,
                            password);
        } catch (final DatabaseConnectionException dbe) {
            this.log.fatal("Error while establishing conncetion", dbe);
            throw new MetricGatheringException("Error accesing the database",
                    Status.UNKNOWN, dbe);
        }

        assert dbConn != null;
        return dbConn;
    }

    /**
     * Retrieves an ID to identify a database.
     *
     * @param cl
     *            Command line.
     * @return Unique URL to the database.
     */
    protected final String getId(final ICommandLine cl) {
        String ret;
        final String[] values = this.getUrlValues(cl);
        ret = values[0] + ':' + values[1] + '/' + values[2];
        return ret;
    }

    /**
     * Return the connection values.
     *
     * @param cl
     *            Command line.
     * @return Array with parameters.
     */
    private String[] getUrlValues(final ICommandLine cl) {
        final String[] ret = new String[3];
        final String hostname = cl.getOptionValue("hostname");
        final String portNumberString = cl.getOptionValue("port");
        final String databaseName = cl.getOptionValue("database");
        ret[0] = hostname;
        ret[1] = portNumberString;
        ret[2] = databaseName;
        return ret;
    }

}