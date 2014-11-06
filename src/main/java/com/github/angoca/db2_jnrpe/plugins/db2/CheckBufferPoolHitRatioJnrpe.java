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
import java.util.Set;

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
        Database database = DatabasesManager.getInstance().getDatabase(
                this.getURL(cl));
        if (database == null) {
            String url = this.getURL(cl);
            database = new Database(url);
            DatabasesManager.getInstance().add(url, database);
        }
        Set<String> bufferpoolNames = database.getBufferpools().keySet();
        // Checks the values.
        if (!database.isBufferpoolListUpdated()) {
            System.out.println(">>>READ THRESHOLD FROM DATABASE");
            try {
                Map<String, BufferpoolRead> bufferpoolReads = CheckBufferPoolHitRatioDB2.check(this.getConnection(cl));
                database.setBufferpoolReads(bufferpoolReads);
                bufferpoolNames = database.getBufferpools().keySet();
            } catch (DatabaseConnectionException | MetricGatheringException e) {
                this.log.fatal("Error while retrieving names", e);
                throw new BadThresholdException(
                        "Problem retrieving the values "
                                + "for threshold from the database: "
                                + e.getMessage(), e);
            }
        }
        final String bufferpoolName = cl.getOptionValue("bufferpool");
        if ((bufferpoolName == null) || (bufferpoolName.compareTo("") == 0)) {
            String name;
            for (Iterator<String> iterator = bufferpoolNames.iterator(); iterator
                    .hasNext();) {
                String bpName = (String) iterator.next();
                name = CheckBufferPoolHitRatioJnrpe.THRESHOLD_NAME_BUFFERPOOL
                        + bpName;
                this.log.debug("Threshold for bufferpool: " + name);
                thrb.withLegacyThreshold(name, null,
                        cl.getOptionValue("warning", "90"),
                        cl.getOptionValue("critical", "95"));
            }
        } else if (bufferpoolNames.contains(bufferpoolName)) {
            this.log.debug("Threshold for bufferpool: " + bufferpoolName);
            thrb.withLegacyThreshold(
                    CheckBufferPoolHitRatioJnrpe.THRESHOLD_NAME_BUFFERPOOL
                            + bufferpoolName, null,
                    cl.getOptionValue("warning", "90"),
                    cl.getOptionValue("critical", "95"));
        } else {
            this.log.error("The bufferpool " + bufferpoolName
                    + " does not exist");
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
        Database database = DatabasesManager.getInstance().getDatabase(
                this.getURL(cl));
        Map<String, BufferpoolRead> bufferpoolReads = database.getBufferpools();
        // Converts result to arrays and create metrics.
        BigDecimal ratio;
        BigDecimal min;
        BigDecimal max;
        final List<Metric> res = new ArrayList<Metric>();
        final Iterator<String> iter = bufferpoolReads.keySet().iterator();
        while (iter.hasNext()) {
            String name = iter.next();
            final BufferpoolRead bpDesc = bufferpoolReads.get(name);
            name = CheckBufferPoolHitRatioJnrpe.THRESHOLD_NAME_BUFFERPOOL
                    + name;
            this.log.debug("Metrics: " + name);
            ratio = new BigDecimal(bpDesc.getRatio());
            final String message = String.format(
                    "Bufferpool %s at member %s has %s logical reads "
                            + "and %s physical reads, with a hit "
                            + "ratio of %s%%.", name, bpDesc.getMember(),
                    bpDesc.getLogicalReads(), bpDesc.getPhysicalReads(), ratio);
            min = new BigDecimal(0);
            max = new BigDecimal(100);

            res.add(new Metric(name, message, ratio, min, max));
        }
        this.log.debug(res.size() + " metrics");

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

        final String[] values = this.getURLValues(cl);
        final String hostname = values[0];
        int portNumber;
        final String portNumberString = values[1];
        this.log.debug("Port number: " + portNumberString);
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
        this.log.debug("Hostname " + hostname + "-Port " + portNumber + "-db"
                + databaseName + "-user " + username);

        final String databaseConnection = DB2Connection.class.getName();
        String connectionPool;
        connectionPool = com.github.angoca.db2_jnrpe.database.pools.c3p0.DBCP_c3p0.class
                .getName();
        // connectionPool =
        // com.github.angoca.db2_jnrpe.database.pools.db2direct.DBCP_db2Direct.class
        // .getName();
        // connectionPool =
        // com.github.angoca.db2_jnrpe.database.pools.hikari.DBCP_Hikari.class
        // .getName();
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

    /*
     * (non-Javadoc)
     * 
     * @see it.jnrpe.plugins.PluginBase#getPluginName()
     */
    @Override
    protected final String getPluginName() {
        return "CHECK_BUFFER_POOL_HIT_RATIO";
    }

    /**
     * Retrieves the connection URL to identify a database.
     * 
     * @param cl
     *            Command line.
     * @return Unique URL to the database.
     */
    private String getURL(ICommandLine cl) {
        String ret;
        String[] values = getURLValues(cl);
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
    private String[] getURLValues(ICommandLine cl) {
        String[] ret = new String[3];
        final String hostname = cl.getOptionValue("hostname");
        final String portNumberString = cl.getOptionValue("port");
        final String databaseName = cl.getOptionValue("database");
        ret[0] = hostname;
        ret[1] = portNumberString;
        ret[2] = databaseName;
        return ret;
    }

}
