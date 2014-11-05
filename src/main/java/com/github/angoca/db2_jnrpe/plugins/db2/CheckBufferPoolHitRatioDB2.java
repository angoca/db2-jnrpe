package com.github.angoca.db2_jnrpe.plugins.db2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.github.angoca.db2_jnrpe.database.DatabaseConnection;
import com.github.angoca.db2_jnrpe.database.DatabaseConnectionException;
import com.github.angoca.db2_jnrpe.database.DatabaseConnectionsManager;
import com.github.angoca.db2_jnrpe.database.pools.ConnectionPoolsManager;
import com.github.angoca.db2_jnrpe.database.rdbms.db2.DB2Connection;
import com.github.angoca.db2_jnrpe.database.rdbms.db2.DB2Helper;
import com.github.angoca.db2_jnrpe.database.rdbms.db2.DB2MajorVersions;

/**
 * Connects to the database and retrieve the values about buffer pool hit ratio.
 * 
 * @author Andres Gomez Casanova (@AngocA)
 * @version 2014-11-03
 */
public final class CheckBufferPoolHitRatioDB2 {

    /**
     * Columns of the table.
     */
    public final static String[] keys = { "BP_NAME", "LOGICAL_READS",
            "PHYSICAL_READS", "HIT_RATIO", "MEMBER" };

    /**
     * Query for DB2 after v9.7.
     */
    private final static String queryAfter_v9_7 = "WITH BPMETRICS AS ("
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

    /**
     * Query to retrieve the names of the bufferpools.
     */
    private final static String queryBufferpoolNames = "SELECT BPNAME "
            + "FROM SYSCAT.BUFFERPOOLS";

    /**
     * Returns the list of bufferpools.
     * 
     * @param dbConn
     *            Object that wraps the connection.
     * @return A list with the names of the bufferpools.
     * @throws DatabaseConnectionException
     */
    final static List<String> getBufferpoolNames(final DatabaseConnection dbConn)
            throws DatabaseConnectionException {
        List<String> names = new ArrayList<>();
        Connection connection = null;
        try {
            connection = ConnectionPoolsManager.getInstance()
                    .getConnectionPool(dbConn.getConnectionsPool())
                    .getConnection(dbConn);
            PreparedStatement stmt = connection
                    .prepareStatement(queryBufferpoolNames);
            ResultSet res = stmt.executeQuery();

            while (res.next()) {
                names.add(res.getString(1));
            }
            res.close();
            stmt.close();
            ConnectionPoolsManager.getInstance()
                    .getConnectionPool(dbConn.getConnectionsPool())
                    .closeConnection(dbConn);
        } catch (SQLException sqle) {
            DB2Helper.processException(sqle);
            throw new DatabaseConnectionException(sqle);
        }
        return names;
    }

    /**
     * Checks the bufferpool hit ratio with the given database connection.
     * 
     * @param dbConn
     *            Object that wraps the connection.
     * @return Matrix that describe the results.
     * @throws DatabaseConnectionException
     *             If any problem occur while accessing the database.
     */
    final static Map<String, List<String>> check(final DatabaseConnection dbConn)
            throws DatabaseConnectionException {
        assert dbConn != null;
        Map<String, List<String>> allValues = new HashMap<String, List<String>>();
        DB2MajorVersions version = DB2Helper.getDB2MajorVersion(dbConn);
        // This query cannot be executed in a database with db2 v9.5 or before.
        if (version.isEqualOrMoreRecentThan(DB2MajorVersions.V9_7)) {

            Connection connection = null;
            try {
                connection = ConnectionPoolsManager.getInstance()
                        .getConnectionPool(dbConn.getConnectionsPool())
                        .getConnection(dbConn);
                PreparedStatement stmt = connection
                        .prepareStatement(queryAfter_v9_7);
                ResultSet res = stmt.executeQuery();

                List<String> values;
                String ratio;
                String name;
                while (res.next()) {
                    name = res.getString(1);
                    values = new ArrayList<>();
                    values.add(res.getString(2));
                    values.add(res.getString(3));
                    ratio = res.getString(4);
                    if (ratio == null) {
                        ratio = "100";
                    }
                    values.add(ratio);
                    values.add(res.getString(5));
                    values.add(name);
                    allValues.put(name, values);
                }
                res.close();
                stmt.close();
                ConnectionPoolsManager.getInstance()
                        .getConnectionPool(dbConn.getConnectionsPool())
                        .closeConnection(dbConn);
            } catch (SQLException sqle) {
                DB2Helper.processException(sqle);
                throw new DatabaseConnectionException(sqle);
            }
        }

        assert allValues != null;
        return allValues;
    }

    /**
     * Tester.
     * 
     * @param args
     *            Arguments
     * @throws Exception
     *             Any exception.
     */
    public final static void main(final String[] args) throws Exception {
        System.out.println("Test: Connection with pool");
        final String hostname = "localhost";
        final int portNumber = 50000;
        final String databaseName = "sample";
        final String username = "db2inst1";
        final String password = "db2inst1";

        final String databaseConnection = DB2Connection.class.getName();
        final String connectionPool = com.github.angoca.db2_jnrpe.database.pools.c3p0.DBBroker_c3p0.class
                .getName();
        final DatabaseConnection dbConn = DatabaseConnectionsManager
                .getInstance().getDatabaseConnection(connectionPool,
                        databaseConnection, hostname, portNumber, databaseName,
                        username, password);

        final Map<String, List<String>> bufferpoolsDesc = CheckBufferPoolHitRatioDB2
                .check(dbConn);
        Iterator<String> iter = bufferpoolsDesc.keySet().iterator();
        while (iter.hasNext()) {
            String name = iter.next();
            List<String> bpDesc = bufferpoolsDesc.get(name);
            String message = String.format("Bufferpool %s at member %s has %s "
                    + "logical reads and %s physical reads, with a hit "
                    + "ratio of %s%%.", bpDesc.get(4), bpDesc.get(3),
                    bpDesc.get(0), bpDesc.get(1), bpDesc.get(2));

            System.out.println(message);
        }
    }
}
