package com.github.angoca.db2jnrpe.plugins.jnrpe;

import it.jnrpe.ICommandLine;
import it.jnrpe.plugins.Metric;
import it.jnrpe.utils.BadThresholdException;
import it.jnrpe.utils.thresholds.ThresholdsEvaluatorBuilder;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import com.github.angoca.db2jnrpe.plugins.db2.DatabaseSnapshot;

/**
 * This plugin allows to calculate the sorting time per transaction.<br/>
 * In order to execute this plugin, it is necessary to have DB2 in at least one
 * of the following version:
 * <ul>
 * <li>At least v9.7 FP1</li>
 * <li>At least v9.8 FP2</li>
 * <li>v10.1 or newer</li>
 * </ul>
 *
 * @author Andres Gomez Casanova (@AngocA)
 * @version 2014-12-19
 */
@SuppressWarnings("PMD.CommentSize")
public final class CheckSortingTimePerTransactionPlugin extends
AbstractSingleMetricDB2Plugin {

    /**
     * Value to consider the physical IO as critical.
     */
    private static final String CRITICAL_VALUE = "20:";

    /**
     * Label for the physical IO per transaction.
     */
    @SuppressWarnings("PMD.LongVariable")
    private static final String SORT_TIME_P_TRX = "Sort_time_per_trx";
    /**
     * Value to consider the physical IO as warning.
     */
    private static final String WARNING_VALUE = "15:";

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
        final ICommandLine cl = new ICommandLine() {

            @Override
            public String getOptionValue(final char shortOptionName) {
                return null;
            }

            @Override
            public String getOptionValue(final char shortOptionName,
                    final String defaultValue) {
                return null;
            }

            @Override
            public String getOptionValue(final String optionName) {
                String ret = null;
                if (optionName.equals("hostname")) {
                    ret = "localhost";
                } else if (optionName.equals("port")) {
                    ret = "50000";
                } else if (optionName.equals("database")) {
                    ret = "sample";
                } else if (optionName.equals("username")) {
                    ret = "db2inst1";
                } else if (optionName.equals("password")) {
                    ret = "db2inst1";
                }
                return ret;
            }

            @Override
            public List<String> getOptionValues(final String optionName) {
                return null;
            }

            @Override
            public String getOptionValue(final String optionName,
                    final String defaultValue) {
                return null;
            }

            @Override
            public List<String> getOptionValues(final char shortOptionName) {
                return null;
            }

            @Override
            public boolean hasOption(final String optionName) {
                return false;
            }

            @Override
            public boolean hasOption(final char shortOptionName) {
                return false;
            }
        };
        final ThresholdsEvaluatorBuilder thrb = new ThresholdsEvaluatorBuilder();
        Collection<Metric> c;
        AbstractDB2PluginBase p;
        p = new CheckSortingTimePerTransactionPlugin();
        p.configureThresholdEvaluatorBuilder(thrb, cl);
        try {
            p.gatherMetrics(cl);
        } catch (final Exception e) {
            System.out.println("First snapshot");
        }
        Thread.sleep(2000);
        try {
            c = p.gatherMetrics(cl);
            System.out.println(c.toString() + ':' + c.size());
        } catch (final Exception e) {
            System.out.println("Second snapshot");
        }
        Thread.sleep(5000);
        c = p.gatherMetrics(cl);
        System.out.println(c.toString() + ':' + c.size());
        Thread.sleep(5000);
        c = p.gatherMetrics(cl);
        System.out.println(c.toString() + ':' + c.size());
        // CHECKSTYLE:ON
    }

    /**
     * Empty constructor.
     */
    public CheckSortingTimePerTransactionPlugin() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.github.angoca.db2jnrpe.plugins.jnrpe.AbstractDB2PluginBase#setThreshold
     * (it.jnrpe.utils.thresholds.ThresholdsEvaluatorBuilder,
     * it.jnrpe.ICommandLine)
     */
    @Override
    void setThreshold(final ThresholdsEvaluatorBuilder thrb,
            final ICommandLine line) throws BadThresholdException {
        thrb.withLegacyThreshold(
                CheckSortingTimePerTransactionPlugin.SORT_TIME_P_TRX, null,
                line.getOptionValue("warning",
                        CheckSortingTimePerTransactionPlugin.WARNING_VALUE),
                line.getOptionValue("critical",
                        CheckSortingTimePerTransactionPlugin.CRITICAL_VALUE));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.github.angoca.db2jnrpe.plugins.jnrpe.AbstractDB2PluginBase#addMetric
     * (java.util.List, com.github.angoca.db2jnrpe.plugins.db2.DatabaseSnapshot)
     */
    @Override
    void addMetric(final List<Metric> res, final DatabaseSnapshot snapshot) {
        final String message = String.format(
                "The sorting time per transaction is %.1f (%d/%d)",
                snapshot.getLastSortTimePerTransaction(),
                snapshot.getLastTotalSortTimeSecs(), snapshot.getLastCommits());
        res.add(new Metric(
                CheckSortingTimePerTransactionPlugin.SORT_TIME_P_TRX, message,
                new BigDecimal(snapshot.getLastSortTimePerTransaction()), null,
                null));
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.jnrpe.plugins.PluginBase#getPluginName()
     */
    @Override
    @SuppressWarnings("PMD.CommentRequired")
    protected String getPluginName() {
        return "Check_Avergare_Sorting_Time";
    }
}
