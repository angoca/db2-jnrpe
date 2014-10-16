package com.github.angoca.db2_jnrpe.plugins.db2;

import it.jnrpe.ICommandLine;
import it.jnrpe.plugins.Metric;
import it.jnrpe.plugins.MetricGatheringException;
import it.jnrpe.plugins.PluginBase;
import it.jnrpe.utils.BadThresholdException;
import it.jnrpe.utils.thresholds.ThresholdsEvaluatorBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.github.angoca.db2_jnrpe.database.DatabaseConnection;
import com.github.angoca.db2_jnrpe.database.DatabaseConnectionsManager;
import com.github.angoca.db2_jnrpe.database.pools.c3p0.DBBroker_c3p0;
import com.github.angoca.db2_jnrpe.database.rdbms.db2.DB2Connection;

public class CheckBufferPoolHitRatioJnrpe extends PluginBase {

    @Override
    public void configureThresholdEvaluatorBuilder(
            ThresholdsEvaluatorBuilder thrb, ICommandLine cl)
            throws BadThresholdException {
        // TODO check all bufferpools
        // TODO default value
        thrb.withLegacyThreshold("bufferpool-hit-ratio_", null,
                cl.getOptionValue("warning"), cl.getOptionValue("critical"));
    }

    @Override
    public final Collection<Metric> gatherMetrics(final ICommandLine cl)
            throws MetricGatheringException {
        String hostname = cl.getOptionValue("hostname");
        int portNumber = Integer.valueOf(cl.getOptionValue("portNumber"));
        String databaseName = cl.getOptionValue("databaseName");
        String username = cl.getOptionValue("username");
        String password = cl.getOptionValue("password");

        String databaseConnection = DB2Connection.class.getName();
        String connectionPool = DBBroker_c3p0.class.getName();
        DatabaseConnection dbConn = DatabaseConnectionsManager.getInstance()
                .getDatabaseConnection(connectionPool, databaseConnection,
                        hostname, portNumber, databaseName, username, password);
        String[][] values = CheckBufferPoolHitRatioDB2.check(dbConn);

        List<Metric> res = new ArrayList<Metric>();
        for (int i = 0; i < values.length; i++) {
            String name = "bufferpool-hit-ratio_" + values[i][0];
            String message = String.format("Bufferpool %s at member %s has %s "
                    + "logical reads and %s physical reads, with a hit "
                    + "ratio of %s%%.", values[i][0], values[i][4],
                    values[i][1], values[0][2], values[i][3]);
            BigDecimal value = new BigDecimal(values[i][3]);
            BigDecimal min = new BigDecimal(0);
            BigDecimal max = new BigDecimal(100);

            res.add(new Metric(name, message, value, min, max));
        }

        return res;
    }

    @Override
    protected String getPluginName() {
        return "CHECK_BUFFER_POOL_HIT_RATIO";
    }
}
