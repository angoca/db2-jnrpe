package com.github.angoca.db2jnrpe.plugins.db2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains the values of a snapshot. This is used for the database load.
 *
 * @author Andres Gomez Casanova (@AngocA)
 * @version 2014-11-24
 */
@SuppressWarnings({ "PMD.CommentSize", "PMD.TooManyFields" })
public final class DatabaseSnapshot implements Cloneable {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(DatabaseSnapshot.class);
    /**
     * Milliseconds.
     */
    private static final int MILLISECONDS = 1000;
    /**
     * Snapshot frequency to read the corresponding values : 10 minutes.
     */
    public static final long SNAPSHOT_FREQ = DB2Database.STANDARD_FREQ;
    /**
     * Bufferpool data physical reads.
     */
    private transient long bpData;
    /**
     * Bufferpool index physical reads.
     */
    private transient long bpIndex;
    /**
     * Bufferpool temporal data physical reads.
     *
     */
    private transient long bpTempData;
    /**
     * Bufferpool temporal index physical reads.
     */
    private transient long bpTempIndex;
    /**
     * Quantity of commits in the database.
     */
    private transient long commitSQLstmts;
    /**
     * Database that keeps all data.
     */
    private final transient DB2Database database;
    /**
     * Partition number.
     */
    private transient int dbPartNum;
    /**
     * Time of the last snapshot.
     */
    private transient long lastSnapshot;

    /**
     * Previous bufferpool data physical reads.
     */
    private transient long prevBpData;
    /**
     * Previous bufferpool index physical reads.
     */
    private transient long prevBpIndex;
    /**
     * Previous bufferpool temporal index physical reads.
     */
    private transient long prevBpTempData;
    /**
     * Previous bufferpool temporal index physical reads.
     */
    private transient long prevBpTempIndex;
    /**
     * Previous read of the quantity of commits in the database.
     */
    private transient long prevComSQLstmts;
    /**
     * Previous read of the quantity of selects in the database.
     */
    private transient long prevSelSQLstmts;
    /**
     * Time when the previous snapshot was taken. This is used the first time
     * the script is executed in order to not return the values since the
     * database was activated, and also used to get the quantity of seconds
     * between calls in order to retrieve the mean.
     */
    private transient long prevSnapshot;
    /**
     * Previous read of the quantity of modifications in the database (update,
     * insert, delete).
     */
    private transient long prevUidSQLstmts;
    /**
     * Quantity of selects in the database.
     */
    private transient long selectSQLstmts;
    /**
     * Quantity of modifications in the database.
     */
    private transient long uidSQLstmts;
    /**
     * Quantity of time passed doing sorts.
     */
    private long totalSortTimeSecs;
    /**
     * Previous value of totalSortTimeSecs.
     */
    private long prevTotalSortTime;
    /**
     * Quantity of sorts.
     */
    private long totalSorts;
    /**
     * Previous value of total sorts.
     */
    private long prevTotalSorts;

    /**
     * Creates a snapshot with the retrieved values from the table.
     *
     * @param dataBase
     *            Object that holds all data.
     * @param partitionnum
     *            Database partition number.
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
    public DatabaseSnapshot(final DB2Database dataBase, final int partitionnum,
            final long selectSQL, final long uidSQL, final long bpdata,
            final long bpindex, final long bptempdata, final long bptempindex) {
        this.database = dataBase;
        this.dbPartNum = partitionnum;
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
    @SuppressWarnings({ "PMD.CommentRequired", "PMD.ProperCloneImplementation" })
    public DatabaseSnapshot clone() {
        final DatabaseSnapshot copy = new DatabaseSnapshot(this.database,
                this.dbPartNum, this.selectSQLstmts, this.uidSQLstmts,
                this.bpData, this.bpIndex, this.bpTempData, this.bpTempIndex);
        copy.prevComSQLstmts = this.prevComSQLstmts;
        copy.prevSelSQLstmts = this.prevSelSQLstmts;
        copy.prevUidSQLstmts = this.prevUidSQLstmts;
        copy.lastSnapshot = this.lastSnapshot;
        copy.prevSnapshot = this.prevSnapshot;
        copy.prevBpData = this.prevBpData;
        copy.prevBpIndex = this.prevBpIndex;
        copy.prevBpTempData = this.prevBpTempData;
        copy.prevBpTempIndex = this.prevBpTempIndex;

        copy.commitSQLstmts = this.commitSQLstmts;
        copy.prevComSQLstmts = this.prevComSQLstmts;

        copy.totalSorts = this.totalSorts;
        copy.prevTotalSorts = this.prevTotalSorts;

        copy.totalSortTimeSecs = this.totalSortTimeSecs;
        copy.prevTotalSortTime = this.prevTotalSortTime;
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
     * Returns the average of sort time between the last two checks.
     * 
     * @return Average of sort time.
     */
    public double getLastAverageSortTime() {
        double ret = 0;
        final long dividend = this.getLastTotalSortTimeSecs();
        final long divisor = this.getLastTotalSorts();
        if (divisor != 0) {
            ret = (double) dividend / (double) divisor;
        }
        // Database was recycled.
        if (ret < 0) {
            ret = 0;
        }
        return ret;
    }

