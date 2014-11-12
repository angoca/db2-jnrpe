package com.github.angoca.db2jnrpe.plugins.db2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.github.angoca.db2jnrpe.database.DatabaseConnection;
import com.github.angoca.db2jnrpe.database.DatabaseConnectionException;
import com.github.angoca.db2jnrpe.database.DatabaseConnectionsManager;
import com.github.angoca.db2jnrpe.database.pools.ConnectionPoolsManager;
import com.github.angoca.db2jnrpe.database.rdbms.db2.DB2Connection;
import com.github.angoca.db2jnrpe.database.rdbms.db2.DB2Helper;
import com.github.angoca.db2jnrpe.database.rdbms.db2.DB2MajorVersions;

/**
 * Connects to the database and retrieve the values about buffer pool hit ratio.
 *
 * @author Andres Gomez Casanova (@AngocA)
 * @version 2014-11-03
 */
public final class CheckBufferPoolHitRatioDB2 implements Runnable {

    /**
     * Columns of the table.
     */
    public static final String[] keys = { "BP_NAME", "LOGICAL_READS",
            "PHYSICAL_READS", "HIT_RATIO", "MEMBER" };

    /**
     * Prevent multiple concurrent executions.
     */
    private static final Map<String, Integer> locks = new HashMap<String, Integer>();

    /**
     * Query for DB2 after v9.7.
     */
    private static final String queryAfter_v9_7 = "WITH BPMETRICS AS ("
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
     * Tester.
     *
     * @param args
     *            Arguments
     * @throws Exception
     *             Any exception.
     */
    public static final void main(final String[] args) throws Exception {
        System.out.println("Test: Connection with pool");
        String hostname;
        int portNumber;
        String databaseName;
        String username;
        String password;

        Map<String, BufferpoolRead> bufferpoolsDesc;
        Iterator<String> iter;
        String databaseConnection;
        String connectionPool;
        DatabaseConnection dbConn;

        hostname = "localhost";
        portNumber = 50000;
        databaseName = "sample";
        username = "db2inst1";
        password = "db2inst1";

        databaseConnection = DB2Connection.class.getName();
        connectionPool = com.github.angoca.db2jnrpe.database.pools.c3p0.DbcpC3p0.class
                .getName();
        dbConn = DatabaseConnectionsManager.getInstance()
                .getDatabaseConnection(connectionPool, databaseConnection,
                        hostname, portNumber, databaseName, username, password);

        DB2DatabasesManager.getInstance().add(dbConn.getUrl(),
                new DB2Database(dbConn.getUrl()));
        new CheckBufferPoolHitRatioDB2(dbConn, new DB2Database("1")).check();
        bufferpoolsDesc = DB2DatabasesManager.getInstance()
                .getDatabase(dbConn.getUrl()).getBufferpools();
        iter = bufferpoolsDesc.keySet().iterator();
        while (iter.hasNext()) {
            final String name = iter.next();
            final BufferpoolRead bpDesc = bufferpoolsDesc.get(name);
            final String message = String.format(
                    "Bufferpool %s at member %s has %s "
                            + "logical reads and %s total reads, with a hit "
                            + "ratio of %s%%.", bpDesc.getName(),
                    bpDesc.getMember(), bpDesc.getLogicalReads(),
                    bpDesc.getTotalReads(), bpDesc.getLastRatio());

            System.out.println(message);
        }
        Thread.sleep(2000);

        hostname = "127.0.0.1";
        portNumber = 50002;
        databaseName = "sample";
        username = "db2inst2";
        password = "db2inst2";

        databaseConnection = DB2Connection.class.getName();
        connectionPool = com.github.angoca.db2jnrpe.database.pools.c3p0.DbcpC3p0.class
                .getName();
        dbConn = DatabaseConnectionsManager.getInstance()
                .getDatabaseConnection(connectionPool, databaseConnection,
                        hostname, portNumber, databaseName, username, password);

        new CheckBufferPoolHitRatioDB2(dbConn, new DB2Database("2")).check();
        bufferpoolsDesc = DB2DatabasesManager.getInstance()
                .getDatabase(dbConn.getUrl()).getBufferpools();
        iter = bufferpoolsDesc.keySet().iterator();
        while (iter.hasNext()) {
            final String name = iter.next();
            final BufferpoolRead bpDesc = bufferpoolsDesc.get(name);
            final String message = String.format(
                    "Bufferpool %s at member %s has %s "
                            + "logical reads and %s total reads, with a hit "
                            + "ratio of %s%%.", bpDesc.getName(),
                    bpDesc.getMember(), bpDesc.getLogicalReads(),
                    bpDesc.getTotalReads(), bpDesc.getRatio());

            System.out.println(message);
        }
    }

    /**
     * DB2 database.
     */
    private final DB2Database db2db;

    /**
     * Connection properties.
     */
    private final DatabaseConnection dbConn;

    /**
     * Creates the object associating a connection properties.
     *
     * @param dbConn
     *            Connection properties.
     */
    public CheckBufferPoolHitRatioDB2(final DatabaseConnection dbConn,
            final DB2Database db2db) {
        this.dbConn = dbConn;
        this.db2db = db2db;
    }

    /**
     * Checks the bufferpool hit ratio with the given database connection.
     *
     * @return List of bufferpool reads.
     * @throws DatabaseConnectionException
     *             If any problem occur while accessing the database.
     */
    final void check() throws DatabaseConnectionException {
        assert this.dbConn != null;
        final Map<String, BufferpoolRead> allValues = new HashMap<String, BufferpoolRead>();
        final DB2MajorVersions version = DB2Helper
                .getDB2MajorVersion(this.dbConn);
        // This query cannot be executed in a database with db2 v9.5 or before.
        if (version.isEqualOrMoreRecentThan(DB2MajorVersions.V9_7)) {

            Connection connection = null;
            try {
                connection = ConnectionPoolsManager.getInstance()
                        .getConnectionPool(this.dbConn)
                        .getConnection(this.dbConn);
                final PreparedStatement stmt = connection
                        .prepareStatement(CheckBufferPoolHitRatioDB2.queryAfter_v9_7);
                final ResultSet res = stmt.executeQuery();

                BufferpoolRead read;
                String name;
                int logical;
                int physical;
                int member;
                while (res.next()) {
                    // Name.
                    name = res.getString(1);
                    // Logical reads.
                    logical = res.getInt(2);
                    // Physical reads.
                    physical = res.getInt(3);
                    // Member
                    member = res.getInt(4);

                    read = new BufferpoolRead(name, logical,
                            logical + physical, member);
                    allValues.put(name, read);
                }
                res.close();
                stmt.close();
                ConnectionPoolsManager.getInstance()
                        .getConnectionPool(this.dbConn)
                        .closeConnection(this.dbConn, connection);
            } catch (final SQLException sqle) {
                DB2Helper.processException(sqle);
                throw new DatabaseConnectionException(sqle);
            }
        }

        DB2DatabasesManager.getInstance().getDatabase(this.db2db.getId())
                .setBufferpoolReads(allValues);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        try {
            // Controls multiple concurrent executions.
            // This prevents to create multiple threads trying to access the
            // database. This is a problem when the database is not available,
            // or it has a big workload, and multiple connections are
            // established.
            if (!CheckBufferPoolHitRatioDB2.locks.containsKey(this.db2db
                    .getId())) {
                CheckBufferPoolHitRatioDB2.locks.put(this.db2db.getId(), 1);
                this.check();
                CheckBufferPoolHitRatioDB2.locks.remove(this.db2db.getId());
            }
        } catch (final DatabaseConnectionException e) {
            e.printStackTrace();
        }
    }
}
