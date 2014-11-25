package com.github.angoca.db2jnrpe.plugins.db2.broker;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.angoca.db2jnrpe.database.DatabaseConnection;
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
    private static final Map<String, Integer> LOCKS = new HashMap<String, Integer>();
    /**
     * Logger.
     */
    private static Logger log = LoggerFactory
            .getLogger(AbstractDB2Broker.class);
    /**
     * DB2 database.
     */
    private DB2Database db2db;
    /**
     * Connection properties.
     */
    private DatabaseConnection dbConn;

    /**
     * Performs the query.
     *
     * @throws DatabaseConnectionException
     *             If any problem occur while accessing the database.
     */
    protected abstract void check() throws DatabaseConnectionException;

    /**
     * Retrieves the database that contains the values of the ojects.
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
    protected final DatabaseConnection getDatabaseConnection() {
        return this.dbConn;
    }

    /**
     * Sets the database that contains the values.
     *
     * @param db
     *            Database.
     */
    protected final void setDB2database(final DB2Database db) {
        this.db2db = db;
    }

    /**
     * Sets the object that contains the connection properties.
     *
     * @param conn
     *            Connection properties.
     */
    protected final void setDBConnection(final DatabaseConnection conn) {
        this.dbConn = conn;
    }

    /**
     * Sets the a lock of this execution to allow just one execution at the
     * time.
     */
    protected final void setLock() {
        try {
            // Controls multiple concurrent executions.
            // This prevents to create multiple threads trying to access the
            // database. This is a problem when the database is not available,
            // or it has a big workload, and multiple connections are
            // established.
            if (!AbstractDB2Broker.LOCKS.containsKey(this.db2db.getId())) {
                AbstractDB2Broker.LOCKS.put(this.db2db.getId(), 1);
                this.check();
                AbstractDB2Broker.LOCKS.remove(this.db2db.getId());
            } else {
                AbstractDB2Broker.log.warn(this.dbConn.getUrl()
                        + "::There is a lock for: " + this.db2db.getId());
            }
        } catch (final Exception e) {
            AbstractDB2Broker.log.error(this.dbConn.getUrl()
                    + "::Error while reading bufferpool values", e);
            AbstractDB2Broker.LOCKS.remove(this.db2db.getId());
            e.printStackTrace();
        }
    }
}