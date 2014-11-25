package com.github.angoca.db2jnrpe.plugins.db2;

import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.angoca.db2jnrpe.database.DatabaseConnection;
import com.github.angoca.db2jnrpe.plugins.db2.broker.DB2BufferpoolHitRatioBroker;
import com.github.angoca.db2jnrpe.plugins.db2.broker.DB2DatabaseSnapshotBroker;

/**
 * Models a database with its connection URL.
 *
 * @author Andres Gomez Casanova (@AngocA)
 * @version 2014-11-03
 */
public final class DB2Database {
    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(DB2Database.class);
    /**
     * Normal frequency for all elements.
     */
    static final long STANDARD_FREQUENCY = 300000;
    /**
     * Bufferpool reads.
     */
    private Bufferpools bufferpools;
    /**
     * Identification of the database.
     */
    private final String id;
    /**
     * Snapshot of the database.
     */
    private DatabaseSnapshot snap;

    /**
     * Creates a database with an ID.
     *
     * @param dbId
     *            Unique id to identify this database.
     */
    public DB2Database(final String dbId) {
        this.id = dbId;

        DB2Database.log.debug("New database " + dbId);
    }

    /**
     * Returns the object that represents the bufferpool reads.
     *
     * @return Bufferpool reads.
     */
    public Bufferpools getBufferpools() {
        return this.bufferpools;
    }

    /**
     * Retrieves the map of bufferpoolReads and update the information async for
     * the next call.
     *
     * @param dbConn
     *            Connection properties.
     * @return Map of bufferpoolReads.
     * @throws UnknownValueException
     *             If the bufferpool values have not been read.
     */
    public Bufferpools getBufferpoolsAndRefresh(final DatabaseConnection dbConn)
            throws UnknownValueException {
        if (this.bufferpools == null) {
            new Thread(new DB2BufferpoolHitRatioBroker(dbConn, this)).start();
            throw new UnknownValueException(
                    "Bufferpool values have not been read");
        } else if (!this.bufferpools.isBufferpoolListUpdated()) {
            // Updates for the next time. The current execution returns the
            // previous values.
            new Thread(new DB2BufferpoolHitRatioBroker(dbConn, this)).start();
        }
        log.info("Bufferpool values returned taken at "
                + new Timestamp(this.bufferpools.getLastBufferpoolRefresh()));
        return (Bufferpools) this.bufferpools.clone();
    }

    /**
     * Returns the ID of the database.
     *
     * @return ID of the database.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Retrieves the snapshot associated with this database.
     *
     * @return Snap of the database.
     */
    public DatabaseSnapshot getSnap() {
        return this.snap;
    }

    /**
     * Retrieves the snapshot and refresh if necessary.
     *
     * @param dbConn
     *            Connection properties.
     * @return Object that contains all values from snapshot.
     * @throws UnknownValueException
     *             If the values have not been read.
     */
    public DatabaseSnapshot getSnapshotAndRefresh(
            final DatabaseConnection dbConn) throws UnknownValueException {
        if (this.snap == null) {
            new Thread(new DB2DatabaseSnapshotBroker(dbConn, this)).start();
            throw new UnknownValueException("Snapshot has not been read");
        } else if (!this.snap.isSnapshotUpdated()) {
            // Updates for the next time. The current execution returns the
            // previous values.
            new Thread(new DB2DatabaseSnapshotBroker(dbConn, this)).start();
        }
        log.info("Snapshot returned taken at "
                + new Timestamp(this.snap.getLastSnapshotRefresh()));
        return this.snap.clone();
    }

    /**
     * Sets the object that holds the reads.
     *
     * @param bps
     *            Set of reads.
     */
    public void setBufferpools(final Bufferpools bps) {
        this.bufferpools = bps;
    }

    /**
     * Retrieves the snapshot associated with this database.
     *
     * @param snapshot
     *            Snap of the database.
     */
    public void setSnap(final DatabaseSnapshot snapshot) {
        this.snap = snapshot;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final String ret = this.id;
        return ret;
    }

}
