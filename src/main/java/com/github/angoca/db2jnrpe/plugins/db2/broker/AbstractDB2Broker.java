package com.github.angoca.db2jnrpe.plugins.db2.broker;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.angoca.db2jnrpe.database.AbstractDatabaseConnection;
import com.github.angoca.db2jnrpe.database.DatabaseConnectionException;
import com.github.angoca.db2jnrpe.plugins.db2.DB2Database;

/**
 * Abstract class to define the structure of a broker.
 *
 * @author Andres Gomez Casanova (@AngocA)
 * @version 2014-11-24
 */
public abstract class AbstractDB2Broker {

    /**
     * Prevent multiple concurrent executions.
     */
    private static final ConcurrentMap<String, Integer> LOCKS = new ConcurrentHashMap<String, Integer>();
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(AbstractDB2Broker.class);

    /**
     * Checks if there is a lock for the given key (there is already an
     * execution in process for the same database).
     *
     * @param key
     *            Key that identifies the database.
     * @return True if there is an execution for the database. False otherwise.
     */
    private static boolean hasLock(final String key) {
        return AbstractDB2Broker.LOCKS.containsKey(key);
    }

    /**
     * Puts a lock for the given key. This means an execution for the database
     * has started.
     *
     * @param key
     *            Key that identifies the database.
     */
    private static void putLock(final String key) {
        AbstractDB2Broker.LOCKS.put(key, 1);
    }

    /**
     * Removes the lock for the given key that represents a database. This means
     * the execution is finished.
     *
     * @param key
     *            Key that identifies the database.
     */
    private static void removeLock(final String key) {
        AbstractDB2Broker.LOCKS.remove(key);
    }

    /**
     * DB2 database.
     */
    @SuppressWarnings("PMD.FieldDeclarationsShouldBeAtStartOfClass")
    private transient DB2Database db2db;

    /**
     * Connection properties.
     */
    @SuppressWarnings("PMD.FieldDeclarationsShouldBeAtStartOfClass")
    private transient AbstractDatabaseConnection dbConn;

    /**
     * Empty constructor.
     */
    protected AbstractDB2Broker() {
        // Empty.
    }

    /**
     * Performs the query.
     *
     * @throws DatabaseConnectionException
     *             If any problem occur while accessing the database.
     */
    protected abstract void check() throws DatabaseConnectionException;

    /**
     * Retrieves the database that contains the values of the objects.
     *
     * @return DB.
     */
    protected final DB2Database getDatabase() {
        return this.db2db;
    }

    /**
     * Retrieves the properties of the connection.
     *
     * @return Connection properties.
     */
    protected final AbstractDatabaseConnection getDatabaseConnection() {
        return this.dbConn;
    }

    /**
     * Sets the database that contains the values.
     *
     * @param database
     *            Database.
     */
    protected final void setDB2database(final DB2Database database) {
        this.db2db = database;
    }

    /**
     * Sets the object that contains the connection properties.
     *
     * @param conn
     *            Connection properties.
     */
    protected final void setDBConnection(final AbstractDatabaseConnection conn) {
        this.dbConn = conn;
    }

    /**
     * Sets the a lock of this execution to allow just one execution at the
     * time.
     */
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    protected final void setLock() {
        final String key = this.db2db.getId();
        final String url = this.dbConn.getUrl();
        try {
            // Controls multiple concurrent executions.
            // This prevents to create multiple threads trying to access the
            // database. This is a problem when the database is not available,
            // or it has a big workload, and multiple connections are
            // established.
            if (AbstractDB2Broker.hasLock(key)) {
                AbstractDB2Broker.LOGGER.warn("{}::There is a lock for: ", url,
                        key);
            } else {
                AbstractDB2Broker.putLock(key);
                this.check();
                AbstractDB2Broker.removeLock(key);
            }
        } catch (final Exception e) {
            AbstractDB2Broker.LOGGER.error(
                    "{}::Error while reading bufferpool values", url, e);
            AbstractDB2Broker.removeLock(key);
        }
    }
}