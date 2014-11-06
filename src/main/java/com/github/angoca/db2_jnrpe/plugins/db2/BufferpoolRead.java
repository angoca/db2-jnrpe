package com.github.angoca.db2_jnrpe.plugins.db2;

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
    private int logicalReads = 0;
    /**
     * Member of the database
     */
    private final int member;
    /**
     * Name of the bufferpool.
     */
    private String name;
    /**
     * Most recent value of total reads.
     */
    private int totalReads = 0;

    /**
     * Creates a set of most recent reads for a bufferpool.
     *
     * @param name
     *            Name of the bufferpool.
     * @param logical
     *            Quantity of logical reads.
     * @param total
     *            Quantity of total reads.
     * @param member
     *            Member of the database.
     */
    BufferpoolRead(final String name, final int logical, final int total,
            final int member) {
        assert logical <= total : "Logical reads should be less that total reads.";
        this.setName(name);
        this.setTotalReads(total);
        this.setLogicalReads(logical);
        this.member = member;
    }

    /**
     * Retrieves the logical reads.
     *
     * @return Quantity of logical reads.s
     */
    public int getLogicalReads() {
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
    public int getPhysicalReads() {
        return this.totalReads - this.logicalReads;
    }

    /**
     * Returns the ratio between logical reads and total reads (logical +
     * physical reads).
     *
     * @return Ratio of the reads.
     */
    public int getRatio() {
        int ret = 0;
        if (this.totalReads == 0) {
            ret = 100;
        } else {
            ret = this.logicalReads / this.totalReads;
        }
        return ret;
    }

    /**
     * Retrieves the total reads.
     *
     * @return Total reads.
     */
    public int getTotalReads() {
        return this.totalReads;
    }

    /**
     * Establishes the new quantity of logical reads.
     *
     * @param logical
     *            Quantity of logical reads.
     */
    private void setLogicalReads(final int logical) {
        assert logical > 0 : "Logical reads should be greater than zero.";
        assert logical < this.totalReads : "Logical reads should be less that total reads.";
        this.logicalReads = logical;
    }

    /**
     * Sets a new name for the bufferpool.
     *
     * @param name
     *            Bufferpool name.
     */
    private void setName(final String name) {
        this.name = name;
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
    void setReads(final int logical, final int total) {
        this.setLogicalReads(logical);
        this.setTotalReads(total);
    }

    /**
     * Establishes the new quantity of total reads.
     *
     * @param total
     *            Quantity of total reads.
     */
    private void setTotalReads(final int total) {
        assert total > 0 : "Total reads should be greater than zero.";
        assert this.logicalReads < total : "Logical reads should be less that total reads.";
        this.totalReads = total;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final String ret = this.name + ". Logical " + this.logicalReads + '/'
                + this.totalReads;
        return ret;
    }
}
