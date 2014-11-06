package com.github.angoca.db2_jnrpe.plugins.db2;

import java.util.HashMap;
import java.util.Map;

/**
 * Controls the databases.
 *
 * @author Andres Gomez Casanova (@AngocA)
 * @version 2014-11-03
 */
public class DatabasesManager {
    /**
     * Singleton instance.
     */
    private static DatabasesManager singleton;

    /**
     * Retrieves the only instance.
     *
     * @return Singleton.
     */
    static DatabasesManager getInstance() {
        if (DatabasesManager.singleton == null) {
            DatabasesManager.singleton = new DatabasesManager();
        }
        return DatabasesManager.singleton;
    }

    /**
     * List of databases.
     */
    private final Map<String, Database> databases;

    /**
     * Creates the singleton.
     */
    private DatabasesManager() {
        this.databases = new HashMap<String, Database>();
    }

    /**
     * Adds a database in the set.
     *
     * @param url
     *            Unique URL to identify this database.
     * @param database
     *            Database.
     */
    public void add(final String url, final Database database) {
        this.databases.put(url, database);
    }

    /**
     * Retrieves a database given its URL.
     *
     * @param name
     *            URL of the database.
     * @return The database that corresponds to the given url.
     */
    Database getDatabase(final String url) {
        return this.databases.get(url);
    }

    /**
     * Retrieves all databases.
     *
     * @return Map of databases.
     */
    Map<String, Database> getDatabases() {
        return this.databases;
    }
}
