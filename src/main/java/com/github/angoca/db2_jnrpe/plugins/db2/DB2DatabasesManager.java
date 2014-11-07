package com.github.angoca.db2_jnrpe.plugins.db2;

import java.util.HashMap;
import java.util.Map;

/**
 * Controls the DB2Databases. A database is a respresentation of the objects
 * associates to a DB2 database. Objects can be tablespaces, bufferpool, etc.
 *
 * @author Andres Gomez Casanova (@AngocA)
 * @version 2014-11-03
 */
public class DB2DatabasesManager {
    /**
     * Singleton instance.
     */
    private static DB2DatabasesManager singleton;

    /**
     * Retrieves the only instance.
     *
     * @return Singleton.
     */
    static DB2DatabasesManager getInstance() {
        if (DB2DatabasesManager.singleton == null) {
            DB2DatabasesManager.singleton = new DB2DatabasesManager();
        }
        return DB2DatabasesManager.singleton;
    }

    /**
     * List of dB2Databases.
     */
    private final Map<String, DB2Database> dB2Databases;

    /**
     * Creates the singleton.
     */
    private DB2DatabasesManager() {
        this.dB2Databases = new HashMap<String, DB2Database>();
    }

    /**
     * Adds a database in the set.
     *
     * @param url
     *            Unique URL to identify this database.
     * @param dB2Database
     *            DB2Database.
     */
    public void add(final String url, final DB2Database dB2Database) {
        this.dB2Databases.put(url, dB2Database);
    }

    /**
     * Retrieves a database given its URL.
     *
     * @param name
     *            URL of the database.
     * @return The database that corresponds to the given url.
     */
    DB2Database getDatabase(final String url) {
        return this.dB2Databases.get(url);
    }

    /**
     * Retrieves all dB2Databases.
     *
     * @return Map of dB2Databases.
     */
    Map<String, DB2Database> getDatabases() {
        return this.dB2Databases;
    }
}
