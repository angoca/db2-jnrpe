package com.github.angoca.db2jnrpe.plugins.db2.broker;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.angoca.db2jnrpe.database.DatabaseConnection;
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
public final class DB2DatabaseSnapshotBroker extends AbstractDB2Broker
        implements Runnable {

    /**
     * Position of column commit quantity.
     */
    private static final int COL_POS_COMMIT_SQL_STMTS = 2;
    /**
     * Position of column select quantity.
     */
    private static final int COL_POS_SELECT_SQL_STMTS = 3;
    /**
     * Position of column database partition.
     */
    private static final int COL_POS_DBPARTITIONNUM = 1;
    /**
     * Position of column modifications quantity.
     */
    private static final int COL_POS_UID_SQL_STMTS = 4;
    /**
     * Position of column bufferpool data physical reads.
     */
    private static final int COL_POS_POOL_DATA_P_READS = 5;
    /**
     * Position of column bufferpool index physical reads.
     */
    private static final int POOL_INDEX_P_READS = 6;
    /**
     * Position of column bufferpool temp physical reads.
     */
    private static final int POOL_TEMP_DATA_P_READS = 7;
    /**
     * Position of column bufferpool temp index physical reads.
     */
    private static final int POOL_TEMP_INDEX_P_READS = 8;

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory
            .getLogger(DB2DatabaseSnapshotBroker.class);

    /**
     * Query to get the values of the database charge.
     */
    private static final String QUERY = "SELECT DBPARTITIONNUM, "
            + "COMMIT_SQL_STMTS, SELECT_SQL_STMTS, UID_SQL_STMTS, "
            + "POOL_DATA_P_READS, POOL_INDEX_P_READS, POOL_TEMP_DATA_P_READS, "
            + "POOL_TEMP_INDEX_P_READS " + "FROM SYSIBMADM.SNAPDB";

    /**
     * Tester.
     *
     * @param args
     *            Arguments
     * @throws Exception
     *             Any exception.
     */
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
        DatabaseConnection dbConn;

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
    public DB2DatabaseSnapshotBroker(final DatabaseConnection connProps,
            final DB2Database db2database) {
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
    protected void check() throws DatabaseConnectionException {
        assert this.getDatabaseConnection() != null;
        final DB2MajorVersion majorVersion = DB2Helper.getDB2MajorVersion(this
                .getDatabaseConnection());
        final DB2MinorVersion minorVersion = DB2Helper.getDB2MinorVersion(this
                .getDatabaseConnection());
        // This query cannot be executed in a database with db2 v9.5 or before.
        if (majorVersion.isEqualOrMoreRecentThan(DB2MajorVersion.V10_1)
                || (majorVersion.isEqualThan(DB2MajorVersion.V9_7) && minorVersion
                        .isEqualOrMoreRecentThan(DB2MinorVersion.V9_7_1))
                || (majorVersion.isEqualThan(DB2MajorVersion.V9_8) && minorVersion
                        .isEqualOrMoreRecentThan(DB2MinorVersion.V9_8_2))) {

            Connection connection = null;
            try {
                connection = ConnectionPoolsManager.getInstance()
                        .getConnectionPool(this.getDatabaseConnection())
                        .getConnection(this.getDatabaseConnection());
                final PreparedStatement stmt = connection
                        .prepareStatement(DB2DatabaseSnapshotBroker.QUERY);
                final ResultSet res = stmt.executeQuery();

                int dbpartitionnum;
                long commitSQLstmts;
                long selectSQLstmts;
                long uidSQLstmts;
                long bpdata;
                long bpindex;
                long bptempdata;
                long bptempindex;
                DatabaseSnapshot snap;
                while (res.next()) {
                    // Partition.
                    dbpartitionnum = res
                            .getInt(DB2DatabaseSnapshotBroker.COL_POS_DBPARTITIONNUM);
                    // Quantity of commits.
                    commitSQLstmts = res
                            .getLong(DB2DatabaseSnapshotBroker.COL_POS_COMMIT_SQL_STMTS);
                    // Quantity of sql.
                    selectSQLstmts = res
                            .getLong(DB2DatabaseSnapshotBroker.COL_POS_SELECT_SQL_STMTS);
                    // Quantity of modifications.
                    uidSQLstmts = res
                            .getLong(DB2DatabaseSnapshotBroker.COL_POS_UID_SQL_STMTS);
                    // Quantity of bufferpool physical reads for data.
                    bpdata = res
                            .getLong(DB2DatabaseSnapshotBroker.COL_POS_POOL_DATA_P_READS);
                    // Quantity of bufferpool physical reads for index.
                    bpindex = res
                            .getLong(DB2DatabaseSnapshotBroker.POOL_INDEX_P_READS);
                    // Quantity of bufferpool physical reads for temporal data.
                    bptempdata = res
                            .getLong(DB2DatabaseSnapshotBroker.POOL_TEMP_DATA_P_READS);
                    // Quantity of bufferpool physical reads for temporal index.
                    bptempindex = res
                            .getLong(DB2DatabaseSnapshotBroker.POOL_TEMP_INDEX_P_READS);

                    DB2DatabaseSnapshotBroker.log.info(this
                            .getDatabaseConnection().getUrl()
                            + "::Part "
                            + dbpartitionnum
                            + ", commit "
                            + commitSQLstmts
                            + ", select "
                            + selectSQLstmts
                            + ", uid "
                            + uidSQLstmts);
                    snap = this.getDatabase().getSnap();
                    if (snap == null) {
                        DB2DatabaseSnapshotBroker.log.debug(this
                                .getDatabaseConnection().getUrl()
                                + "::Creating snap");
                        snap = new DatabaseSnapshot(this.getDatabase(),
                                dbpartitionnum, commitSQLstmts, selectSQLstmts,
                                uidSQLstmts, bpdata, bpindex, bptempdata,
                                bptempindex);
                        this.getDatabase().setSnap(snap);
                    } else {
                        DB2DatabaseSnapshotBroker.log.debug(this
                                .getDatabaseConnection().getUrl()
                                + "::Snap updated");
                        snap.setValues(dbpartitionnum, commitSQLstmts, selectSQLstmts,
                                uidSQLstmts, bpdata, bpindex, bptempdata,
                                bptempindex);
                    }
                }
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
    public void run() {
        super.setLock();
    }

}
