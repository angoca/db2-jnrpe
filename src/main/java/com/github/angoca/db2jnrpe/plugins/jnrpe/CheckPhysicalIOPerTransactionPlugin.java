package com.github.angoca.db2jnrpe.plugins.jnrpe;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.github.angoca.db2jnrpe.plugins.db2.DB2Database;
import com.github.angoca.db2jnrpe.plugins.db2.DB2DatabasesManager;
import com.github.angoca.db2jnrpe.plugins.db2.DatabaseSnapshot;
import com.github.angoca.db2jnrpe.plugins.db2.UnknownValueException;

import it.jnrpe.ICommandLine;
import it.jnrpe.Status;
import it.jnrpe.plugins.Metric;
import it.jnrpe.plugins.MetricGatheringException;
import it.jnrpe.utils.BadThresholdException;
import it.jnrpe.utils.thresholds.ThresholdsEvaluatorBuilder;

/**
 * This plugin allows to calculate the average physical IO per transaction.<br/>
 * In order to execute this plugin, it is necessary to have DB2 in at least one
 * of the following version:
 * <ul>
 * <li>v9.7 FP1</li>
 * <li>v9.8 FP2</li>
 * <li>v10.1 or newer</li>
 * </ul>
 *
 * @author Andres Gomez Casanova (@AngocA)
 * @version 2014-11-21
 */
public final class CheckPhysicalIOPerTransactionPlugin extends
        AbstractDB2PluginBase {

    /**
     * Label for the physical IO per transaction.
     */
    private static final String IO_PER_TRANSACTION = "IOperTrans";

    /**
     * Value to consider the physical IO as warning.
     */
    private static final String CRITICAL_VALUE = "20:";
    /**
     * Value to consider the physical IO as critical.
     */
    private static final String WARNING_VALUE = "15:";

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
        final String dbId = this.getId(cl);
        this.log.warn("Database: " + dbId);
        thrb.withLegacyThreshold(
                CheckPhysicalIOPerTransactionPlugin.IO_PER_TRANSACTION, null,
                cl.getOptionValue("warning",
                        CheckPhysicalIOPerTransactionPlugin.WARNING_VALUE),
                cl.getOptionValue("critical",
                        CheckPhysicalIOPerTransactionPlugin.CRITICAL_VALUE));

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
    public Collection<Metric> gatherMetrics(final ICommandLine cl)
            throws MetricGatheringException {
        final List<Metric> res = new ArrayList<Metric>();

        final String id = this.getId(cl);
        DB2Database db2Database = DB2DatabasesManager.getInstance()
                .getDatabase(id);
        if (db2Database == null) {
            db2Database = new DB2Database(id);
            DB2DatabasesManager.getInstance().add(id, db2Database);
        }
        DatabaseSnapshot snapshot;
        try {
            snapshot = db2Database
                    .getSnapshotAndRefresh(this.getConnection(cl));

            String message = "The average of physical I/O activity per "
                    + "committed transaction is "
                    + snapshot.getLastQuantityReadsWritesPerTransaction();
            res.add(new Metric(
                    CheckPhysicalIOPerTransactionPlugin.IO_PER_TRANSACTION,
                    message, new BigDecimal(snapshot
                            .getLastQuantityReadsWritesPerTransaction()), null,
                    null));
        } catch (final UnknownValueException e) {
            this.log.warn(id + "::No values");
            throw new MetricGatheringException(
                    "Not enough values have been gathered: " + e.getMessage(),
                    Status.UNKNOWN, null);
        }

        // Metadata
        final boolean metadata = cl.hasOption("metadata");
        if (metadata) {
            res.add(new Metric("Cache-data", "", new BigDecimal(db2Database
                    .getSnap().getLastSnapshotRefresh()), null, null));
            res.add(new Metric("Cache-old", "", new BigDecimal(System
                    .currentTimeMillis()
                    - db2Database.getSnap().getLastSnapshotRefresh()), null,
                    null));
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
        return "Check_Physical_IO_Per_Transaction";
    }
}
