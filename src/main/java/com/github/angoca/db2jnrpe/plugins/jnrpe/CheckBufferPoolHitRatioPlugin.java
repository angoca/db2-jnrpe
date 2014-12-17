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

import org.slf4j.LoggerFactory;

import com.github.angoca.db2jnrpe.database.AbstractDatabaseConnection;
import com.github.angoca.db2jnrpe.database.DatabaseConnectionsManager;
import com.github.angoca.db2jnrpe.database.rdbms.db2.DB2Connection;
import com.github.angoca.db2jnrpe.plugins.db2.BufferpoolRead;
import com.github.angoca.db2jnrpe.plugins.db2.Bufferpools;
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
@SuppressWarnings("PMD.CommentSize")
public final class CheckBufferPoolHitRatioPlugin extends AbstractDB2PluginBase {

    /**
     * Critical value by default: X < 90%.
     */
    private static final String CRITICAL_VALUE = "90";
    /**
     * A thousand.
     */
    private static final int KILO = 1000;
    /**
     * Logger.
     */
    private static final org.slf4j.Logger LOGGER = LoggerFactory
            .getLogger(CheckBufferPoolHitRatioPlugin.class);
    /**
     * A hundred.
     */
    private static final int MEGA = 1000000;
    /**
     * Warning value by default: X < 95%.
     */
    private static final String WARNING_VALUE = "95";

    /**
     * Tests the complete chain.
     *
     * @param args
     *            Nothing.
     * @throws Exception
     *             If any error occur.
     */
    @SuppressWarnings("PMD")
    public static void main(final String[] args) throws Exception {
        // CHECKSTYLE:OFF
        AbstractDatabaseConnection dbConn = null;
        dbConn = DatabaseConnectionsManager
                .getInstance()
                .getDatabaseConnection(
                        com.github.angoca.db2jnrpe.database.pools.hikari.DbcpHikari.class
                                .getName(), DB2Connection.class.getName(),
                        "localhost", 50000, "sample", "db2inst1", "db2inst1");
        final String id = "localhost:50000/sample";

        new CheckBufferPoolHitRatioPlugin().getBufferpoolNames(id, dbConn);
        Thread.sleep(5000);
        new CheckBufferPoolHitRatioPlugin().getBufferpoolNames(id, dbConn);
        Thread.sleep(5000);
        new CheckBufferPoolHitRatioPlugin().getBufferpoolNames(id, dbConn);
        Thread.sleep(5000);
        new CheckBufferPoolHitRatioPlugin().getBufferpoolNames(id, dbConn);
        // CHECKSTYLE:ON
    }

    /**
     * List of bufferpools.
     */
    private transient Map<String, BufferpoolRead> bufferpoolReads;

