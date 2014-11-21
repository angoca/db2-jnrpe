package com.github.angoca.db2jnrpe.plugins.db2;

import java.util.HashMap;
import java.util.Map;

/**
 * Controls the DB2Databases. A database is a respresentation of the objects
 * associates to a DB2 database. Objects can be tablespaces, bufferpool, etc.
 *
 * @author Andres Gomez Casanova (@AngocA)
 * @version 2014-11-03
 */
public final class DB2DatabasesManager {
    /**
     * Singleton instance.
     */
    private static DB2DatabasesManager singleton;

    /**
     * Retrieves the only instance.
     *
     * @return Singleton.
     */
    public static DB2DatabasesManager getInstance() {
        if (DB2DatabasesManager.singleton == null) {
            DB2DatabasesManager.singleton = new DB2DatabasesManager();
        }
        return DB2DatabasesManager.singleton;
    }

    /**
     * List of db2Databases.
     */
    private final Map<String, DB2Database> db2Databases;

    /**
     * Creates the singleton.
     */
    private DB2DatabasesManager() {
        this.db2Databases = new HashMap<String, DB2Database>();
    }

    /**
     * Adds a database in the set.
     *
     * @param id
     *            ID to identify the database.
     * @param db2Database
     *            DB2Database.
     */
    public void add(final String id, final DB2Database db2Database) {
        this.db2Databases.put(id, db2Database);
    }

    /**
     * Retrieves a database given its ID.
     *
     * @param id
     *            Id of the database.
     * @return The database that corresponds to the given ID.
     */
    public DB2Database getDatabase(final String id) {
        final DB2Database db = this.db2Databases.get(id);
        return db;
    }

    /**
     * Retrieves all db2Databases.
     *
     * @return Map of db2Databases.
     */
    Map<String, DB2Database> getDatabases() {
        return this.db2Databases;
    }
}
