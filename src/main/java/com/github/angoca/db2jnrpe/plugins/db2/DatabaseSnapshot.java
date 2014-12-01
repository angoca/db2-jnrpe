package com.github.angoca.db2jnrpe.plugins.db2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains the values of a snapshot. This is used for the database load.
 *
 * @author Andres Gomez Casanova (@AngocA)
 * @version 2014-11-24
 */
public final class DatabaseSnapshot {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(DatabaseSnapshot.class);
    /**
     * Milliseconds.
     */
    private static final int MILLISECONDS = 1000;
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
     * Time when the previous snapshot was taken. This is used the first time
     * the script is executed in order to not return the values since the
     * database was activated, and also used to get the quantity of seconds
     * between calls in order to retrieve the mean.
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
     * Bufferpool data physical reads.
     */
    private long bpData;
    /**
     * Bufferpool index physical reads.
     */
    private long bpIndex;
    /**
     * Bufferpool temporal data physical reads.
     * 
     */
    private long bpTempData;
    /**
     * Bufferpool temporal index physical reads.
     */
    private long bpTempIndex;
    /**
     * Previous bufferpool data physical reads.
     */
    private long previousBpData;
    /**
     * Previous bufferpool index physical reads.
     */
    private long previousBpIndex;
    /**
     * Previous bufferpool temporal index physical reads.
     */
    private long previousBpTempData;
    /**
     * Previous bufferpool temporal index physical reads.
     */
    private long previousBpTempIndex;

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
     * @param bpdata
     *            Quantity of bufferpool data physical reads.
     * @param bpindex
     *            Quantity of bufferpool index physical reads.
     * @param bptempdata
     *            Quantity of bufferpool temporal data physical reads.
     * @param bptempindex
     *            Quantity of bufferpool temporal index physical reads.
     */
    public DatabaseSnapshot(final DB2Database db, final int partitionnum,
            final long commitSQL, final long selectSQL, final long uidSQL,
            final long bpdata, final long bpindex, final long bptempdata,
            final long bptempindex) {
        this.database = db;
        this.dbpartitionnum = partitionnum;
        this.commitSQLstmts = commitSQL;
        this.selectSQLstmts = selectSQL;
        this.uidSQLstmts = uidSQL;
        this.bpData = bpdata;
        this.bpIndex = bpindex;
        this.bpTempData = bptempdata;
        this.bpTempIndex = bptempindex;
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
                this.uidSQLstmts, this.bpData, this.bpIndex, this.bpTempData,
                this.bpTempIndex);
        copy.previousCommitSQLstmts = this.previousCommitSQLstmts;
        copy.previousSelectSQLstmts = this.previousSelectSQLstmts;
        copy.previousUidSQLstmts = this.previousUidSQLstmts;
        copy.lastSnapshot = this.lastSnapshot;
        copy.previousSnapshot = this.previousSnapshot;
        copy.previousBpData = this.bpData;
        copy.previousBpIndex = this.bpIndex;
        copy.previousBpTempData = this.bpTempData;
        copy.previousBpTempIndex = this.bpTempIndex;
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
    public double getLastCommitRate() throws UnknownValueException {
        if (this.previousSnapshot == 0) {
            throw new UnknownValueException("Second snapshot has not been read");
        }
        return (this.commitSQLstmts - this.previousCommitSQLstmts)
                / this.getLastSeconds();
    }

    /**
     * Returns the average physical I/O activity per committed transaction.
     * 
     * @return Quantity or read and writes per transaction.
     */
    public double getLastQuantityReadsWritesPerTransaction() {
        double ret = 0;
        if (this.commitSQLstmts > 0
                && this.previousCommitSQLstmts != this.commitSQLstmts) {
            long dividend = this.bpData - this.previousBpData + this.bpIndex
                    - this.previousBpIndex + this.bpTempData
                    - this.previousBpTempData + this.bpTempIndex
                    - this.previousBpTempIndex;
            long divisor = this.commitSQLstmts - this.previousCommitSQLstmts;
            ret = dividend / divisor;
            DatabaseSnapshot.log.debug("Ratio " + dividend + '/' + divisor
                    + '=' + ret);
        }
        return ret;
    }

    /**
     * Retrieves the quantity of seconds between the last two calls.
     * 
     * @return Quantity of seconds between the most recent calls.
     */
    public long getLastSeconds() {
        long ret = (this.lastSnapshot - this.previousSnapshot) / MILLISECONDS;
        return ret;
    }

