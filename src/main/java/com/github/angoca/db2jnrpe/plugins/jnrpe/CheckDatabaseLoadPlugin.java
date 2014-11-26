package com.github.angoca.db2jnrpe.plugins.jnrpe;

import it.jnrpe.ICommandLine;
import it.jnrpe.Status;
import it.jnrpe.plugins.Metric;
import it.jnrpe.plugins.MetricGatheringException;
import it.jnrpe.utils.BadThresholdException;
import it.jnrpe.utils.thresholds.ThresholdsEvaluatorBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.github.angoca.db2jnrpe.plugins.db2.DB2Database;
import com.github.angoca.db2jnrpe.plugins.db2.DB2DatabasesManager;
import com.github.angoca.db2jnrpe.plugins.db2.DatabaseSnapshot;
import com.github.angoca.db2jnrpe.plugins.db2.UnknownValueException;

/**
 * This is the implementation of the plugin that allows to monitor the load of
 * the database.
 *
 * @author Andres Gomez Casanova (@AngocA)
 * @version 2014-11-21
 */
public final class CheckDatabaseLoadPlugin extends AbstractDB2PluginBase {

    /**
     * Name of the metric for the quantity of commits.
     */
    private static final String COMMIT_LOAD = "commitsLoad";
    /**
     * Critical value by default: 10K operations.
     */
    private static final String CRITICAL_VALUE = "10000";
    /**
     * Name of the metric for the quantity of selects.
     */
    private static final String SELECT_LOAD = "selectsLoad";
    /**
     * Name of the metric for the insert update and deletes.
     */
    private static final String UID_LOAD = "uidLoad";
    /**
     * Warning value by default: 7K operations.
     */
    private static final String WARNING_VALUE = "7000";

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
        thrb.withLegacyThreshold(CheckDatabaseLoadPlugin.UID_LOAD, null, cl
                .getOptionValue("warning",
                        CheckDatabaseLoadPlugin.WARNING_VALUE), cl
                .getOptionValue("critical",
                        CheckDatabaseLoadPlugin.CRITICAL_VALUE));
        thrb.withLegacyThreshold(CheckDatabaseLoadPlugin.SELECT_LOAD, null, cl
                .getOptionValue("warning",
                        CheckDatabaseLoadPlugin.WARNING_VALUE), cl
                .getOptionValue("critical",
                        CheckDatabaseLoadPlugin.CRITICAL_VALUE));
        thrb.withLegacyThreshold(CheckDatabaseLoadPlugin.COMMIT_LOAD, null, cl
                .getOptionValue("warning",
                        CheckDatabaseLoadPlugin.WARNING_VALUE), cl
                .getOptionValue("critical",
                        CheckDatabaseLoadPlugin.CRITICAL_VALUE));

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

            res.add(new Metric(CheckDatabaseLoadPlugin.UID_LOAD,
                    "The UID load is " + snapshot.getLastUIDs() + '.',
                    new BigDecimal(snapshot.getLastUIDs()), null, null));
            res.add(new Metric(CheckDatabaseLoadPlugin.SELECT_LOAD,
                    "The Select load is " + snapshot.getLastSelects(),
                    new BigDecimal(snapshot.getLastSelects() + '.'), null, null));
            res.add(new Metric(CheckDatabaseLoadPlugin.COMMIT_LOAD,
                    "The Commit load is " + snapshot.getLastCommits(),
                    new BigDecimal(snapshot.getLastCommits() + '.'), null, null));

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
        return "Check_Database_Load";
    }

}