    /*
     * (non-Javadoc)
     * 
     * @see
     * it.jnrpe.plugins.PluginBase#configureThresholdEvaluatorBuilder(it.jnrpe
     * .utils.thresholds.ThresholdsEvaluatorBuilder, it.jnrpe.ICommandLine)
     */
    @Override
    @SuppressWarnings("PMD.CommentRequired")
    public void configureThresholdEvaluatorBuilder(
            final ThresholdsEvaluatorBuilder thrb, final ICommandLine line)
            throws BadThresholdException {
        Set<String> bufferpoolNames;
        final String dbId = AbstractDB2PluginBase.getId(line);
        this.log.warn("Database: " + dbId);
        try {
            bufferpoolNames = this.getBufferpoolNames(dbId,
                    this.getConnection(line));
        } catch (final MetricGatheringException e) {
            this.log.fatal("Error while retrieving names", e);
            throw new BadThresholdException("Problem retrieving the values "
                    + "for threshold from the database: " + e.getMessage(), e);
        }
        if (this.bufferpoolReads != null) {
            final String bufferpoolName = line.getOptionValue("bufferpool");
            if (bufferpoolName == null || bufferpoolName.compareTo("") == 0) {
                final StringBuffer logMessage = new StringBuffer(
                        "Threshold for BPs: ");
                for (final String string : bufferpoolNames) {
                    final String bpName = string;
                    logMessage.append(bpName).append(' ');
                    thrb.withLegacyThreshold(
                            bpName,
                            null,
                            line.getOptionValue("warning",
                                    CheckBufferPoolHitRatioPlugin.WARNING_VALUE),
                            line.getOptionValue("critical",
                                    CheckBufferPoolHitRatioPlugin.CRITICAL_VALUE));
                }
                this.log.debug(logMessage.toString());
            } else if (bufferpoolNames.contains(bufferpoolName)) {
                this.log.debug("Threshold for bufferpool: " + bufferpoolName);
                thrb.withLegacyThreshold(bufferpoolName, null, line
                        .getOptionValue("warning",
                                CheckBufferPoolHitRatioPlugin.WARNING_VALUE),
                        line.getOptionValue("critical",
                                CheckBufferPoolHitRatioPlugin.CRITICAL_VALUE));
            } else {
                this.log.error("The bufferpool " + bufferpoolName
                        + " does not exist");
                throw new BadThresholdException(
                        "The given bufferpool does not "
                                + "exist in the database.");
            }
        }

        // Metadata
        final boolean metadata = line.hasOption("metadata");
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
    @SuppressWarnings({ "PMD.CommentRequired",
            "PMD.AvoidInstantiatingObjectsInLoops" })
    public Collection<Metric> gatherMetrics(final ICommandLine line)
            throws MetricGatheringException {
        final String dbId = AbstractDB2PluginBase.getId(line);
        this.log.warn("Database: " + dbId);
        final List<Metric> res = new ArrayList<Metric>();
        if (this.bufferpoolReads == null) {
            this.log.warn(dbId + "::No values");
            throw new MetricGatheringException("Values have not been gathered",
                    Status.UNKNOWN, null);
        }
        if (DB2DatabasesManager.getInstance().getDatabase(dbId)
                .getBufferpools().isRecentBufferpoolRead()) {
            this.log.warn("Values are old: "
                    + new Timestamp(DB2DatabasesManager.getInstance()
                            .getDatabase(dbId).getBufferpools()
                            .getLastBufferpoolRefresh()));
            throw new MetricGatheringException("Values are not recent",
                    Status.UNKNOWN, null);
        }
        // Converts result to arrays and create metrics.
        BigDecimal ratio;
        final Iterator<String> iter = this.bufferpoolReads.keySet().iterator();
        final StringBuffer logMessage = new StringBuffer("Metrics: ");
        while (iter.hasNext()) {
            final String name = iter.next();
            final BufferpoolRead bpDesc = this.bufferpoolReads.get(name);
            ratio = new BigDecimal(bpDesc.getLastRatio());
            logMessage.append(String.format("BP %s: %.1f%% ", name, ratio));
            final String logStr = CheckBufferPoolHitRatioPlugin
                    .getSimplifiedValue(bpDesc.getLogicalReads());
            final String phyStr = CheckBufferPoolHitRatioPlugin
                    .getSimplifiedValue(bpDesc.getPhysicalReads());
            final String message = String.format(
                    "%s at %d has %s LR and %s PR, ratio of " + "%.1f%%.",
                    name, bpDesc.getMember(), logStr, phyStr, ratio);

            res.add(new Metric(name, message, ratio, null, null));
        }
        this.log.debug(res.size() + " metrics. " + logMessage);

        // Metadata
        final boolean metadata = line.hasOption("metadata");
        if (metadata) {
            final DB2Database db2Database = DB2DatabasesManager.getInstance()
                    .getDatabase(AbstractDB2PluginBase.getId(line));
            res.add(new Metric("Cache-data", "", new BigDecimal(db2Database
                    .getBufferpools().getLastBufferpoolRefresh()), null, null));
            res.add(new Metric("Cache-old", "", new BigDecimal(System
                    .currentTimeMillis()
                    - db2Database.getBufferpools().getLastBufferpoolRefresh()),
                    null, null));
        }

        return res;
    }

    /**
     * Returns the names of the bufferpoools.
     *
     * @param identification
     *            Database id.
     * @param conn
     *            Connection to the database.
     * @return Set of bufferpool names.
     * @throws BadThresholdException
     *             If there is an error retrieving the values.
     */
    private Set<String> getBufferpoolNames(final String identification,
            final AbstractDatabaseConnection conn) throws BadThresholdException {
        DB2Database db2Database = DB2DatabasesManager.getInstance()
                .getDatabase(identification);
        if (db2Database == null) {
            db2Database = new DB2Database(identification);
            DB2DatabasesManager.getInstance().add(identification, db2Database);
        }
        Set<String> bufferpoolNames = null;
        try {
            final Bufferpools bufferpools = db2Database
                    .getBufferpoolsAndRefresh(conn);
            if (bufferpools != null) {
                this.bufferpoolReads = bufferpools.getBufferpoolReads();
                bufferpoolNames = this.bufferpoolReads.keySet();
            }
        } catch (final UnknownValueException e) {
            // There are not values in the cache. Do nothing.
            CheckBufferPoolHitRatioPlugin.LOGGER
                    .info("Values have not been read");
        }
        return bufferpoolNames;
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.jnrpe.plugins.PluginBase#getPluginName()
     */
    @Override
    @SuppressWarnings("PMD.CommentRequired")
    protected String getPluginName() {
        return "Check_Bufferpool_Hit_Ratio";
    }

    /**
     * Returns a simplified value for long values.
     *
     * @param value
     *            Value with a log of digits.
     * @return Metrical notation to simplify big values.
     */
    private static String getSimplifiedValue(final long value) {
        String logStr;
        if (value > CheckBufferPoolHitRatioPlugin.MEGA) {
            logStr = value / CheckBufferPoolHitRatioPlugin.MEGA + "M";
        } else if (value > CheckBufferPoolHitRatioPlugin.KILO) {
            logStr = value / CheckBufferPoolHitRatioPlugin.KILO + "K";
        } else {
            logStr = Long.toString(value);
        }
        return logStr;
    }

}
