package com.github.angoca.db2jnrpe.plugins.db2;

/**
 * This class represents a read of the bufferpool values.
 *
 * @author Andres Gomez Casanova (@AngocA)
 * @version 2014-11-03
 */
public final class BufferpoolRead {
    /**
     * Most recent value of logical reads.
     */
    private long logicalReads = 0;
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
    private long previousLogicalReads = 0;
    /**
     * Previous read of total reads.
     */
    private long previousTotalReads = 0;
    /**
     * Most recent value of total reads.
     */
    private long totalReads = 0;

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
    BufferpoolRead(final String bpName, final long logical, final long total,
            final int dbMember) {
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
    protected BufferpoolRead clone() {
        final BufferpoolRead copy = new BufferpoolRead(this.name,
                this.logicalReads, this.totalReads, this.member);
        copy.previousLogicalReads = this.previousLogicalReads;
        copy.previousTotalReads = this.previousTotalReads;
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
            ret = 100;
        } else if (this.previousTotalReads == 0) {
            ret = this.getRatio();
        } else if (this.previousTotalReads == this.totalReads) {
            ret = 100;
        } else {
            ret = (double) (this.logicalReads - this.previousLogicalReads)
                    * 100 / (this.totalReads - this.previousTotalReads);
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
    String getName() {
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
     * Returns the ratio between logical reads and total reads (logical +
     * physical reads).
     *
     * @return Ratio of the reads.
     */
    double getRatio() {
        double ret = 0;
        if (this.totalReads == 0) {
            // No reads until now.
            ret = 100;
        } else {
            ret = (double) this.logicalReads * 100 / this.totalReads;
        }
        return ret;
    }

    /**
     * Retrieves the total reads.
     *
     * @return Total reads.
     */
    long getTotalReads() {
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
            this.previousLogicalReads = 0;
        } else {
            this.previousLogicalReads = this.logicalReads;
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
    void setReads(final long logical, final long total) {
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
            this.previousTotalReads = 0;
        } else {
            this.previousTotalReads = this.totalReads;
        }
        this.totalReads = total;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final String ret = this.name + ";reads:" + this.logicalReads + '/'
                + this.totalReads;
        return ret;
    }
}
