package com.github.angoca.db2jnrpe.plugins.db2;

import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.angoca.db2jnrpe.database.AbstractDatabaseConnection;
import com.github.angoca.db2jnrpe.plugins.db2.broker.DB2BufferpoolHitRatioBroker;
import com.github.angoca.db2jnrpe.plugins.db2.broker.DB2DatabaseSnapshotBroker;

/**
 * Models a database with its connection URL.
 *
 * @author Andres Gomez Casanova (@AngocA)
 * @version 2014-11-03
 */
@SuppressWarnings("PMD.CommentSize")
public final class DB2Database {
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(DB2Database.class);
    /**
     * Normal frequency for all elements: 10 minutes.
     */
    public static final long STANDARD_FREQ = 600000;
    /**
     * Bufferpool reads.
     */
    private Bufferpools bufferpools;
    /**
     * Identification of the database.
     */
    private final transient String identification;
    /**
     * Snapshot of the database.
     */
    private DatabaseSnapshot snap;

    /**
     * Creates a database with an ID.
     *
     * @param dbId
     *            Unique identification to identify this database.
     */
    public DB2Database(final String dbId) {
        this.identification = dbId;

        if (DB2Database.LOGGER.isDebugEnabled()) {
            DB2Database.LOGGER.debug("New database " + dbId);
        }
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
     * Retrieves the map of bufferpoolReads and update the information for the
     * next call in an asynchronous way.
     *
     * @param dbConn
     *            Connection properties.
     * @return Map of bufferpoolReads.
     * @throws UnknownValueException
     *             If the bufferpool values have not been read.
     */
    public Bufferpools getBufferpoolsAndRefresh(
            final AbstractDatabaseConnection dbConn)
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
        DB2Database.LOGGER.info("Bufferpool values returned taken at {}",
                new Timestamp(this.bufferpools.getLastBufferpoolRefresh()));
        return (Bufferpools) this.bufferpools.clone();
    }

    /**
     * Returns the ID of the database.
     *
     * @return ID of the database.
     */
    public String getId() {
        return this.identification;
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
            final AbstractDatabaseConnection dbConn)
            throws UnknownValueException {
        if (this.snap == null) {
            new Thread(new DB2DatabaseSnapshotBroker(dbConn, this)).start();
            throw new UnknownValueException("First snapshot has not been read");
        } else if (!this.snap.isSnapshotUpdated()) {
            // Updates for the next time. The current execution returns the
            // previous values.
            new Thread(new DB2DatabaseSnapshotBroker(dbConn, this)).start();
        }
        DB2Database.LOGGER.info("Snapshot returned taken at {}", new Timestamp(
                this.snap.getLastSnapshotRefresh()));
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
    @SuppressWarnings("PMD.CommentRequired")
    public String toString() {
        return this.identification;
    }

}
