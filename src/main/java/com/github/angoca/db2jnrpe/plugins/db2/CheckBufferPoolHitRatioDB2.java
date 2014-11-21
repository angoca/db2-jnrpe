package com.github.angoca.db2jnrpe.plugins.db2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
     * Position of column bpname.
     */
    private static final int COL_POS_BPNAME = 1;

    /**
     * Position of column logical reads.
     */
    private static final int COL_POS_LOGICAL_READS = 2;

    /**
     * Position of column for member.
     */
    private static final int COL_POS_MEMBER = 4;

    /**
     * Position of column for total reads.
     */
    private static final int COL_POS_TOTAL_READS = 3;

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory
            .getLogger(CheckBufferPoolHitRatioDB2.class);

    /**
     * Columns of the table.
     */
    public static final String[] KEYS = { "BP_NAME", "LOGICAL_READS",
            "PHYSICAL_READS", "HIT_RATIO", "MEMBER" };

    /**
     * Prevent multiple concurrent executions.
     */
    private static final Map<String, Integer> LOCKS = new HashMap<String, Integer>();

    /**
     * Query for DB2 after v9.7.
     */
    private static final String QUERY_AFTER_V97 = "SELECT BP_NAME, "
            + " POOL_DATA_L_READS + POOL_TEMP_DATA_L_READS + "
            + " POOL_XDA_L_READS + POOL_TEMP_XDA_L_READS + "
            + " POOL_INDEX_L_READS + POOL_TEMP_INDEX_L_READS "
            + " AS LOGICAL_READS, "
            + " POOL_DATA_P_READS + POOL_TEMP_DATA_P_READS + "
            + " POOL_INDEX_P_READS + POOL_TEMP_INDEX_P_READS + "
            + " POOL_XDA_P_READS + POOL_TEMP_XDA_P_READS "
            + " AS PHYSICAL_READS, MEMBER "
            + " FROM TABLE(MON_GET_BUFFERPOOL('', -2)) AS METRICS "
            + " WHERE BP_NAME NOT LIKE 'IBMSYSTEMBP%'";

    /**
     * Tester.
     *
     * @param args
     *            Arguments
     * @throws Exception
     *             Any exception.
     */
    public static void main(final String[] args) throws Exception {
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
        new CheckBufferPoolHitRatioDB2(dbConn, DB2DatabasesManager
                .getInstance().getDatabase(dbConn.getUrl())).check();
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

        DB2DatabasesManager.getInstance().add(dbConn.getUrl(),
                new DB2Database(dbConn.getUrl()));
        new CheckBufferPoolHitRatioDB2(dbConn, DB2DatabasesManager
                .getInstance().getDatabase(dbConn.getUrl())).check();
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
     * @param connProps
     *            Connection properties.
     * @param db2database
     *            DB2 database that contains the info.
     */
    CheckBufferPoolHitRatioDB2(final DatabaseConnection connProps,
            final DB2Database db2database) {
        this.dbConn = connProps;
        this.db2db = db2database;
    }

    /**
     * Checks the bufferpool hit ratio with the given database connection.
     *
     * @throws DatabaseConnectionException
     *             If any problem occur while accessing the database.
     */
    void check() throws DatabaseConnectionException {
        assert this.dbConn != null;
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
                        .prepareStatement(CheckBufferPoolHitRatioDB2.QUERY_AFTER_V97);
                final ResultSet res = stmt.executeQuery();

                BufferpoolRead read;
                String name;
                long logical;
                long physical;
                int member;
                DB2Database db = DB2DatabasesManager.getInstance().getDatabase(
                        this.db2db.getId());
                Map<String, BufferpoolRead> bps = db.getBufferpools();
                final List<String> reads = new ArrayList<String>();
                while (res.next()) {
                    // Name.
                    name = res
                            .getString(CheckBufferPoolHitRatioDB2.COL_POS_BPNAME);
                    // Logical reads.
                    logical = res
                            .getLong(CheckBufferPoolHitRatioDB2.COL_POS_LOGICAL_READS);
                    // Physical reads.
                    physical = res
                            .getLong(CheckBufferPoolHitRatioDB2.COL_POS_TOTAL_READS);
                    // Member
                    member = res
                            .getInt(CheckBufferPoolHitRatioDB2.COL_POS_MEMBER);

                    log.info(this.dbConn.getUrl() + "::Name " + name
                            + ", logical " + logical + ", physical " + physical
                            + ", member " + member);
                    read = bps.get(name);
                    if (read == null) {
                        log.debug(this.dbConn.getUrl() + "::New bufferpool");
                        read = new BufferpoolRead(name, logical, logical
                                + physical, member);
                        db.addBufferpoolRead(read);
                    } else {
                        log.debug(this.dbConn.getUrl() + "::Bufferpool updated");
                        read.setReads(logical, logical + physical);
                    }
                    reads.add(name);
                }
                // Checks the list of bufferpool read to delete the inexistent
                // reads.
                for (String bpName : db.getBufferpools().keySet()) {
                    if (!reads.contains(bpName)) {
                        db.getBufferpools().remove(bpName);
                    }
                }
                db.updateLastBufferpoolRead();
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
            if (!CheckBufferPoolHitRatioDB2.LOCKS.containsKey(this.db2db
                    .getId())) {
                CheckBufferPoolHitRatioDB2.LOCKS.put(this.db2db.getId(), 1);
                this.check();
                CheckBufferPoolHitRatioDB2.LOCKS.remove(this.db2db.getId());
            } else {
                log.warn(this.dbConn.getUrl() + "::There is a lock for: "
                        + this.db2db.getId());
            }
        } catch (final Exception e) {
            log.error(this.dbConn.getUrl()
                    + "::Error while reading bufferpool values", e);
            CheckBufferPoolHitRatioDB2.LOCKS.remove(this.db2db.getId());
            e.printStackTrace();
        }
    }
}
