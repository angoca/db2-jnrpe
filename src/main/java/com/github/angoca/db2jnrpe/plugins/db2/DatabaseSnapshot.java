package com.github.angoca.db2jnrpe.plugins.db2;


/**
 * Contains the values of a snapshot. This is used for the database load.
 *
 * @author Andres Gomez Casanova (@AngocA)
 * @version 2014-11-24
 */
public final class DatabaseSnapshot {

    /**
     * Snapshot frequency to read the corresponding values.
     */
    static final long SNAPSHOT_FREQUENCY = DB2Database.STANDARD_FREQUENCY;
    /**
     * Quantity of commits in the database.
     */
    private long commitSQLstmts;
    /**
     * Database that keeps all data.
     */
    private final DB2Database database;
    /**
     * Partition number.
     */
    private int dbpartitionnum;
    /**
     * Time of the last snapshot.
     */
    private long lastSnapshot = 0;
    /**
     * Quantity of selects in the database.
     */
    private long selectSQLstmts;

    /**
     * Quantity of modifciations in the database.
     */
    private long uidSQLstmts;

    /**
     * Creates a snapshot with the retrieved values from the table.
     *
     * @param db
     *            Object that hols all data.
     * @param partitionnum
     *            Database partition number.
     * @param commitSQL
     *            Quantity of commits.
     * @param selectSQL
     *            Quantity of selects.
     * @param uidSQL
     *            Quantity of modifications (update, insert, delete).
     */
    public DatabaseSnapshot(final DB2Database db, final int partitionnum,
            final long commitSQL, final long selectSQL, final long uidSQL) {
        this.database = db;
        this.dbpartitionnum = partitionnum;
        this.commitSQLstmts = commitSQL;
        this.selectSQLstmts = selectSQL;
        this.uidSQLstmts = uidSQL;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#clone()
     */
    @Override
    public DatabaseSnapshot clone() {
        final DatabaseSnapshot obj = new DatabaseSnapshot(this.database,
                this.dbpartitionnum, this.commitSQLstmts, this.selectSQLstmts,
                this.uidSQLstmts);
        return obj;
    }

    /**
     * Retrieves the quantity of commits.
     *
     * @return Quqnaitty of comits in the database.
     */
    public long getCommits() {
        return this.commitSQLstmts;
    }

    /**
     * Retrieves the last time the snapshot on the database was read.
     *
     * @return Time of the last snapshot.
     */
    public long getLastSnapshotRefresh() {
        return this.lastSnapshot;
    }

    /**
     * Retrieves the quantity of selects in the database.
     *
     * @return Quantity of selects in the database.
     */
    public long getSelects() {
        return this.selectSQLstmts;
    }


    /**
     * Retrieves the quantity of modifications: Updates, inserts and deletes.
     *
     * @return UIDs in the database.
     */
    public long getUIDs() {
        return this.uidSQLstmts;
    }

    /**
     * Checks if the snap should be updated.
     *
     * @return True if the snapshot is outdated or it is still valid.
     */
    boolean isSnapshotUpdated() {
        boolean ret = true;
        final long now = System.currentTimeMillis();
        if (this.lastSnapshot == 0) {
            // Never set.
            ret = false;
        } else if ((now - DatabaseSnapshot.SNAPSHOT_FREQUENCY) > this.lastSnapshot) {
            ret = false;
        }
        return ret;
    }

    /**
     * Sets the quantity of commits in the database.
     *
     * @param commitSQL
     *            Quantity of commits.
     */
    public void setCommtiSQLstmts(final long commitSQL) {
        this.commitSQLstmts = commitSQL;
    }

    /**
     * Changes the partition number.
     *
     * @param partitionnum
     *            Number of the partition.
     */
    public void setDbPartitionNum(final int partitionnum) {
        this.dbpartitionnum = partitionnum;
    }

    /**
     * Sets the quantity of selects in the database.
     *
     * @param selectSQL
     *            Quantity of selects.
     */
    public void setSelectSQLstmts(final long selectSQL) {
        this.selectSQLstmts = selectSQL;
    }

    /**
     * Sets the quantity of modifications in the database. A modification can be
     * an update, insert or delete.
     *
     * @param uidSQL
     *            Quantity of UID.
     */
    public void setUidSQLstmts(final long uidSQL) {
        this.uidSQLstmts = uidSQL;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final String ret = "Snapshot[" + this.dbpartitionnum + ","
                + this.commitSQLstmts + ':' + this.selectSQLstmts + ':'
                + this.uidSQLstmts + ']';
        return ret;
    }

    /**
     * Updates the value of the snap of the database.
     */
    public void updateLastSnapshot() {
        this.lastSnapshot = System.currentTimeMillis();
    }
}
