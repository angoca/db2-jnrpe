package com.github.angoca.db2jnrpe.plugins.db2.broker;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.angoca.db2jnrpe.database.AbstractDatabaseConnection;
import com.github.angoca.db2jnrpe.database.DatabaseConnectionException;
import com.github.angoca.db2jnrpe.database.DatabaseConnectionsManager;
import com.github.angoca.db2jnrpe.database.pools.ConnectionPoolsManager;
import com.github.angoca.db2jnrpe.database.rdbms.db2.DB2Connection;
import com.github.angoca.db2jnrpe.database.rdbms.db2.DB2Helper;
import com.github.angoca.db2jnrpe.database.rdbms.db2.DB2MajorVersion;
import com.github.angoca.db2jnrpe.plugins.db2.BufferpoolRead;
import com.github.angoca.db2jnrpe.plugins.db2.Bufferpools;
import com.github.angoca.db2jnrpe.plugins.db2.DB2Database;
import com.github.angoca.db2jnrpe.plugins.db2.DB2DatabasesManager;

/**
 * Queries the database to retrieve the information about the bufferpool hit
 * ratio.
 *
 * @author Andres Gomez Casanova (@AngocA)
 * @version 2014-11-03
 */
public final class DB2BufferpoolHitRatioBroker extends AbstractDB2Broker
        implements Runnable {

    /**
     * Position of column bpname.
     */
    private static final int COL_POS_BPNAME = 1;

    /**
     * Position of column logical reads.
     */
    @SuppressWarnings("PMD.LongVariable")
    private static final int COL_POS_LOGICAL_READS = 2;

    /**
     * Position of column for member.
     */
    private static final int COL_POS_MEMBER = 4;

    /**
     * Position of column for total reads.
     */
    @SuppressWarnings("PMD.LongVariable")
    private static final int COL_POS_TOTAL_READS = 3;

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(DB2BufferpoolHitRatioBroker.class);

    /**
     * Query to get the values of the logical and physical reads (for DB2 after
     * v9.7).
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
    @SuppressWarnings("PMD")
    public static void main(final String[] args) throws Exception {
        // CHECKSTYLE:OFF
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
        AbstractDatabaseConnection dbConn;

        hostname = "localhost";
        portNumber = 50000;
        databaseName = "sample";
        username = "db2inst1";
        password = "db2inst1";

        databaseConnection = DB2Connection.class.getName();
        connectionPool = com.github.angoca.db2jnrpe.database.pools.hikari.DbcpHikari.class
                .getName();
        dbConn = DatabaseConnectionsManager.getInstance()
                .getDatabaseConnection(connectionPool, databaseConnection,
                        hostname, portNumber, databaseName, username, password);

        DB2DatabasesManager.getInstance().add(dbConn.getUrl(),
                new DB2Database(dbConn.getUrl()));
        new DB2BufferpoolHitRatioBroker(dbConn, DB2DatabasesManager
                .getInstance().getDatabase(dbConn.getUrl())).check();
        bufferpoolsDesc = DB2DatabasesManager.getInstance()
                .getDatabase(dbConn.getUrl()).getBufferpools()
                .getBufferpoolReads();
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
        connectionPool = com.github.angoca.db2jnrpe.database.pools.hikari.DbcpHikari.class
                .getName();
        dbConn = DatabaseConnectionsManager.getInstance()
                .getDatabaseConnection(connectionPool, databaseConnection,
                        hostname, portNumber, databaseName, username, password);

        DB2DatabasesManager.getInstance().add(dbConn.getUrl(),
                new DB2Database(dbConn.getUrl()));
        new DB2BufferpoolHitRatioBroker(dbConn, DB2DatabasesManager
                .getInstance().getDatabase(dbConn.getUrl())).check();
        bufferpoolsDesc = DB2DatabasesManager.getInstance()
                .getDatabase(dbConn.getUrl()).getBufferpools()
                .getBufferpoolReads();
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
        // CHECKSTYLE:ON
    }

    /**
     * Creates the object associating a connection properties.
     *
     * @param connProps
     *            Connection properties.
     * @param db2database
     *            DB2 database that contains the info.
     */
    public DB2BufferpoolHitRatioBroker(
            final AbstractDatabaseConnection connProps,
            final DB2Database db2database) {
        super();
        this.setDBConnection(connProps);
        this.setDB2database(db2database);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.github.angoca.db2jnrpe.plugins.db2.broker.AbstractDB2Broker#check()
     */
    @Override
    @SuppressWarnings({ "PMD.CommentRequired",
            "PMD.DoNotThrowExceptionInFinally" })
    protected void check() throws DatabaseConnectionException {
        assert this.getDatabaseConnection() != null;
        final DB2MajorVersion version = DB2Helper.getDB2MajorVersion(this
                .getDatabaseConnection());
        // This query cannot be executed in a database with db2 v9.5 or before.
        if (version.isEqualOrMoreRecentThan(DB2MajorVersion.V9_7)) {
            ResultSet res = null;

            Connection connection = null;
            try {
                connection = ConnectionPoolsManager.getInstance()
                        .getConnectionPool(this.getDatabaseConnection())
                        .getConnection(this.getDatabaseConnection());
                final PreparedStatement stmt = connection
                        .prepareStatement(DB2BufferpoolHitRatioBroker.QUERY_AFTER_V97);
                res = stmt.executeQuery();

                BufferpoolRead read;
                String name;
                long logical;
                long physical;
                int member;
                Bufferpools bufferpools;
                final List<String> reads = new ArrayList<String>();
                while (res.next()) {
                    // Name.
                    name = res
                            .getString(DB2BufferpoolHitRatioBroker.COL_POS_BPNAME);
                    // Logical reads.
                    logical = res
                            .getLong(DB2BufferpoolHitRatioBroker.COL_POS_LOGICAL_READS);
                    // Physical reads.
                    physical = res
                            .getLong(DB2BufferpoolHitRatioBroker.COL_POS_TOTAL_READS);
                    // Member
                    member = res
                            .getInt(DB2BufferpoolHitRatioBroker.COL_POS_MEMBER);

                    DB2BufferpoolHitRatioBroker.LOGGER.info(
                            "{}::Name{},logical{},physical{},member{}",
                            new Object[] {
                                    this.getDatabaseConnection().getUrl(),
                                    name, logical, physical, member });
                    bufferpools = this.getDatabase().getBufferpools();
                    if (bufferpools == null) {
                        bufferpools = new Bufferpools(this.getDatabase());
                        this.getDatabase().setBufferpools(bufferpools);
                    }
                    read = bufferpools.getBufferpoolReads().get(name);
                    if (read == null) {
                        if (DB2BufferpoolHitRatioBroker.LOGGER.isDebugEnabled()) {
                            DB2BufferpoolHitRatioBroker.LOGGER.debug(this
                                    .getDatabaseConnection().getUrl()
                                    + "::New bufferpool");
                        }
                        read = new BufferpoolRead(name, logical, logical
                                + physical, member);
                        bufferpools.addBufferpoolRead(read);
                    } else {
                        if (DB2BufferpoolHitRatioBroker.LOGGER.isDebugEnabled()) {
                            DB2BufferpoolHitRatioBroker.LOGGER.debug(this
                                    .getDatabaseConnection().getUrl()
                                    + "::Bufferpool updated");
                        }
                        read.setReads(logical, logical + physical);
                    }
                    reads.add(name);
                }
                // Checks the list of bufferpool read to delete the inexistent
                // reads.
                final ConcurrentMap<String, BufferpoolRead> newBps = new ConcurrentHashMap<String, BufferpoolRead>();
                for (final String bpName : this.getDatabase().getBufferpools()
                        .getBufferpoolReads().keySet()) {
                    if (reads.contains(bpName)) {
                        newBps.put(bpName, this.getDatabase().getBufferpools()
                                .getBufferpoolReads().get(bpName));
                    }
                }
                this.getDatabase().getBufferpools().setBufferpools(newBps);
                this.getDatabase().getBufferpools().updateLastBufferpoolRead();
                res.close();
                stmt.close();
                ConnectionPoolsManager
                        .getInstance()
                        .getConnectionPool(this.getDatabaseConnection())
                        .closeConnection(this.getDatabaseConnection(),
                                connection);
            } catch (final SQLException sqle) {
                DB2Helper.processException(sqle);
                throw new DatabaseConnectionException(sqle);
            } finally {
                try {
                    if (res != null) {
                        res.close();
                    }
                } catch (final SQLException e) {
                    throw new DatabaseConnectionException(e);
                }
                try {
                    if (connection != null) {
                        connection.close();
                    }
                } catch (SQLException e) {
                    throw new DatabaseConnectionException(e);
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    @SuppressWarnings("PMD.CommentRequired")
    public void run() {
        super.setLock();
    }
}
