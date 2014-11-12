package com.github.angoca.db2jnrpe.plugins.db2;

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

import com.github.angoca.db2jnrpe.database.DatabaseConnection;
import com.github.angoca.db2jnrpe.database.DatabaseConnectionException;
import com.github.angoca.db2jnrpe.database.DatabaseConnectionsManager;
import com.github.angoca.db2jnrpe.database.rdbms.db2.DB2Connection;

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
     * List of bufferpools.
     */
    private Map<String, BufferpoolRead> bufferpoolReads;

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
        DB2Database db2Database = DB2DatabasesManager.getInstance()
                .getDatabase(this.getId(cl));
        if (db2Database == null) {
            final String id = this.getId(cl);
            db2Database = new DB2Database(id);
            DB2DatabasesManager.getInstance().add(id, db2Database);
        }
        Set<String> bufferpoolNames = null;
        try {
            this.bufferpoolReads = db2Database.getBufferpoolsAndRefresh(this
                    .getConnection(cl));
            bufferpoolNames = this.bufferpoolReads.keySet();
        } catch (MetricGatheringException | DatabaseConnectionException e) {
            this.log.fatal("Error while retrieving names", e);
            throw new BadThresholdException("Problem retrieving the values "
                    + "for threshold from the database: " + e.getMessage(), e);
        } catch (final UnknownValueException e) {
            // There are not values in the cache. Do nothing.
        }
        if (this.bufferpoolReads != null) {
            final String bufferpoolName = cl.getOptionValue("bufferpool");
            if ((bufferpoolName == null) || (bufferpoolName.compareTo("") == 0)) {
                String name;
                for (final String string : bufferpoolNames) {
                    final String bpName = string;
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
                throw new BadThresholdException(
                        "The given bufferpool does not "
                                + "exist in the database.");
            }
        }

        // Metadata
        final boolean metadata = cl.hasOption("metadata");
        if (metadata) {
            thrb.withLegacyThreshold("Cache-data", null, null, null);
            thrb.withLegacyThreshold("Cache-old", null, null, null);
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
        final List<Metric> res;
        if (this.bufferpoolReads != null) {
            // Converts result to arrays and create metrics.
            BigDecimal ratio;
            BigDecimal min;
            BigDecimal max;
            res = new ArrayList<Metric>();
            final Iterator<String> iter = this.bufferpoolReads.keySet()
                    .iterator();
            while (iter.hasNext()) {
                String name = iter.next();
                final BufferpoolRead bpDesc = this.bufferpoolReads.get(name);
                name = CheckBufferPoolHitRatioJnrpe.THRESHOLD_NAME_BUFFERPOOL
                        + name;
                this.log.debug("Metrics: " + name);
                ratio = new BigDecimal(bpDesc.getLastRatio());
                final String message = String.format(
                        "Bufferpool %s at member %s has %s logical reads "
                                + "and %s physical reads, with a hit "
                                + "ratio of %s%%.", name, bpDesc.getMember(),
                        bpDesc.getLogicalReads(), bpDesc.getPhysicalReads(),
                        ratio);
                min = new BigDecimal(0);
                max = new BigDecimal(100);

                res.add(new Metric(name, message, ratio, min, max));
            }
            this.log.debug(res.size() + " metrics");

            // Metadata
            final boolean metadata = cl.hasOption("metadata");
            if (metadata) {
                final DB2Database db2Database = DB2DatabasesManager
                        .getInstance().getDatabase(this.getId(cl));
                res.add(new Metric("Cache-data", "", new BigDecimal(db2Database
                        .getLastRefresh()), null, null));
                res.add(new Metric("Cache-old", "", new BigDecimal(System
                        .currentTimeMillis() - db2Database.getLastRefresh()),
                        null, null));
            }
        } else {
            throw new MetricGatheringException("Values have not been gathered",
                    Status.UNKNOWN, null);
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

        final String[] values = this.getUrlValues(cl);
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
        connectionPool = com.github.angoca.db2jnrpe.database.pools.c3p0.Dbcp_c3p0.class
                .getName();
        // connectionPool =
        // com.github.angoca.db2jnrpe.database.pools.db2direct.Dbcp_db2Direct.class
        // .getName();
        // connectionPool =
        // com.github.angoca.db2jnrpe.database.pools.hikari.Dbcp_Hikari.class
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

    /**
     * Retrieves an ID to identify a database.
     *
     * @param cl
     *            Command line.
     * @return Unique URL to the database.
     */
    private String getId(ICommandLine cl) {
        String ret;
        final String[] values = this.getUrlValues(cl);
        ret = values[0] + ':' + values[1] + '/' + values[2];
        return ret;
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
     * Return the connection values.
     *
     * @param cl
     *            Command line.
     * @return Array with parameters.
     */
    private String[] getUrlValues(ICommandLine cl) {
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
