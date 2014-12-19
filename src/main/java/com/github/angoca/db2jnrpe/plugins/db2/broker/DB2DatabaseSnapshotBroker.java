package com.github.angoca.db2jnrpe.plugins.db2.broker;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.angoca.db2jnrpe.database.AbstractDatabaseConnection;
import com.github.angoca.db2jnrpe.database.DatabaseConnectionException;
import com.github.angoca.db2jnrpe.database.DatabaseConnectionsManager;
import com.github.angoca.db2jnrpe.database.pools.ConnectionPoolsManager;
import com.github.angoca.db2jnrpe.database.rdbms.db2.DB2Connection;
import com.github.angoca.db2jnrpe.database.rdbms.db2.DB2Helper;
import com.github.angoca.db2jnrpe.database.rdbms.db2.DB2MajorVersion;
import com.github.angoca.db2jnrpe.database.rdbms.db2.DB2MinorVersion;
import com.github.angoca.db2jnrpe.plugins.db2.DB2Database;
import com.github.angoca.db2jnrpe.plugins.db2.DB2DatabasesManager;
import com.github.angoca.db2jnrpe.plugins.db2.DatabaseSnapshot;
import com.github.angoca.db2jnrpe.plugins.db2.IncopatibleDB2VersionException;

/**
 * Queries the database to retrieve the snapshot. The check verifies the
 * database version. Because this uses the sysproc.snpa_db administrative view,
 * the DB2 version should be at least in v9.7 FP1 or v9.8 FP2 or any other
 * recent version.
 *
 * @author Andres Gomez Casanova (@AngocA)
 * @version 2014-11-24
 */
