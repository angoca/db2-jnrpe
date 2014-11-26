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
import com.github.angoca.db2jnrpe.plugins.db2.DB2Database;
import com.github.angoca.db2jnrpe.plugins.db2.DB2DatabasesManager;
import com.github.angoca.db2jnrpe.plugins.db2.DatabaseSnapshot;

/**
 * Queries the database to retrieve the snapshot.
 *
 * @author Andres Gomez Casanova (@AngocA)
 * @version 2014-11-24
 */
public final class DB2DatabaseSnapshotBroker extends AbstractDB2Broker
implements Runnable {

    /**
     * Position of column commit quantity.
     */
    private static final int COL_POS_COMMIT = 2;

    /**
     * Position of column select quantity.
     */
    private static final int COL_POS_MEMBER = 3;

    /**
     * Position of column database partition.
     */
    private static final int COL_POS_PARTITION = 1;

    /**
     * Position of column modifications quantity.
     */
    private static final int COL_POS_TOTAL_READS = 4;

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory
            .getLogger(DB2DatabaseSnapshotBroker.class);

    /**
     * Query to get the values of the database charge.
     */
    private static final String QUERY = "SELECT DBPARTITIONNUM, "
            + "COMMIT_SQL_STMTS, SELECT_SQL_STMTS, UID_SQL_STMTS "
            + "FROM SYSIBMADM.SNAPDB";

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
        System.out.println("UIDs:" + snap.getLastUIDs() + ",Selects:"
                + snap.getLastSelects() + ",Commits:" + snap.getLastCommits());
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
        System.out.println("UIDs:" + snap.getLastUIDs() + ",Selects:"
                + snap.getLastSelects() + ",Commits:" + snap.getLastCommits());
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
            DatabaseSnapshot snap;
            while (res.next()) {
                // Partition.
                dbpartitionnum = res
                        .getInt(DB2DatabaseSnapshotBroker.COL_POS_PARTITION);
                // Quantity of commits;
                commitSQLstmts = res
                        .getLong(DB2DatabaseSnapshotBroker.COL_POS_COMMIT);
                // Quantity of sql;
                selectSQLstmts = res
                        .getLong(DB2DatabaseSnapshotBroker.COL_POS_MEMBER);
                // Quantity of modifications;
                uidSQLstmts = res
                        .getLong(DB2DatabaseSnapshotBroker.COL_POS_TOTAL_READS);

                DB2DatabaseSnapshotBroker.log.info(this.getDatabaseConnection()
                        .getUrl()
                        + "::Part "
                        + dbpartitionnum
                        + ", commit "
                        + commitSQLstmts
                        + ", select "
                        + selectSQLstmts
                        + ", uid " + uidSQLstmts);
                snap = this.getDatabase().getSnap();
                if (snap == null) {
                    DB2DatabaseSnapshotBroker.log.debug(this
                            .getDatabaseConnection().getUrl()
                            + "::Creating snap");
                    snap = new DatabaseSnapshot(this.getDatabase(),
                            dbpartitionnum, commitSQLstmts, selectSQLstmts,
                            uidSQLstmts);
                    this.getDatabase().setSnap(snap);
                } else {
                    DB2DatabaseSnapshotBroker.log.debug(this
                            .getDatabaseConnection().getUrl()
                            + "::Snap updated");
                    snap.setDbPartitionNum(dbpartitionnum);
                    snap.setValues(commitSQLstmts, selectSQLstmts, uidSQLstmts);
                }
            }
            this.getDatabase().getSnap().updateLastSnapshot();
            res.close();
            stmt.close();
            ConnectionPoolsManager.getInstance()
            .getConnectionPool(this.getDatabaseConnection())
            .closeConnection(this.getDatabaseConnection(), connection);
        } catch (final SQLException sqle) {
            DB2Helper.processException(sqle);
            throw new DatabaseConnectionException(sqle);
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
