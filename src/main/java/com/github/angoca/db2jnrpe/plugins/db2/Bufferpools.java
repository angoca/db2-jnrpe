package com.github.angoca.db2jnrpe.plugins.db2;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Object that holds the bufferpool reads and the time when the values were
 * gathered.
 *
 * @author Andres Gomez Casanova (@AngocA)
 * @version 2014-11-03
 */
public final class Bufferpools implements Cloneable {
    /**
     * Hash of bufferpoolReads reads.
     */
    private transient Map<String, BufferpoolRead> bufferpoolReads;
    /**
     * Database that keeps all data.
     */
    private final transient DB2Database database;

    /**
     * Time of the last bufferpoolReads read.
     */
    private transient long lastBpRead;

    /**
     * Creates a bufferpool with the associated database.
     *
     * @param dataBase
     *            Associated database.
     */
    public Bufferpools(final DB2Database dataBase) {
        this.database = dataBase;
        this.bufferpoolReads = new HashMap<String, BufferpoolRead>();
    }

    /**
     * Adds a bufferpool read to the database.
     *
     * @param bpr
     *            Bufferpool read.
     */
    public void addBufferpoolRead(final BufferpoolRead bpr) {
        this.bufferpoolReads.put(bpr.getName(), bpr);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#clone()
     */
    @Override
    @SuppressWarnings({ "PMD.CommentRequired", "PMD.ProperCloneImplementation" })
    public Object clone() {
        final Bufferpools ret = new Bufferpools(this.database);
        ret.lastBpRead = this.lastBpRead;
        ret.bufferpoolReads = this.cloneBufferpools();
        return ret;
    }

    /**
     * Clone the set of bufferpoolReads.
     *
     * @return Copy of the set of bufferpoolReads.
     */
    private Map<String, BufferpoolRead> cloneBufferpools() {
        final ConcurrentMap<String, BufferpoolRead> copy = new ConcurrentHashMap<String, BufferpoolRead>();
        for (final String key : this.bufferpoolReads.keySet()) {
            final BufferpoolRead clone = this.bufferpoolReads.get(key).clone();
            copy.put(key, clone);
        }
        return copy;
    }

    /**
     * Retrieves the set of bufferpool reads.
     *
     * @return Map to the bufferpool reads.
     */
    public Map<String, BufferpoolRead> getBufferpoolReads() {
        return this.bufferpoolReads;
    }

    /**
     * Retrieves the last time the bufferpoolReads reads were updated.
     *
     * @return Time of last read.
     */
    public long getLastBufferpoolRefresh() {
        return this.lastBpRead;
    }

    /**
     * Checks if the list of bufferpoolReads should be updated.
     *
     * @return True if the list is too old or never set. False otherwise.
     */
    public boolean isBufferpoolListUpdated() {
        boolean ret = true;
        final long now = System.currentTimeMillis();
        if (this.lastBpRead == 0) {
            // Never set.
            ret = false;
        } else if (now - BufferpoolRead.BUFFERPOOL_FREQ > this.lastBpRead) {
            ret = false;
        }
        return ret;
    }

    /**
     * Checks if the values are recent or not.
     *
     * @return True if the values are not old, false otherwise.
     */
    public boolean isRecentBufferpoolRead() {
        boolean ret = true;
        final long now = System.currentTimeMillis();
        if (now - 2 * BufferpoolRead.BUFFERPOOL_FREQ < this.lastBpRead) {
            ret = false;
        }
        return ret;
    }

    /**
     * Set the bufferpool reads.
     *
     * @param bps
     *            Set of bufferpool reads.
     */
    public void setBufferpools(final Map<String, BufferpoolRead> bps) {
        this.bufferpoolReads = bps;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    @SuppressWarnings("PMD.CommentRequired")
    public String toString() {
        final String ret = this.bufferpoolReads.size()
                + " bufferpoolReads. Last at " + this.lastBpRead;
        return ret;
    }

    /**
     * Updates the value of the last bufferpool read. This is general to all
     * bufferpoolReads because they are updated at the same time.
     */
    public void updateLastBufferpoolRead() {
        this.lastBpRead = System.currentTimeMillis();
    }

}