    /**
     * Returns the last difference of commits.
     * 
     * @return Quantity of commits between the last two checks.
     */
    public long getLastCommits() {
        return this.commitSQLstmts - this.prevComSQLstmts;
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
        if (this.prevSnapshot == 0) {
            throw new UnknownValueException("Second snapshot has not been read");
        }
        double ret = 0;
        long secs = this.getLastSeconds();
        if (secs != 0) {
            ret = (double) this.getLastCommits() / secs;
        }
        // Database was recycled.
        if (ret < 0) {
            ret = 0;
        }
        return ret;
    }

    /**
     * Quantity of generated IO in the last two checks.
     * 
     * @return Quantity of IO in the last two checks.
     */
    public long getLastIO() {
        long ret = this.bpData - this.prevBpData + this.bpIndex
                - this.prevBpIndex + this.bpTempData - this.prevBpTempData
                + this.bpTempIndex - this.prevBpTempIndex;
        return ret;
    }

    /**
     * Returns the quantity of sorts between the last two checks.
     * 
     * @return Delta of quantity of sorts.
     */
    public long getLastTotalSorts() {
        long ret = this.totalSorts - this.prevTotalSorts;
        return ret;
    }

    /**
     * Returns the quantity of time expended doing sorts between the last two
     * checks.
     * 
     * @return Delta of time expended doing sorts.
     */
    public long getLastTotalSortTimeSecs() {
        long ret = this.totalSortTimeSecs - this.prevTotalSortTime;
        return ret;
    }

    /**
     * Returns the average physical I/O activity per committed transaction.
     *
     * @return Quantity or read and writes per transaction.
     */
    public double getLastQuantityReadsWritesPerTransaction() {
        double ret = 0;
        final long dividend = this.getLastIO();
        final long divisor = this.getLastCommits();
        if (divisor != 0) {
            ret = (double) dividend / (double) divisor;

            if (DatabaseSnapshot.LOGGER.isDebugEnabled()) {
                DatabaseSnapshot.LOGGER.debug("Values {}-{}+{}-{}+{}-{}+{}-{}",
                        new Object[] { this.bpData, this.prevBpData,
                                this.bpIndex, this.prevBpIndex,
                                this.bpTempData, this.prevBpTempData,
                                this.bpTempIndex, this.prevBpTempIndex });
                DatabaseSnapshot.LOGGER.debug(
                        "Ratio {}/{}={} at {}",
                        new Object[] { dividend, divisor, ret,
                                this.database.getId() });
            }
        }
        if (ret < 0) {
            ret = 0;
        }
        return ret;
    }

