package com.github.angoca.db2jnrpe.plugins.db2;

/**
 * Contains the values of a snapshot. This is used for the database load.
 *
 * @author Andres Gomez Casanova (@AngocA)
 * @version 2014-11-24
 */
public final class DatabaseSnapshot {

    /**
     * Snapshot frequency to read the corresponding values : 10 minutes.
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
     * Previous read of the quantity of commits in the database.
     */
    private long previousCommitSQLstmts;
    /**
     * Previous read of the quantity of selects in the database.
     */
    private long previousSelectSQLstmts;
    /**
     * Time when the previous snapshot was taken. This is only used the first
     * time the script is executed in order to not return the values since the
     * database was activated.
     */
    private long previousSnapshot;
    /**
     * Previous read of the quantity of modifications in the database (update,
     * insert, delete).
     */
    private long previousUidSQLstmts;

    /**
     * Quantity of selects in the database.
     */
    private long selectSQLstmts;
    /**
     * Quantity of modifications in the database.
     */
    private long uidSQLstmts;

    /**
     * Creates a snapshot with the retrieved values from the table.
     *
     * @param db
     *            Object that holds all data.
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
        final DatabaseSnapshot copy = new DatabaseSnapshot(this.database,
                this.dbpartitionnum, this.commitSQLstmts, this.selectSQLstmts,
                this.uidSQLstmts);
        copy.previousCommitSQLstmts = this.previousCommitSQLstmts;
        copy.previousSelectSQLstmts = this.previousSelectSQLstmts;
        copy.previousUidSQLstmts = this.previousUidSQLstmts;
        copy.lastSnapshot = this.lastSnapshot;
        copy.previousSnapshot = this.previousSnapshot;
        return copy;
    }

    /**
     * Retrieves the quantity of commits.
     *
     * @return Quantity of commits in the database.
     */
    public long getCommits() {
        return this.commitSQLstmts;
    }

    /**
     * Returns the delta of the last commits.
     *
     * @return Quantity of commits between the last two calls.
     * @throws UnknownValueException
     *             There is not a comparison value, the plugin should have two
     *             values in order to compare.
     */
    public long getLastCommits() throws UnknownValueException {
        if (this.previousSnapshot == 0) {
            throw new UnknownValueException("Second snapshot has not been read");
        }
        return this.commitSQLstmts - this.previousCommitSQLstmts;
    }

    /**
     * Returns the delta of the last selects.
     *
     * @return Quantity of selects between the last two calls.
     * @throws UnknownValueException
     *             There is not a comparison value, the plugin should have two
     *             values in order to compare.
     */
    public long getLastSelects() throws UnknownValueException {
        if (this.previousSnapshot == 0) {
            throw new UnknownValueException("Second snapshot has not been read");
        }
        return this.selectSQLstmts - this.previousSelectSQLstmts;
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
     * Retrieves the delta of the last modifications.
     *
     * @return Quantity of UIDs between the last two calls.
     * @throws UnknownValueException
     *             There is not a comparison value, the plugin should have two
     *             values in order to compare.
     */
    public long getLastUIDs() throws UnknownValueException {
        if (this.previousSnapshot == 0) {
            throw new UnknownValueException("Second snapshot has not been read");
        }
        return this.uidSQLstmts - this.previousUidSQLstmts;
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
     * @return True if the snapshot is obsolete or it is still valid.
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
    private void setCommitSQLstmts(final long commitSQL) {
        if (commitSQL < this.commitSQLstmts) {
            // The database was recycled between two checks.
            this.previousCommitSQLstmts = 0;
        } else {
            this.previousCommitSQLstmts = this.commitSQLstmts;
        }
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
    private void setSelectSQLstmts(final long selectSQL) {
        if (selectSQL < this.selectSQLstmts) {
            // The database was recycled between two checks.
            this.previousSelectSQLstmts = 0;
        } else {
            this.previousSelectSQLstmts = this.selectSQLstmts;
        }
        this.selectSQLstmts = selectSQL;
    }

    /**
     * Sets the quantity of modifications in the database. A modification can be
     * an update, insert or delete.
     *
     * @param uidSQL
     *            Quantity of UID.
     */
    private void setUidSQLstmts(final long uidSQL) {
        if (uidSQL < this.uidSQLstmts) {
            // The database was recycled between two checks.
            this.previousUidSQLstmts = 0;
        } else {
            this.previousUidSQLstmts = this.uidSQLstmts;
        }
        this.uidSQLstmts = uidSQL;
    }

    /**
     * Establishes all values.
     *
     * @param commitSQL
     *            Quantity of commits.
     * @param selectSQL
     *            Quantity of selects.
     * @param uidSQL
     *            Quantity of UID.
     */
    public void setValues(final long commitSQL, final long selectSQL,
            final long uidSQL) {
        this.setCommitSQLstmts(selectSQL);
        this.setSelectSQLstmts(commitSQL);
        this.setUidSQLstmts(uidSQL);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final String ret = "Snapshot[" + this.dbpartitionnum + ';'
                + this.commitSQLstmts + ';' + this.selectSQLstmts + ';'
                + this.uidSQLstmts + ']';
        return ret;
    }

    /**
     * Updates the value of the snap of the database. Sets the previous snapshot
     * in order to have a compare point the first time the snapshot is taken.
     */
    public void updateLastSnapshot() {
        this.previousSnapshot = this.lastSnapshot;
        this.lastSnapshot = System.currentTimeMillis();
    }
}