@SuppressWarnings("PMD.CommentSize")
public final class DB2DatabaseSnapshotBroker extends AbstractDB2Broker
        implements Runnable {

    /**
     * Position of column commit quantity.
     */
    @SuppressWarnings({ "PMD.LongVariable", "PMD.AvoidDuplicateLiterals" })
    private static final int C_COMMIT_SQL_STMTS = 2;
    /**
     * Position of column database partition.
     */
    private static final int C_DBPARTITIONNUM = 1;
    /**
     * Position of column bufferpool data physical reads.
     */
    @SuppressWarnings("PMD.LongVariable")
    private static final int C_POOL_DATA_P_READS = 5;
    /**
     * Position of column bufferpool index physical reads.
     */
    @SuppressWarnings("PMD.LongVariable")
    private static final int C_POOL_INDEX_P_READS = 6;
    /**
     * Position of column bufferpool temp physical reads.
     */
    @SuppressWarnings("PMD.LongVariable")
    private static final int C_POOL_TEMP_DATA_P_READS = 7;
    /**
     * Position of column bufferpool temp index physical reads.
     */
    @SuppressWarnings("PMD.LongVariable")
    private static final int C_POOL_TEMP_INDEX_P_READS = 8;
    /**
     * Position of column select quantity.
     */
    @SuppressWarnings("PMD.LongVariable")
    private static final int C_SELECT_SQL_STMTS = 3;
    /**
     * Position of column modifications quantity.
     */
    private static final int C_UID_SQL_STMTS = 4;
    /**
     * Position of column total sort time.
     */
    @SuppressWarnings("PMD.LongVariable")
    private static final int C_TOT_SORT_TIME = 9;
    /**
     * Position of column total sorts.
     */
    @SuppressWarnings("PMD.LongVariable")
    private static final int C_TOTAL_SORTS = 10;

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(DB2DatabaseSnapshotBroker.class);

    /**
     * Query to get the values of the database charge.
     */
    private static final String QUERY = "SELECT DBPARTITIONNUM, "
            + "COMMIT_SQL_STMTS, SELECT_SQL_STMTS, UID_SQL_STMTS, "
            + "POOL_DATA_P_READS, POOL_INDEX_P_READS, "
            + "POOL_TEMP_DATA_P_READS, POOL_TEMP_INDEX_P_READS, "
            + "TOTAL_SORT_TIME, TOTAL_SORTS "
            + "FROM SYSIBMADM.SNAPDB";

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

        DatabaseSnapshot snap;
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
        new DB2DatabaseSnapshotBroker(dbConn, DB2DatabasesManager.getInstance()
                .getDatabase(dbConn.getUrl())).check();
        new DB2DatabaseSnapshotBroker(dbConn, DB2DatabasesManager.getInstance()
                .getDatabase(dbConn.getUrl())).check();
        snap = DB2DatabasesManager.getInstance().getDatabase(dbConn.getUrl())
                .getSnap();
        System.out.println("UIDs:" + snap.getLastUIDRate() + ",Selects:"
                + snap.getLastSelectRate() + ",Commits:"
                + snap.getLastCommitRate());
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
        new DB2BufferpoolHitRatioBroker(dbConn, DB2DatabasesManager
                .getInstance().getDatabase(dbConn.getUrl())).check();
        snap = DB2DatabasesManager.getInstance().getDatabase(dbConn.getUrl())
                .getSnap();
        System.out.println("UIDs:" + snap.getLastUIDRate() + ",Selects:"
                + snap.getLastSelectRate() + ",Commits:"
                + snap.getLastCommitRate());
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
    public DB2DatabaseSnapshotBroker(
            final AbstractDatabaseConnection connProps,
            final DB2Database db2database) {
        super();
        this.setDBConnection(connProps);
        this.setDB2database(db2database);
    }

    /**
     * Assign the read values to an existent object, or it creates a new one.
     *
     * @param res
     *            Result set.
     * @throws SQLException
     *             If any problem ocurrs while reading the info.
     */
    private void assignValues(final ResultSet res) throws SQLException {
        int dbpartitionnum;
        long commitSQLstmts;
        long selectSQLstmts;
        long uidSQLstmts;
        long bpdata;
        long bpindex;
        long bptempdata;
        long bptempindex;
        long totalsorttime;
        long totalsorts;
        DatabaseSnapshot snap;
        while (res.next()) {
            // Partition.
            dbpartitionnum = res
                    .getInt(DB2DatabaseSnapshotBroker.C_DBPARTITIONNUM);
            // Quantity of commits.
            commitSQLstmts = res
                    .getLong(DB2DatabaseSnapshotBroker.C_COMMIT_SQL_STMTS);
            // Quantity of sql.
            selectSQLstmts = res
                    .getLong(DB2DatabaseSnapshotBroker.C_SELECT_SQL_STMTS);
            // Quantity of modifications.
            uidSQLstmts = res
                    .getLong(DB2DatabaseSnapshotBroker.C_UID_SQL_STMTS);
            // Quantity of bufferpool physical reads for data.
            bpdata = res.getLong(DB2DatabaseSnapshotBroker.C_POOL_DATA_P_READS);
            // Quantity of bufferpool physical reads for index.
            bpindex = res
                    .getLong(DB2DatabaseSnapshotBroker.C_POOL_INDEX_P_READS);
            // Quantity of bufferpool physical reads for temporal data.
            bptempdata = res
                    .getLong(DB2DatabaseSnapshotBroker.C_POOL_TEMP_DATA_P_READS);
            // Quantity of bufferpool physical reads for temporal index.
            bptempindex = res
                    .getLong(DB2DatabaseSnapshotBroker.C_POOL_TEMP_INDEX_P_READS);
            // Total sort time
            totalsorttime = res
                    .getLong(DB2DatabaseSnapshotBroker.C_TOT_SORT_TIME);
            // Total sorts 
            totalsorts = res
                    .getLong(DB2DatabaseSnapshotBroker.C_TOTAL_SORTS);

            DB2DatabaseSnapshotBroker.LOGGER.info(
                    "{}::Part{},commit{},select{},uid{}", new Object[] {
                            this.getDatabaseConnection().getUrl(),
                            dbpartitionnum, commitSQLstmts, +selectSQLstmts,
                            uidSQLstmts });
            snap = this.getDatabase().getSnap();
            if (snap == null) {
                if (DB2DatabaseSnapshotBroker.LOGGER.isDebugEnabled()) {
                    DB2DatabaseSnapshotBroker.LOGGER.debug(this
                            .getDatabaseConnection().getUrl()
                            + "::Creating snap");
                }
                snap = new DatabaseSnapshot(this.getDatabase(), dbpartitionnum,
                        commitSQLstmts, selectSQLstmts, uidSQLstmts, bpdata,
                        bpindex, bptempdata, bptempindex);
                this.getDatabase().setSnap(snap);
            } else {
                if (DB2DatabaseSnapshotBroker.LOGGER.isDebugEnabled()) {
                    DB2DatabaseSnapshotBroker.LOGGER.debug(this
                            .getDatabaseConnection().getUrl()
                            + "::Snap updated");
                }
                snap.setValues(dbpartitionnum, commitSQLstmts, selectSQLstmts,
                        uidSQLstmts, bpdata, bpindex, bptempdata, bptempindex);
            }
            snap.setTotalSortTime(totalsorttime);
            snap.setTotalSorts(totalsorts);
        }
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
        final DB2MajorVersion majorVersion = DB2Helper.getDB2MajorVersion(this
                .getDatabaseConnection());
        final DB2MinorVersion minorVersion = DB2Helper.getDB2MinorVersion(this
                .getDatabaseConnection());
        // This query cannot be executed in a database with db2 v9.5 or before.
        if (majorVersion.isEqualOrMoreRecentThan(DB2MajorVersion.V10_1)
                || majorVersion.isEqualThan(DB2MajorVersion.V9_7)
                && minorVersion.isEqualOrMoreRecentThan(DB2MinorVersion.V9_7_1)
                || majorVersion.isEqualThan(DB2MajorVersion.V9_8)
                && minorVersion.isEqualOrMoreRecentThan(DB2MinorVersion.V9_8_2)) {

            Connection connection = null;
            ResultSet res = null;
            try {
                connection = ConnectionPoolsManager.getInstance()
                        .getConnectionPool(this.getDatabaseConnection())
                        .getConnection(this.getDatabaseConnection());
                final PreparedStatement stmt = connection
                        .prepareStatement(DB2DatabaseSnapshotBroker.QUERY);
                res = stmt.executeQuery();

                this.assignValues(res);
                this.getDatabase().getSnap().updateLastSnapshot();
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
        } else {
            if (majorVersion.isEqualThan(DB2MajorVersion.V9_7)) {
                throw new DatabaseConnectionException(
                        new IncopatibleDB2VersionException(minorVersion,
                                DB2MinorVersion.V9_7_1));
            } else if (majorVersion.isEqualThan(DB2MajorVersion.V9_8)) {
                throw new DatabaseConnectionException(
                        new IncopatibleDB2VersionException(minorVersion,
                                DB2MinorVersion.V9_8_2));
            } else {
                throw new DatabaseConnectionException(
                        new IncopatibleDB2VersionException(minorVersion,
                                DB2MinorVersion.V9_7_1));
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