    /**
     * Retrieves the quantity of seconds between the last two calls.
     *
     * @return Quantity of seconds between the most recent calls.
     */
    public long getLastSeconds() {
        return (this.lastSnapshot - this.prevSnapshot)
                / DatabaseSnapshot.MILLISECONDS;
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
        if (this.prevSnapshot == 0) {
            throw new UnknownValueException("Second snapshot has not been read");
        }
        double ret = 0;
        long secs = this.getLastSeconds();
        if (secs != 0) {
            ret = (this.selectSQLstmts - this.prevSelSQLstmts) / secs;
        }
        // Database was recycled.
        if (ret < 0) {
            ret = 0;
        }
        return ret;
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
     * Returns the time used for sorts per transaction.
     * 
     * @return Time used for sorts per transaction between the last two checks.
     */
    public double getLastSortTimePerTransaction() {
        double ret = 0;
        final long dividend = this.getLastTotalSortTimeSecs();
        final long divisor = this.getLastCommits();
        if (divisor != 0) {
            ret = (double) dividend / (double) divisor;
        }
        // Database was recycled.
        if (ret < 0) {
            ret = 0;
        }
        return ret;
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
        if (this.prevSnapshot == 0) {
            throw new UnknownValueException("Second snapshot has not been read");
        }
        double ret = 0;
        long secs = this.getLastSeconds();
        if (secs != 0) {
            ret = (this.uidSQLstmts - this.prevUidSQLstmts) / secs;
        }
        // Database was recycled.
        if (ret < 0) {
            ret = 0;
        }
        return ret;
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
     * Returns the value of the total sorts.
     * 
     * @return Total sorts.
     */
    public long getTotalSorts() {
        return this.totalSorts;
    }

    /**
     * Returns the total quantity of seconds used for sort time.
     * 
     * @return Total time for sorts.
     */
    public long getTotalSortTimeSec() {
        return this.totalSortTimeSecs;
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
    public boolean isSnapshotUpdated() {
        boolean ret = true;
        final long now = System.currentTimeMillis();
        if (this.lastSnapshot == 0 || this.prevSnapshot == 0) {
            // Never set.
            ret = false;
        } else if (now - DatabaseSnapshot.SNAPSHOT_FREQ > this.lastSnapshot) {
            ret = false;
        }
        return ret;
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
            this.prevBpData = 0;
            this.prevBpIndex = 0;
            this.prevBpTempData = 0;
            this.prevBpTempIndex = 0;
        } else {
            this.prevBpData = this.bpData;
            this.prevBpIndex = this.bpIndex;
            this.prevBpTempData = this.bpTempData;
            this.prevBpTempIndex = this.bpTempIndex;
        }
        this.bpData = bpdata;
        this.bpIndex = bpindex;
        this.bpTempData = bptempdata;
        this.bpTempIndex = bptempindex;
        if (DatabaseSnapshot.LOGGER.isDebugEnabled()) {
            DatabaseSnapshot.LOGGER.debug("New:{},{},{},{};Old:{},{},{},{}",
                    new Object[] { this.bpData, this.bpIndex, this.bpTempData,
                            this.bpTempIndex, this.prevBpData,
                            this.prevBpIndex, this.prevBpTempData,
                            this.prevBpTempIndex });
        }
    }

    /**
     * Sets the quantity of commits.
     * 
     * @param commitSQL
     *            Quantity of commits.
     */
    public void setCommits(final long commitSQL) {
        if (commitSQL < this.commitSQLstmts) {
            // The database was recycled between two checks.
            this.prevComSQLstmts = 0;
        } else {
            this.prevComSQLstmts = this.commitSQLstmts;
        }
        this.commitSQLstmts = commitSQL;
    }

    /**
     * Changes the partition number.
     *
     * @param partitionnum
     *            Number of the partition.
     */
    private void setDbPartitionNum(final int partitionnum) {
        this.dbPartNum = partitionnum;
    }

    /**
     * Sets the values for the of the database load.
     *
     * @param selectSQL
     *            Quantity of selects.
     * @param uidSQL
     *            Quantity of modifications. A modification can be an update,
     *            insert or delete.
     */
    private void setStmts(final long selectSQL, final long uidSQL) {
        if (selectSQL < this.selectSQLstmts || uidSQL < this.uidSQLstmts) {
            // The database was recycled between two checks.
            this.prevSelSQLstmts = 0;
            this.prevUidSQLstmts = 0;
        } else {
            this.prevSelSQLstmts = this.selectSQLstmts;
            this.prevUidSQLstmts = this.uidSQLstmts;
        }
        this.selectSQLstmts = selectSQL;
        this.uidSQLstmts = uidSQL;

    }

    /**
     * Establishes all values.
     *
     * @param partition
     *            Partition number of the database.
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
    public void setValues(final int partition, final long selectSQL,
            final long uidSQL, final long bpdata, final long bpindex,
            final long bptempdata, final long bptempindex) {
        this.setDbPartitionNum(partition);
        this.setStmts(selectSQL, uidSQL);
        this.setBpValues(bpdata, bpindex, bptempdata, bptempindex);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    @SuppressWarnings("PMD.CommentRequired")
    public String toString() {
        final String ret = "Snapshot[" + this.dbPartNum + ';'
                + this.commitSQLstmts + ';' + this.selectSQLstmts + ';'
                + this.uidSQLstmts + ';' + this.bpData + ';' + this.bpIndex
                + ';' + this.bpTempData + ';' + this.bpTempIndex + ';'
                + this.totalSorts + ';' + this.totalSortTimeSecs + ']';
        return ret;
    }

    /**
     * Updates the value of the snap of the database. Sets the previous snapshot
     * in order to have a compare point the first time the snapshot is taken.
     */
    public void updateLastSnapshot() {
        this.prevSnapshot = this.lastSnapshot;
        this.lastSnapshot = System.currentTimeMillis();
    }

    /**
     * Sets the quantity of time expended doing sorts. The value received is in
     * milliseconds, but converted to seconds.
     * 
     * @param totalsorttime
     *            Total time used for sorts (milliseconds).
     */
    public void setTotalSortTime(final long totalsorttime) {
        if (totalsorttime < this.totalSortTimeSecs) {
            // The database was recycled between two checks.
            this.prevTotalSortTime = 0;
        } else {
            this.prevTotalSortTime = this.totalSortTimeSecs;
        }
        this.totalSortTimeSecs = totalsorttime / 1000;
        if (DatabaseSnapshot.LOGGER.isDebugEnabled()) {
            DatabaseSnapshot.LOGGER.debug("New:{};Old:{}",
                    this.totalSortTimeSecs, this.prevTotalSortTime);
        }
    }

    /**
     * Sets the quantity of sorts.
     * 
     * @param totalsorts
     *            Quantity of sorts.
     */
    public void setTotalSorts(final long totalsorts) {
        if (totalsorts < this.totalSorts) {
            // The database was recycled between two checks.
            this.prevTotalSorts = 0;
        } else {
            this.prevTotalSorts = this.totalSorts;
        }
        this.totalSorts = totalsorts;
        if (DatabaseSnapshot.LOGGER.isDebugEnabled()) {
            DatabaseSnapshot.LOGGER.debug("New:{};Old:{}", this.totalSorts,
                    this.prevTotalSorts);
        }
    }
}
