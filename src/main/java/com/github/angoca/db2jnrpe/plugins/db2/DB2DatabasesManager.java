package com.github.angoca.db2jnrpe.plugins.db2;

import java.util.HashMap;
import java.util.Map;

/**
 * Controls the DB2Databases. A database is a representation of the objects
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
    @SuppressWarnings("PMD.FieldDeclarationsShouldBeAtStartOfClass")
    private final transient Map<String, DB2Database> db2Databases;

    /**
     * Creates the singleton.
     */
    private DB2DatabasesManager() {
        this.db2Databases = new HashMap<String, DB2Database>();
    }

    /**
     * Adds a database in the set.
     *
     * @param identification
     *            ID to identify the database.
     * @param db2Database
     *            DB2Database.
     */
    public void add(final String identification, final DB2Database db2Database) {
        this.db2Databases.put(identification, db2Database);
    }

    /**
     * Retrieves a database given its ID.
     *
     * @param identification
     *            Id of the database.
     * @return The database that corresponds to the given ID.
     */
    public DB2Database getDatabase(final String identification) {
        return this.db2Databases.get(identification);
    }

    /**
     * Retrieves all db2Databases.
     *
     * @return Map of db2Databases.
     */
    public Map<String, DB2Database> getDatabases() {
        return this.db2Databases;
    }
}