    /**
     * Returns the delta of the last selects.
     *
     * @return Quantity of selects between the last two calls.
     * @throws UnknownValueException
     *             There is not a comparison value, the plugin should have two
     *             values in order to compare.
     */
    public double getLastSelectRate() throws UnknownValueException {
        if (this.previousSnapshot == 0) {
            throw new UnknownValueException("Second snapshot has not been read");
        }
        return (this.selectSQLstmts - this.previousSelectSQLstmts)
                / this.getLastSeconds();
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
    public double getLastUIDRate() throws UnknownValueException {
        if (this.previousSnapshot == 0) {
            throw new UnknownValueException("Second snapshot has not been read");
        }
        return (this.uidSQLstmts - this.previousUidSQLstmts)
                / this.getLastSeconds();
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
        if (this.lastSnapshot == 0 || this.previousSnapshot == 0) {
            // Never set.
            ret = false;
        } else if ((now - DatabaseSnapshot.SNAPSHOT_FREQUENCY) > this.lastSnapshot) {
            ret = false;
        }
        return ret;
    }

    /**
     * Changes the partition number.
     *
     * @param partitionnum
     *            Number of the partition.
     */
    private void setDbPartitionNum(final int partitionnum) {
        this.dbpartitionnum = partitionnum;
    }

    /**
     * Sets the values for the of the database load.
     * 
     * @param commitSQL
     *            Quantity of commits.
     * @param selectSQL
     *            Quantity of selects.
     * @param uidSQL
     *            Quantity of modifications. A modification can be an update,
     *            insert or delete.
     */
    private void setStmts(final long commitSQL, final long selectSQL,
            final long uidSQL) {
        if (commitSQL < this.commitSQLstmts || selectSQL < this.selectSQLstmts
                || uidSQL < this.uidSQLstmts) {
            // The database was recycled between two checks.
            this.previousCommitSQLstmts = 0;
            this.previousSelectSQLstmts = 0;
            this.previousUidSQLstmts = 0;
        } else {
            this.previousCommitSQLstmts = this.commitSQLstmts;
            this.previousSelectSQLstmts = this.selectSQLstmts;
            this.previousUidSQLstmts = this.uidSQLstmts;
        }
        this.commitSQLstmts = commitSQL;
        this.selectSQLstmts = selectSQL;
        this.uidSQLstmts = uidSQL;

    }

    /**
     * Establishes all values.
     * 
     * @param partition
     *            Partition number of the database.
     * @param commitSQL
     *            Quantity of commits.
     * @param selectSQL
     *            Quantity of selects.
     * @param uidSQL
     *            Quantity of UID.
     * @param bpdata
     *            Quantity of bufferpool data physical reads.
     * @param bpindex
     *            Quantity of bufferpool index physical reads.
     * @param bptempdata
     *            Quantity of bufferpool temporal data physical reads.
     * @param bptempindex
     *            Quantity of bufferpool temporal index physical reads.
     */
    public void setValues(final int partition, final long commitSQL,
            final long selectSQL, final long uidSQL, final long bpdata,
            final long bpindex, final long bptempdata, final long bptempindex) {
        this.setDbPartitionNum(partition);
        this.setStmts(commitSQL, selectSQL, uidSQL);
        this.setBpValues(bpdata, bpindex, bptempdata, bptempindex);
    }

    /**
     * Assign the bufferpool values to the snapshot, keeping old values to have
     * a comparison point.
     * 
     * @param bpdata
     *            Quantity of bufferpool data physical reads.
     * @param bpindex
     *            Quantity of bufferpool index physical reads.
     * @param bptempdata
     *            Quantity of bufferpool temporal data physical reads.
     * @param bptempindex
     *            Quantity of bufferpool temporal index physical reads.
     */
    private void setBpValues(final long bpdata, final long bpindex,
            final long bptempdata, final long bptempindex) {
        if (bpdata < this.bpData || bpindex < this.bpIndex
                || bptempdata < this.bpTempData
                || bptempindex < this.bpTempIndex) {
            // The database was recycled between two checks.
            this.previousBpData = 0;
            this.previousBpIndex = 0;
            this.previousBpTempData = 0;
            this.previousBpTempIndex = 0;
        } else {
            this.previousBpData = this.bpData;
            this.previousBpIndex = this.bpIndex;
            this.previousBpTempData = this.bpTempData;
            this.previousBpTempIndex = this.bpTempIndex;
        }
        this.bpData = bpdata;
        this.bpIndex = bpindex;
        this.bpTempData = bptempdata;
        this.bpTempIndex = bptempindex;
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
                + this.uidSQLstmts + ';' + this.bpData + ';' + this.bpIndex
                + ';' + this.bpTempData + ';' + this.bpTempIndex + ']';
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
