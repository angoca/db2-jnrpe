package com.github.angoca.db2jnrpe.plugins.jnrpe;

import it.jnrpe.ICommandLine;
import it.jnrpe.Status;
import it.jnrpe.plugins.Metric;
import it.jnrpe.plugins.MetricGatheringException;
import it.jnrpe.utils.BadThresholdException;
import it.jnrpe.utils.thresholds.ThresholdsEvaluatorBuilder;

import java.math.BigDecimal;
import java.sql.Timestamp;
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
import com.github.angoca.db2jnrpe.plugins.db2.BufferpoolRead;
import com.github.angoca.db2jnrpe.plugins.db2.DB2Database;
import com.github.angoca.db2jnrpe.plugins.db2.DB2DatabasesManager;
import com.github.angoca.db2jnrpe.plugins.db2.UnknownValueException;

/**
 * This plugin allows to see the bufferpool hit ratio of the bufferpool or a
 * single bufferpool.
 *
 * @author Andres Gomez Casanova (@AngocA)
 * @version 2014-11-03
 */
public final class CheckBufferPoolHitRatioJnrpe extends DB2PluginBase {

    /**
     * Tests the complete chain.
     * 
     * @param args
     *            Nothing.
     * @throws Exception
     *             If any error occur.
     */
    public static void main(final String[] args) throws Exception {
        DatabaseConnection dbConn = null;
        dbConn = DatabaseConnectionsManager
                .getInstance()
                .getDatabaseConnection(
                        com.github.angoca.db2jnrpe.database.pools.hikari.DbcpHikari.class
                                .getName(), DB2Connection.class.getName(),
                        "localhost", 50000, "sample", "db2inst1", "db2inst1");
        String id = "localhost:50000/sample";

        new CheckBufferPoolHitRatioJnrpe().getBufferpoolNames(id, dbConn);
        Thread.sleep(5000);
        new CheckBufferPoolHitRatioJnrpe().getBufferpoolNames(id, dbConn);
        Thread.sleep(5000);
        new CheckBufferPoolHitRatioJnrpe().getBufferpoolNames(id, dbConn);
        Thread.sleep(5000);
        new CheckBufferPoolHitRatioJnrpe().getBufferpoolNames(id, dbConn);
    }

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
    public void configureThresholdEvaluatorBuilder(
            final ThresholdsEvaluatorBuilder thrb, final ICommandLine cl)
            throws BadThresholdException {
        Set<String> bufferpoolNames;
        final String dbId = this.getId(cl);
        this.log.warn("Database: " + dbId);
        try {
            bufferpoolNames = this.getBufferpoolNames(dbId,
                    this.getConnection(cl));
        } catch (MetricGatheringException e) {
            this.log.fatal("Error while retrieving names", e);
            throw new BadThresholdException("Problem retrieving the values "
                    + "for threshold from the database: " + e.getMessage(), e);
        }
        if (this.bufferpoolReads != null) {
            final String bufferpoolName = cl.getOptionValue("bufferpool");
            if ((bufferpoolName == null) || (bufferpoolName.compareTo("") == 0)) {
                String logMessage = "Threshold for BPs: ";
                for (final String string : bufferpoolNames) {
                    final String bpName = string;
                    logMessage += bpName + " ";
                    thrb.withLegacyThreshold(bpName, null,
                            cl.getOptionValue("warning", "95"),
                            cl.getOptionValue("critical", "90"));
                }
                this.log.debug(logMessage);
            } else if (bufferpoolNames.contains(bufferpoolName)) {
                this.log.debug("Threshold for bufferpool: " + bufferpoolName);
                thrb.withLegacyThreshold(bufferpoolName, null,
                        cl.getOptionValue("warning", "95"),
                        cl.getOptionValue("critical", "90"));
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

    /**
     * Returns the names of the bufferpoools.
     * 
     * @param id
     *            Database id.
     * @param conn
     *            Connection to the database.
     * @return Set of bufferpool names.
     * @throws BadThresholdException
     *             If there is an error retrieving the values.
     */
    private Set<String> getBufferpoolNames(final String id,
            final DatabaseConnection conn) throws BadThresholdException {
        DB2Database db2Database = DB2DatabasesManager.getInstance()
                .getDatabase(id);
        if (db2Database == null) {
            db2Database = new DB2Database(id);
            DB2DatabasesManager.getInstance().add(id, db2Database);
        }
        Set<String> bufferpoolNames = null;
        try {
            this.bufferpoolReads = db2Database.getBufferpoolsAndRefresh(conn);
            bufferpoolNames = this.bufferpoolReads.keySet();
        } catch (DatabaseConnectionException e) {
            this.log.fatal("Error while retrieving names", e);
            throw new BadThresholdException("Problem retrieving the values "
                    + "for threshold from the database: " + e.getMessage(), e);
        } catch (final UnknownValueException e) {
            // There are not values in the cache. Do nothing.
        }
        return bufferpoolNames;
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.jnrpe.plugins.PluginBase#gatherMetrics(it.jnrpe.ICommandLine)
     */
    @Override
    public Collection<Metric> gatherMetrics(final ICommandLine cl)
            throws MetricGatheringException {
        final String dbId = this.getId(cl);
        this.log.warn("Database: " + dbId);
        final List<Metric> res;
        if (this.bufferpoolReads != null) {
            if (DB2DatabasesManager.getInstance().getDatabase(dbId)
                    .isRecentBufferpoolRead()) {
                this.log.warn("Values are old: "
                        + new Timestamp(DB2DatabasesManager.getInstance()
                                .getDatabase(dbId).getLastRefresh()));
                throw new MetricGatheringException("Values are not recent",
                        Status.UNKNOWN, null);
            }
            // Converts result to arrays and create metrics.
            BigDecimal ratio;
            res = new ArrayList<Metric>();
            final Iterator<String> iter = this.bufferpoolReads.keySet()
                    .iterator();
            String logMessage = "Metrics: ";
            while (iter.hasNext()) {
                String name = iter.next();
                final BufferpoolRead bpDesc = this.bufferpoolReads.get(name);
                ratio = new BigDecimal(bpDesc.getLastRatio());
                logMessage += String.format("BP %s: %.1f%% ", name, ratio);
                final String message = String.format(
                        "BP %s::%d has %d LR and %d PR, with a ratio of "
                                + "%.1f%%.", name, bpDesc.getMember(),
                        bpDesc.getLogicalReads(), bpDesc.getPhysicalReads(),
                        ratio);

                res.add(new Metric(name, message, ratio, null, null));
            }
            this.log.debug(res.size() + " metrics. " + logMessage);

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
            this.bufferpoolReads = null;
        } else {
            this.log.warn(dbId + "::No values");
            throw new MetricGatheringException("Values have not been gathered",
                    Status.UNKNOWN, null);
        }

        return res;
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.jnrpe.plugins.PluginBase#getPluginName()
     */
    @Override
    protected String getPluginName() {
        return "CHECK_BUFFERPOOL_HIT_RATIO";
    }

}
