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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

    /**
     * Prefix for thresholds.
     */
    private static final String THRESHOLD_NAME_BUFFERPOOL = "bufferpool_hit_ratio-";
    /**
     * List of bufferpool names.
     */
    private List<String> bufferpoolNames = new ArrayList<String>();

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
        try {
            this.bufferpoolNames = CheckBufferPoolHitRatioDB2
                    .getBufferpoolNames(this.getConnection(cl));
        } catch (DatabaseConnectionException | MetricGatheringException e) {
            log.fatal("Error while retrieving names", e);
            throw new BadThresholdException("Problem retrieving the values "
                    + "for threshold from the database: " + e.getMessage(), e);
        }
        final String bufferpoolName = cl.getOptionValue("bufferpool");
        if (bufferpoolName == null || bufferpoolName.compareTo("") == 0) {
            String name;
            for (int i = 0; i < this.bufferpoolNames.size(); i++) {
                name = CheckBufferPoolHitRatioJnrpe.THRESHOLD_NAME_BUFFERPOOL
                        + this.bufferpoolNames.get(i);
                log.debug("Threshold: " + name);
                thrb.withLegacyThreshold(name, null,
                        cl.getOptionValue("warning", "90"),
                        cl.getOptionValue("critical", "95"));
            }
        } else if (this.bufferpoolNames.contains(bufferpoolName)) {
            thrb.withLegacyThreshold(
                    CheckBufferPoolHitRatioJnrpe.THRESHOLD_NAME_BUFFERPOOL
                            + bufferpoolName, null,
                    cl.getOptionValue("warning", "90"),
                    cl.getOptionValue("critical", "95"));
        } else {
            throw new BadThresholdException("The given bufferpool does not "
                    + "exist in the database.");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.jnrpe.plugins.PluginBase#gatherMetrics(it.jnrpe.ICommandLine)
     */
    @Override
    public final Collection<Metric> gatherMetrics(final ICommandLine cl)
            throws MetricGatheringException {
        // Checks the values.
        Map<String, List<String>> bufferpoolsDesc;
        try {
            bufferpoolsDesc = CheckBufferPoolHitRatioDB2.check(this
                    .getConnection(cl));
        } catch (DatabaseConnectionException e) {
            log.fatal("Error while checking", e);
            throw new MetricGatheringException("Problem retrieving the values "
                    + "for metrics from the database: " + e.getMessage(),
                    Status.UNKNOWN, e);
        }

        // Converts result to arrays and create metrics.
        BigDecimal value;
        BigDecimal min;
        BigDecimal max;
        final List<Metric> res = new ArrayList<Metric>();
        Iterator<String> iter = bufferpoolsDesc.keySet().iterator();
        while (iter.hasNext()) {
            String name = iter.next();
            if (this.bufferpoolNames.add(name)) {
                List<String> bpDesc = bufferpoolsDesc.get(name);
                name = CheckBufferPoolHitRatioJnrpe.THRESHOLD_NAME_BUFFERPOOL
                        + name;
                log.debug("Metrics: " + name);
                value = new BigDecimal(bpDesc.get(2));
                String message = String.format(
                        "Bufferpool %s at member %s has %s logical reads "
                                + "and %s physical reads, with a hit "
                                + "ratio of %s%%.", name, bpDesc.get(3),
                        bpDesc.get(0), bpDesc.get(1), value);
                min = new BigDecimal(0);
                max = new BigDecimal(100);

                res.add(new Metric(name, message, value, min, max));
            }
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
        final String connectionPool = com.github.angoca.db2_jnrpe.database.pools.c3p0.DBCP_c3p0.class
                .getName();
        // final String connectionPool =
        // com.github.angoca.db2_jnrpe.database.pools.db2direct.DBCP_db2Direct.class
        // .getName();
        // final String connectionPool =
        // com.github.angoca.db2_jnrpe.database.pools.hikari.DBCP_Hikari.class
        // .getName();
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
