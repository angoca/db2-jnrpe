package com.github.angoca.db2_jnrpe.plugins.db2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.github.angoca.db2_jnrpe.database.DatabaseConnection;
import com.github.angoca.db2_jnrpe.database.DatabaseConnectionsManager;
import com.github.angoca.db2_jnrpe.database.pools.DBBrokersManager;
import com.github.angoca.db2_jnrpe.database.pools.c3p0.DBBroker_c3p0;
import com.github.angoca.db2_jnrpe.database.rdbms.db2.DB2Connection;
import com.github.angoca.db2_jnrpe.database.rdbms.db2.DB2Helper;
import com.github.angoca.db2_jnrpe.database.rdbms.db2.DB2Versions;

public class CheckBufferPoolHitRatioDB2 {

    public final static String[] keys = { "BP_NAME", "LOGICAL_READS",
            "PHYSICAL_READS", "HIT_RATIO", "MEMBER" };

    private static String queryAfter_v9_7 = "WITH BPMETRICS AS ("
            + " SELECT BP_NAME, "
            + " POOL_DATA_L_READS + POOL_TEMP_DATA_L_READS + "
            + " POOL_XDA_L_READS + POOL_TEMP_XDA_L_READS + "
            + " POOL_INDEX_L_READS + POOL_TEMP_INDEX_L_READS "
            + " AS LOGICAL_READS, "
            + " POOL_DATA_P_READS + POOL_TEMP_DATA_P_READS + "
            + " POOL_INDEX_P_READS + POOL_TEMP_INDEX_P_READS + "
            + " POOL_XDA_P_READS + POOL_TEMP_XDA_P_READS "
            + " AS PHYSICAL_READS, MEMBER "
            + " FROM TABLE(MON_GET_BUFFERPOOL('', -2)) AS METRICS "
            + " WHERE BP_NAME NOT LIKE 'IBMSYSTEMBP%') "
            + "SELECT BP_NAME, LOGICAL_READS, PHYSICAL_READS, "
            + "CASE WHEN LOGICAL_READS > 0 "
            + " THEN DEC((1 - (FLOAT(PHYSICAL_READS) "
            + " / FLOAT(LOGICAL_READS))) * 100, 5, 2) ELSE NULL "
            + "END AS HIT_RATIO, MEMBER FROM BPMETRICS";

    public static String[][] check(final DatabaseConnection dbConn) {
        List<List<String>> allValues = new ArrayList<List<String>>();
        DB2Versions version = DB2Versions.getDB2Version(dbConn);
        if (version.isEqualOrMoreRecentThan(DB2Versions.V9_7)) {

            Connection connection = null;
            try {
                connection = DBBrokersManager.getInstance()
                        .getBroker(dbConn.getConnectionsPool())
                        .getConnection(dbConn);
                PreparedStatement stmt = connection
                        .prepareStatement(queryAfter_v9_7);
                ResultSet res = stmt.executeQuery();

                List<String> values;
                String ratio;
                while (res.next()) {
                    values = new ArrayList<>();
                    values.add(res.getString(1));
                    values.add(res.getString(2));
                    values.add(res.getString(3));
                    ratio = res.getString(4);
                    if (ratio == null) {
                        ratio = "100";
                    }
                    values.add(ratio);
                    values.add(res.getString(5));
                    allValues.add(values);
                }
                res.close();
                stmt.close();
                DBBroker_c3p0.getInstance().closeConnection(dbConn);
            } catch (SQLException sqle) {
                DB2Helper.processException(sqle);
                // TODO throw an exception to show that the execution failed
            }
        }
        String[][] retValues = convertArrays(allValues);
        return retValues;
    }

    private static String[][] convertArrays(final List<List<String>> allValues) {
        int sizeY = allValues.size();
        String[][] retValues = new String[sizeY][];
        for (int i = 0; i < sizeY; i++) {
            ArrayList<String> row = (ArrayList<String>) allValues.get(i);
            retValues[i] = row.toArray(new String[row.size()]);
        }
        return retValues;
    }

    public static void main(final String[] args) throws SQLException {
        final String hostname = "localhost";
        final int portNumber = 50000;
        final String databaseName = "sample";
        final String username = "db2inst1";
        final String password = "db2inst1";

        final String databaseConnection = DB2Connection.class.getName();
        final String connectionPool = DBBroker_c3p0.class.getName();
        final DatabaseConnection dbConn = DatabaseConnectionsManager
                .getInstance().getDatabaseConnection(connectionPool,
                        databaseConnection, hostname, portNumber, databaseName,
                        username, password);

        final String[][] values = CheckBufferPoolHitRatioDB2.check(dbConn);

        for (int i = 0; i < values.length; i++) {
            String message = String.format("Bufferpool %s at member %s has %s "
                    + "logical reads and %s physical reads, with a hit "
                    + "ratio of %s%%.", values[i][0], values[i][4],
                    values[i][1], values[0][2], values[i][3]);

            System.out.println(message);
        }
    }
}
