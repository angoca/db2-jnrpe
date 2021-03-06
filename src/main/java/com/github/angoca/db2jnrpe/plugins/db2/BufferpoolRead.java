package com.github.angoca.db2jnrpe.plugins.db2;

/**
 * This class represents a read of the bufferpool values.
 *
 * @author Andres Gomez Casanova (@AngocA)
 * @version 2014-11-03
 */
@SuppressWarnings("PMD.CommentSize")
public final class BufferpoolRead implements Cloneable {
    /**
     * Frequency to read the bufferpools. 60000 means each 10 minutes.
     */
    public static final long BUFFERPOOL_FREQ = DB2Database.STANDARD_FREQ;
    /**
     * Hundred percent.
     */
    private static final int HUNDRED_PERCENT = 100;
    /**
     * Most recent value of logical reads.
     */
    private long logicalReads;
    /**
     * Member of the database.
     */
    private final int member;
    /**
     * Name of the bufferpool.
     */
    private final String name;
    /**
     * Previous read of logical reads.
     */
    private transient long prevLogicalReads;
    /**
     * Previous read of total reads.
     */
    private transient long prevTotalReads;
    /**
     * Most recent value of total reads.
     */
    private long totalReads;

    /**
     * Creates a set of most recent reads for a bufferpool.
     *
     * @param bpName
     *            Name of the bufferpool.
     * @param logical
     *            Quantity of logical reads.
     * @param total
     *            Quantity of total reads.
     * @param dbMember
     *            Member of the database.
     */
    public BufferpoolRead(final String bpName, final long logical,
            final long total, final int dbMember) {
        assert logical <= total : "Logical reads should be less "
                + "that total reads.";
        this.name = bpName;
        this.setTotalReads(total);
        this.setLogicalReads(logical);
        this.member = dbMember;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#clone()
     */
    @Override
    @SuppressWarnings({ "PMD.CommentRequired", "PMD.ProperCloneImplementation" })
    public BufferpoolRead clone() {
        final BufferpoolRead copy = new BufferpoolRead(this.name,
                this.logicalReads, this.totalReads, this.member);
        copy.prevLogicalReads = this.prevLogicalReads;
        copy.prevTotalReads = this.prevTotalReads;
        return copy;
    }

    /**
     * Returns the most recent ratio between logical reads and total reads
     * (logical + physical reads). This is calculated between the previous read
     * values an the current ones.
     *
     * @return Ratio of the reads.
     */
    public double getLastRatio() {
        double ret = 0;
        if (this.totalReads == 0) {
            // No reads until now.
            ret = BufferpoolRead.HUNDRED_PERCENT;
        } else if (this.prevTotalReads == 0) {
            ret = (double) this.logicalReads * BufferpoolRead.HUNDRED_PERCENT
                    / this.totalReads;
        } else if (this.prevTotalReads == this.totalReads) {
            ret = BufferpoolRead.HUNDRED_PERCENT;
        } else {
            ret = (double) (this.logicalReads - this.prevLogicalReads)
                    * BufferpoolRead.HUNDRED_PERCENT
                    / (this.totalReads - this.prevTotalReads);
        }

        // Check values.
        if (ret > BufferpoolRead.HUNDRED_PERCENT) {
            ret = BufferpoolRead.HUNDRED_PERCENT;
        } else if (ret < 0) {
            ret = 0;
        }
        return ret;
    }

    /**
     * Retrieves the logical reads.
     *
     * @return Quantity of logical reads.s
     */
    public long getLogicalReads() {
        return this.logicalReads;
    }

    /**
     * Retrieves the member of the database.
     *
     * @return Member of the database.
     */
    public int getMember() {
        return this.member;
    }

    /**
     * Retrieves the name of the bufferpool.
     *
     * @return Name of the bufferpool.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Retrieves the physical reads.
     *
     * @return Quantity of physical reads (total - logical).
     */
    public long getPhysicalReads() {
        return this.totalReads - this.logicalReads;
    }

    /**
     * Retrieves the total reads.
     *
     * @return Total reads.
     */
    public long getTotalReads() {
        return this.totalReads;
    }

    /**
     * Establishes the new quantity of logical reads.
     *
     * @param logical
     *            Quantity of logical reads.
     */
    private void setLogicalReads(final long logical) {
        assert logical > 0 : "Logical reads should be greater than zero.";
        assert logical < this.totalReads : "Logical reads should be less that total reads.";

        if (logical < this.logicalReads) {
            // The database was recycled between two checks.
            this.prevLogicalReads = 0;
        } else {
            this.prevLogicalReads = this.logicalReads;
        }
        this.logicalReads = logical;
    }

    /**
     * Updates the values of the read with the most recent ones if the last
     * update is old.
     *
     * @param logical
     *            Quantity of logical reads.
     * @param total
     *            Quantity of total reads.
     */
    public void setReads(final long logical, final long total) {
        this.setLogicalReads(logical);
        this.setTotalReads(total);
    }

    /**
     * Establishes the new quantity of total reads.
     *
     * @param total
     *            Quantity of total reads.
     */
    private void setTotalReads(final long total) {
        assert total > 0 : "Total reads should be greater than zero.";
        assert this.logicalReads < total : "Logical reads should be less that total reads.";

        if (total < this.totalReads) {
            // The database was recycled between two checks.
            this.prevTotalReads = 0;
        } else {
            this.prevTotalReads = this.totalReads;
        }
        this.totalReads = total;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    @SuppressWarnings("PMD.CommentRequired")
    public String toString() {
        final String ret = "Bufferpool[" + this.member + ';' + this.name
                + ";reads:" + this.logicalReads + '/' + this.totalReads + ']';
        return ret;
    }
}
