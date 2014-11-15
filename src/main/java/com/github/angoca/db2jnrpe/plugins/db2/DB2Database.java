package com.github.angoca.db2jnrpe.plugins.db2;

import java.util.HashMap;
import java.util.Map;

import com.github.angoca.db2jnrpe.database.DatabaseConnection;
import com.github.angoca.db2jnrpe.database.DatabaseConnectionException;

/**
 * Models a database with its connection URL.
 *
 * @author Andres Gomez Casanova (@AngocA)
 * @version 2014-11-03
 */
public class DB2Database {
    /**
     * Frequency to read the bufferpools. 30000 means each 5 minutes.
     */
    private static final long BUFFERPOOL_FREQUENCY = 300000;
    /**
     * Hash of bufferpools reads.
     */
    private Map<String, BufferpoolRead> bufferpools;
    /**
     * Identification of the database.
     */
    private final String id;
    /**
     * Time of the last bufferpools read.
     */
    private long lastBufferpoolRead = 0;

    /**
     * Creates a database with an ID.
     * 
     * @param dbId
     *            Unique id to identify this database.
     */
    public DB2Database(final String dbId) {
        this.id = dbId;
        this.bufferpools = new HashMap<String, BufferpoolRead>();
        this.lastBufferpoolRead = 0;
    }

    /**
     * Clone the set of bufferpools.
     *
     * @return Copy of the set of bufferpools.
     */
    private Map<String, BufferpoolRead> cloneBufferpools() {
        final Map<String, BufferpoolRead> copy = new HashMap<String, BufferpoolRead>();
        for (final String key : this.bufferpools.keySet()) {
            final BufferpoolRead clone = this.bufferpools.get(key).clone();
            copy.put(key, clone);
        }
        return copy;
    }

    /**
     * Retrieves the map of bufferpools.
     *
     * @return Map of bufferpools.
     */
    final Map<String, BufferpoolRead> getBufferpools() {
        return this.bufferpools;
    }

    /**
     * Retrieves the map of bufferpools and update the information async for the
     * next call.
     *
     * @param dbConn
     *            Connection properties.
     * @return Map of bufferpools.
     * @throws DatabaseConnectionException
     *             If there is a problem while updating the values.
     * @throws UnknownValueException
     *             If the bufferpool values have not been read.
     */
    final Map<String, BufferpoolRead> getBufferpoolsAndRefresh(
            final DatabaseConnection dbConn)
            throws DatabaseConnectionException, UnknownValueException {
        if (this.lastBufferpoolRead == 0) {
            new Thread(new CheckBufferPoolHitRatioDB2(dbConn, this)).start();
            throw new UnknownValueException(
                    "Bufferpool values have not been read");
        } else if (!this.isBufferpoolListUpdated()) {
            // Updates for the next time. The current execution returns the
            // previous values.
            new Thread(new CheckBufferPoolHitRatioDB2(dbConn, this)).start();
        }
        return this.cloneBufferpools();
    }

    /**
     * Returns the ID of the database.
     *
     * @return ID of the database.
     */
    final String getId() {
        return this.id;
    }

    /**
     * Retrieves the last time the bufferpools reads were updated.
     *
     * @return Time of last read.
     */
    final long getLastRefresh() {
        return this.lastBufferpoolRead;
    }

    /**
     * Checks if the list of bufferpools should be updated.
     *
     * @return True if the list is too old or never set. False otherwise.
     */
    private boolean isBufferpoolListUpdated() {
        boolean ret = true;
        final long now = System.currentTimeMillis();
        if (this.lastBufferpoolRead == 0) {
            // Never set.
            ret = false;
        } else if ((now - DB2Database.BUFFERPOOL_FREQUENCY) > this.lastBufferpoolRead) {
            ret = false;
        }
        return ret;
    }

    /**
     * Adds a bufferpool read to the database.
     * 
     * @param bpr
     *            Bufferpool read.
     */
    final void addBufferpoolRead(final BufferpoolRead bpr) {
        this.bufferpools.put(bpr.getName(), bpr);
    }

    /**
     * Updates the value of the last bufferpool read. This is general to all
     * bufferpools because they are updated at the same time.
     */
    final void updateLastBufferpoolRead() {
        this.lastBufferpoolRead = System.currentTimeMillis();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public final String toString() {
        final String ret = this.id + " with " + this.bufferpools.size()
                + " bufferpools. Last at " + this.lastBufferpoolRead;
        return ret;
    }
}
