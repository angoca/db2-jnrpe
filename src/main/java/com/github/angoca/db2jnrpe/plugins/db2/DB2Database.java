package com.github.angoca.db2jnrpe.plugins.db2;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.angoca.db2jnrpe.database.DatabaseConnection;

/**
 * Models a database with its connection URL.
 *
 * @author Andres Gomez Casanova (@AngocA)
 * @version 2014-11-03
 */
public class DB2Database {
    /**
     * Normal frequency for all elements.
     */
    private static final long STANDARD_FREQUENCY = 300000;
    /**
     * Frequency to read the bufferpools. 30000 means each 5 minutes.
     */
    private static final long BUFFERPOOL_FREQUENCY = STANDARD_FREQUENCY;
    /**
     * Load frequency to calculate the load.
     */
    private static final long LOAD_FREQUENCY = STANDARD_FREQUENCY;
    /**
     * Name of the Update/Insert/Delete metric.
     */
    public static final String UID_LOAD = "Uid";
    /**
     * Name of the select metric.
     */
    public static final String SELECT_LOAD = "Select";
    /**
     * Name of the commit metric.
     */
    public static final String COMMIT_LOAD = "Commit";
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
     * Time of the last load read.
     */
    private long lastLoadRead = 0;
    /**
     * List of load database values.
     */
    private Map<String, Long> loadValues;
    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(DB2Database.class);

    /**
     * Creates a database with an ID.
     * 
     * @param dbId
     *            Unique id to identify this database.
     */
    public DB2Database(final String dbId) {
        this.id = dbId;
        this.bufferpools = new HashMap<String, BufferpoolRead>();
        this.loadValues = new HashMap<String, Long>();
        log.debug("New database " + dbId);
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
     * Set the bufferpool reads.
     * 
     * @param bps
     *            Set of bufferpool reads.
     */
    final void setBufferpools(final Map<String, BufferpoolRead> bps) {
        this.bufferpools = bps;
    }

    /**
     * Retrieves the map of bufferpools and update the information async for the
     * next call.
     *
     * @param dbConn
     *            Connection properties.
     * @return Map of bufferpools.
     * @throws UnknownValueException
     *             If the bufferpool values have not been read.
     */
    public final Map<String, BufferpoolRead> getBufferpoolsAndRefresh(
            final DatabaseConnection dbConn) throws UnknownValueException {
        if (this.lastBufferpoolRead == 0) {
            new Thread(new CheckBufferPoolHitRatioDB2(dbConn, this)).start();
            throw new UnknownValueException(
                    "Bufferpool values have not been read");
        } else if (!this.isBufferpoolListUpdated()) {
            // Updates for the next time. The current execution returns the
            // previous values.
            new Thread(new CheckBufferPoolHitRatioDB2(dbConn, this)).start();
        }
        log.info(this.id + "::Bufferpool values returned taken at "
                + new Timestamp(this.lastBufferpoolRead));
        return this.cloneBufferpools();
    }

    /**
     * Retrieves the values of the load. The keys are the following three
     * constant:<br/>
     * <ul>
     * <li>UID_LOAD</li>
     * <li>SELECT_LOAD</li>
     * <li>COMMIT_LOAD</li>
     * </ul>
     * 
     * @param dbConn
     *            Connection properties.
     * @return The map that contains the load values.
     * @throws UnknownValueException
     *             If the values have not been read.
     */
    public final Map<String, Long> getLoadValuesAndRefresh(
            final DatabaseConnection dbConn) throws UnknownValueException {
        if (this.lastLoadRead == 0) {
            // TODO
            new Thread().start();
            throw new UnknownValueException("Load values have not been read");
        } else if (!this.isLoadValuesUpdate()) {
            // Updates for the next time. The current execution returns the
            // previous values.
            // TODO
            new Thread().start();
        }
        log.info(this.id + "::Load values returned taken at "
                + new Timestamp(this.lastBufferpoolRead));
        return this.cloneLoadValues();
    }

    /**
     * Clone the set of load values.
     * 
     * @return Copy of the set of values.
     */
    private Map<String, Long> cloneLoadValues() {
        final Map<String, Long> copy = new HashMap<String, Long>();
        for (final String key : this.loadValues.keySet()) {
            final long clone = this.loadValues.get(key);
            copy.put(key, clone);
        }
        return copy;
    }

    /**
     * Checks if the load values should be updated.
     * 
     * @return True if the list of load values is outdated or it is still valid.
     */
    private boolean isLoadValuesUpdate() {
        boolean ret = true;
        final long now = System.currentTimeMillis();
        if (this.lastLoadRead == 0) {
            // Never set.
            ret = false;
        } else if ((now - DB2Database.LOAD_FREQUENCY) > this.lastBufferpoolRead) {
            ret = false;
        }
        return ret;
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
    public final long getLastBufferpoolRefresh() {
        return this.lastBufferpoolRead;
    }

    /**
     * Checks if the values are recent or not.
     * 
     * @return True if the values are not old, false otherwise.
     */
    public final boolean isRecentBufferpoolRead() {
        boolean ret = true;
        final long now = System.currentTimeMillis();
        if ((now - (2 * DB2Database.BUFFERPOOL_FREQUENCY)) < this.lastBufferpoolRead) {
            ret = false;
        }
        return ret;
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
