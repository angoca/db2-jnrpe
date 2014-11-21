package com.github.angoca.db2jnrpe.plugins.jnrpe;

import it.jnrpe.ICommandLine;
import it.jnrpe.plugins.Metric;
import it.jnrpe.plugins.MetricGatheringException;
import it.jnrpe.utils.BadThresholdException;
import it.jnrpe.utils.thresholds.ThresholdsEvaluatorBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.github.angoca.db2jnrpe.plugins.db2.DB2Database;
import com.github.angoca.db2jnrpe.plugins.db2.DB2DatabasesManager;
import com.github.angoca.db2jnrpe.plugins.db2.UnknownValueException;

/**
 * This is the implementation of the plugin that allows to monitor the load of
 * the database.
 *
 * @author Andres Gomez Casanova (@AngocA)
 * @version 2014-11-21
 */
public final class CheckDatabaseLoadPlugin extends DB2PluginBase {

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
        thrb.withLegacyThreshold(DB2Database.UID_LOAD, null,
                cl.getOptionValue("warning", "80"),
                cl.getOptionValue("critical", "100"));
        thrb.withLegacyThreshold(DB2Database.SELECT_LOAD, null,
                cl.getOptionValue("warning", "80"),
                cl.getOptionValue("critical", "100"));
        thrb.withLegacyThreshold(DB2Database.COMMIT_LOAD, null,
                cl.getOptionValue("warning", "80"),
                cl.getOptionValue("critical", "100"));

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
        Map<String, Long> loadValues;
        try {
            loadValues = db2Database.getLoadValuesAndRefresh(this
                    .getConnection(cl));

            res.add(new Metric(DB2Database.UID_LOAD, "The UID load is "
                    + loadValues.get(DB2Database.UID_LOAD), new BigDecimal(
                    loadValues.get(DB2Database.UID_LOAD)), null, null));
            res.add(new Metric(DB2Database.SELECT_LOAD, "The Select load is "
                    + loadValues.get(DB2Database.SELECT_LOAD), new BigDecimal(
                    loadValues.get(DB2Database.SELECT_LOAD)), null, null));
            res.add(new Metric(DB2Database.COMMIT_LOAD, "The Commit load is "
                    + loadValues.get(DB2Database.COMMIT_LOAD), new BigDecimal(
                    loadValues.get(DB2Database.COMMIT_LOAD)), null, null));

        } catch (UnknownValueException e) {
            // There are not values in the cache. Do nothing.
        }

        // Metadata
        final boolean metadata = cl.hasOption("metadata");
        if (metadata) {
            res.add(new Metric("Cache-data", "", new BigDecimal(db2Database
                    .getLastBufferpoolRefresh()), null, null));
            res.add(new Metric("Cache-old", "", new BigDecimal(System
                    .currentTimeMillis()
                    - db2Database.getLastBufferpoolRefresh()), null, null));
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
