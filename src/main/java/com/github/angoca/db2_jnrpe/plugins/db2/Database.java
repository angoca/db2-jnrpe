package com.github.angoca.db2_jnrpe.plugins.db2;

import java.util.HashMap;
import java.util.Map;

/**
 * Models a database with its connection URL.
 *
 * @author Andres Gomez Casanova (@AngocA)
 * @version 2014-11-03
 */
public class Database {
    /**
     * Frequency to read the bufferpools. 30000 means each 5 minutes.
     */
    private static final long BUFFERPOOL_FREQUENCY = 30000;
    /**
     * Connection URL to identify a database.
     */
    private String url;
    /**
     * Hash of bufferpools reads.
     */
    private Map<String, BufferpoolRead> bufferpools;
    /**
     * Time of the last bufferpools read.
     */
    private long lastBufferpoolRead;

    public Database(final String url) {
        this.url = url;
        this.bufferpools = new HashMap<String, BufferpoolRead>();
        this.lastBufferpoolRead = 0;
    }

    /**
     * Returns the URL of the database.
     * 
     * @return URL of the database.
     */
    String getURL() {
        return this.url;
    }

    /**
     * Retrieves the map of bufferpools.
     * 
     * @return Map of bufferpools.
     */
    Map<String, BufferpoolRead> getBufferpools() {
        return this.bufferpools;
    }

    /**
     * Sets a new set of bufferpools reads.
     * 
     * @param bufferpoolReads
     *            Bufferpool reads.
     */
    void setBufferpoolReads(final Map<String, BufferpoolRead> bufferpoolReads) {
        this.bufferpools = bufferpoolReads;
        this.lastBufferpoolRead = System.currentTimeMillis();
    }

    /**
     * Retrieves the last time the bufferpools reads were updated.
     * 
     * @return Time of last read.
     */
    long getLastRefresh() {
        return this.lastBufferpoolRead;
    }

    /**
     * Checks if the list of bufferpools should be updated.
     * 
     * @return True if the list is too old or never set. False otherwise.
     */
    boolean isBufferpoolListUpdated() {
        boolean ret = true;
        final long now = System.currentTimeMillis();
        if (this.lastBufferpoolRead == 0) {
            // Never set.
            ret = false;
        } else if (now - Database.BUFFERPOOL_FREQUENCY > this.lastBufferpoolRead) {
            ret = false;
        }
        System.out.println(ret);
        return ret;
    }
}
