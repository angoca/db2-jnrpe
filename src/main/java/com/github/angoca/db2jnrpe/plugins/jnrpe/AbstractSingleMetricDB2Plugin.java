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
 * This class contains the common methods for all DB2 plugins that returns a
 * single metric.
 *
 * @author Andres Gomez Casanova (@AngocA)
 * @version 2014-12-22
 */
public abstract class AbstractSingleMetricDB2Plugin extends
        AbstractDB2PluginBase {

    /*
     * (non-Javadoc)
     * 
     * @see
     * it.jnrpe.plugins.PluginBase#configureThresholdEvaluatorBuilder(it.jnrpe
     * .utils.thresholds.ThresholdsEvaluatorBuilder, it.jnrpe.ICommandLine)
     */
    @Override
    @SuppressWarnings("PMD.CommentRequired")
    public final void configureThresholdEvaluatorBuilder(
            final ThresholdsEvaluatorBuilder thrb, final ICommandLine line)
            throws BadThresholdException {
        final String dbId = AbstractDB2PluginBase.getId(line);
        this.log.warn("Database: " + dbId);
        setThreshold(thrb, line);

        // Metadata
        final boolean metadata = line.hasOption("metadata");
        if (metadata) {
            thrb.withLegacyThreshold("Cache-data", null, null, null);
            thrb.withLegacyThreshold("Cache-old", null, null, null);
        }
    }

    /**
     * Sets the thresholds for warning and critical.
     * 
     * @param thrb
     *            Object that contains the thresholds.
     * @param line
     *            Command line that contain the values.
     * @throws BadThresholdException
     *             If the exception is invalid.
     */
    abstract void setThreshold(ThresholdsEvaluatorBuilder thrb,
            ICommandLine line) throws BadThresholdException;

    /*
     * (non-Javadoc)
     * 
     * @see it.jnrpe.plugins.PluginBase#gatherMetrics(it.jnrpe.ICommandLine)
     */
    @Override
    @SuppressWarnings("PMD.CommentRequired")
    public final Collection<Metric> gatherMetrics(final ICommandLine line)
            throws MetricGatheringException {
        final List<Metric> res = new ArrayList<Metric>();

        final String identification = AbstractDB2PluginBase.getId(line);
        DB2Database db2Database = DB2DatabasesManager.getInstance()
                .getDatabase(identification);
        if (db2Database == null) {
            db2Database = new DB2Database(identification);
            DB2DatabasesManager.getInstance().add(identification, db2Database);
        }
        DatabaseSnapshot snapshot;
        try {
            snapshot = db2Database.getSnapshotAndRefresh(this
                    .getConnection(line));

            addMetric(res, snapshot);
        } catch (final UnknownValueException e) {
            this.log.warn(identification + "::No values");
            throw new MetricGatheringException(
                    "Not enough values have been gathered: " + e.getMessage(),
                    Status.UNKNOWN, e);
        }

        // Metadata
        final boolean metadata = line.hasOption("metadata");
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

    /**
     * Sets the gathered values in the metric.
     * 
     * @param res
     *            List of metrics to return.
     * @param snapshot
     *            Snapshot of the database.
     */
    abstract void addMetric(List<Metric> res, DatabaseSnapshot snapshot);

}
