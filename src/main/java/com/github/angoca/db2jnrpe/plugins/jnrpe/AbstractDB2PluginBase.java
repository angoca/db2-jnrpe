package com.github.angoca.db2jnrpe.plugins.jnrpe;

import it.jnrpe.ICommandLine;
import it.jnrpe.Status;
import it.jnrpe.plugins.MetricGatheringException;
import it.jnrpe.plugins.PluginBase;

import com.github.angoca.db2jnrpe.database.AbstractDatabaseConnection;
import com.github.angoca.db2jnrpe.database.DatabaseConnectionException;
import com.github.angoca.db2jnrpe.database.DatabaseConnectionsManager;
import com.github.angoca.db2jnrpe.database.rdbms.db2.DB2Connection;

/**
 * This class contains the common methods for all DB2 plugins.
 *
 * @author Andres Gomez Casanova (@AngocA)
 * @version 2014-11-21
 */
@SuppressWarnings("PMD.CommentSize")
public abstract class AbstractDB2PluginBase extends PluginBase {

    /**
     * Empty constructor.
     */
    protected AbstractDB2PluginBase() {
        super();
    }

    /**
     * Given the connection parameters, it returns an object that wraps the
     * pooled connection.
     *
     * @param line
     *            Handler that contains the parameters.
     * @return Connection wrapper.
     * @throws MetricGatheringException
     *             If there is any error gathering the metrics.
     */
    protected final AbstractDatabaseConnection getConnection(
            final ICommandLine line) throws MetricGatheringException {
        assert line != null;

        final String[] values = AbstractDB2PluginBase.getUrlValues(line);
        int portNumber;
        final String portNumberString = values[1];
        try {
            portNumber = Integer.valueOf(portNumberString);
        } catch (final NumberFormatException ne) {
            this.log.error("Invalid port number " + portNumberString);
            throw new MetricGatheringException(
                    "Invalid format for port number", Status.UNKNOWN, ne);
        }
        final String hostname = values[0];
        final String databaseName = values[2];
        final String username = line.getOptionValue("username");
        final String password = line.getOptionValue("password");
        this.log.debug("Hostname:" + hostname + ";Port:" + portNumber + ";DB:"
                + databaseName + ";User:" + username);

        @SuppressWarnings("PMD.LawOfDemeter")
        final String dbConnection = DB2Connection.class.getName();
        @SuppressWarnings("PMD.LawOfDemeter")
        final String connectionPool = com.github.angoca.db2jnrpe.database.pools.hikari.DbcpHikari.class
                .getName();
        // connectionPool =
        // com.github.angoca.db2jnrpe.database.pools.c3p0.DbcpC3p0.class
        // .getName();
        // connectionPool =
        // com.github.angoca.db2jnrpe.database.pools.db2direct.DbcpDb2Direct.class
        // .getName();
        this.log.debug("Connection pool: " + connectionPool);
        AbstractDatabaseConnection dbConn = null;
        try {
            dbConn = DatabaseConnectionsManager.getInstance()
                    .getDatabaseConnection(connectionPool, dbConnection,
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
     * @param line
     *            Command line.
     * @return Unique URL to the database.
     */
    protected static final String getId(final ICommandLine line) {
        String ret;
        final String[] values = AbstractDB2PluginBase.getUrlValues(line);
        ret = values[0] + ':' + values[1] + '/' + values[2];
        return ret;
    }

    /**
     * Return the connection values.
     *
     * @param line
     *            Command line.
     * @return Array with parameters.
     */
    private static String[] getUrlValues(final ICommandLine line) {
        final String[] ret = new String[3];
        final String hostname = line.getOptionValue("hostname");
        final String portNumberString = line.getOptionValue("port");
        final String databaseName = line.getOptionValue("database");
        ret[0] = hostname;
        ret[1] = portNumberString;
        ret[2] = databaseName;
        return ret;
    }

}