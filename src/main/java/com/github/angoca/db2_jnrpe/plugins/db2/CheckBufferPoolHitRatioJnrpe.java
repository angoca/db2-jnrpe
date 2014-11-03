package com.github.angoca.db2_jnrpe.plugins.db2;

import it.jnrpe.ICommandLine;
import it.jnrpe.Status;
import it.jnrpe.plugins.Metric;
import it.jnrpe.plugins.MetricGatheringException;
import it.jnrpe.plugins.PluginBase;
import it.jnrpe.utils.BadThresholdException;
import it.jnrpe.utils.thresholds.ThresholdsEvaluatorBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.github.angoca.db2_jnrpe.database.DatabaseConnection;
import com.github.angoca.db2_jnrpe.database.DatabaseConnectionException;
import com.github.angoca.db2_jnrpe.database.DatabaseConnectionsManager;
import com.github.angoca.db2_jnrpe.database.rdbms.db2.DB2Connection;

/**
 * This is the bridge between jNRPE and the connection manager (correction and
 * connections pool). This class does not have direct dependencies with any DB2
 * component.
 * 
 * @author Andres Gomez Casanova (@AngocA)
 * @version 2014-11-03
 */
public final class CheckBufferPoolHitRatioJnrpe extends PluginBase {

    /*
     * (non-Javadoc)
     * 
     * @see
     * it.jnrpe.plugins.PluginBase#configureThresholdEvaluatorBuilder(it.jnrpe
     * .utils.thresholds.ThresholdsEvaluatorBuilder, it.jnrpe.ICommandLine)
     */
    @Override
    public final void configureThresholdEvaluatorBuilder(
            final ThresholdsEvaluatorBuilder thrb, final ICommandLine cl)
            throws BadThresholdException {
        // TODO check all bufferpools
        // TODO default value
        thrb.withLegacyThreshold("bufferpool-hit-ratio_", null,
                cl.getOptionValue("warning"), cl.getOptionValue("critical"));
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.jnrpe.plugins.PluginBase#gatherMetrics(it.jnrpe.ICommandLine)
     */
    @Override
    public final Collection<Metric> gatherMetrics(final ICommandLine cl)
            throws MetricGatheringException {
        // Gets the connection.
        final DatabaseConnection dbConn = getConnection(cl);
        // Checks the values.
        String[][] values;
        try {
            values = CheckBufferPoolHitRatioDB2.check(dbConn);
        } catch (DatabaseConnectionException e) {
            throw new MetricGatheringException("Problem retrieving the values "
                    + "from the database", Status.UNKNOWN, e);
        }

        // Converts result to arrays.
        BigDecimal value;
        BigDecimal min;
        BigDecimal max;
        final List<Metric> res = new ArrayList<Metric>();
        for (int i = 0; i < values.length; i++) {
            String name = "bufferpool-hit-ratio_" + values[i][0];
            String message = String.format("Bufferpool %s at member %s has %s "
                    + "logical reads and %s physical reads, with a hit "
                    + "ratio of %s%%.", values[i][0], values[i][4],
                    values[i][1], values[0][2], values[i][3]);
            value = new BigDecimal(values[i][3]);
            min = new BigDecimal(0);
            max = new BigDecimal(100);

            res.add(new Metric(name, message, value, min, max));
        }

        return res;
    }

    /**
     * Given the connection parameters, it returns an object that wraps the
     * pooled connection.
     * 
     * @param cl
     *            Handler that contains the parameters.
     * @return Connection wrapper.
     */
    private final DatabaseConnection getConnection(final ICommandLine cl)
            throws MetricGatheringException {
        assert cl != null;

        final String hostname = cl.getOptionValue("hostname");
        int portNumber;
        final String portNumberString = cl.getOptionValue("port");
        log.fatal("Port number: " + portNumberString);
        try {
            portNumber = Integer.valueOf(portNumberString);
        } catch (NumberFormatException ne) {
            throw new MetricGatheringException(
                    "Invalid format for port number", Status.UNKNOWN, ne);
        }
        final String databaseName = cl.getOptionValue("database");
        final String username = cl.getOptionValue("username");
        final String password = cl.getOptionValue("password");
        log.debug("Hostname " + hostname + "-Port " + portNumber + "-db"
                + databaseName + "-user " + username);

        final String databaseConnection = DB2Connection.class.getName();
        final String connectionPool = com.github.angoca.db2_jnrpe.database.pools.c3p0.DBBroker_c3p0.class
                .getName();
        DatabaseConnection dbConn = null;
        try {
            dbConn = DatabaseConnectionsManager.getInstance()
                    .getDatabaseConnection(connectionPool, databaseConnection,
                            hostname, portNumber, databaseName, username,
                            password);
        } catch (DatabaseConnectionException dbe) {
            throw new MetricGatheringException("Error accesing the database",
                    Status.UNKNOWN, dbe);
        }

        assert dbConn != null;
        return dbConn;
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.jnrpe.plugins.PluginBase#getPluginName()
     */
    @Override
    protected final String getPluginName() {
        return "CHECK_BUFFER_POOL_HIT_RATIO";
    }
}
