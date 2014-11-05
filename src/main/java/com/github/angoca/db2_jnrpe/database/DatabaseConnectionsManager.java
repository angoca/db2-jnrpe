package com.github.angoca.db2_jnrpe.database;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Controls the connections.
 * 
 * @author Andres Gomez Casanova (@AngocA)
 * @version 2014-11-03
 */
public final class DatabaseConnectionsManager {
    /**
     * Singleton instance.
     */
    private static DatabaseConnectionsManager instance;

    /**
     * Returns the singleton.
     * 
     * @return Singleton instance.
     */
    public final static DatabaseConnectionsManager getInstance() {
        if (instance == null) {
            instance = new DatabaseConnectionsManager();
        }
        return instance;
    }

    /**
     * Set of connections to the database.
     */
    private final Map<String, DatabaseConnection> connectionProps;

    /**
     * Constructors to access the database.
     */
    private final Map<String, Constructor<DatabaseConnection>> constructors;

    /**
     * Properties to connect to the databases.
     */
    protected final Properties defaultProperties;

    /**
     * Creates the singleton instance.
     */
    public DatabaseConnectionsManager() {
        this.connectionProps = new HashMap<String, DatabaseConnection>();
        this.constructors = new HashMap<String, Constructor<DatabaseConnection>>();
        this.defaultProperties = new Properties();
    }

    /**
     * Returns a constructor for a given classname.
     * 
     * @param databaseConnectionName
     *            Name of the class to instantiate.
     * @return Constructor of the class.
     * @throws DatabaseConnectionException
     *             If any error occurs while retrieving the constructor.
     */
    @SuppressWarnings("unchecked")
    private final Constructor<DatabaseConnection> getConstructor(
            final String databaseConnectionName)
            throws DatabaseConnectionException {
        Constructor<DatabaseConnection> constructor = this.constructors
                .get(databaseConnectionName);
        if (constructor == null) {
            Class<?> clazz;
            try {
                clazz = Class.forName(databaseConnectionName);
                constructor = (Constructor<DatabaseConnection>) clazz
                        .getConstructor(String.class, Properties.class,
                                String.class, Integer.TYPE, String.class,
                                String.class, String.class);
                this.constructors.put(databaseConnectionName, constructor);
                constructor = this.constructors.get(databaseConnectionName);
            } catch (ClassNotFoundException e) {
                throw new DatabaseConnectionException(e);
            } catch (NoSuchMethodException e) {
                throw new DatabaseConnectionException(e);
            } catch (SecurityException e) {
                throw new DatabaseConnectionException(e);
            }
        }
        return constructor;
    }

    /**
     * Returns a connection object with all the parameters inside after using
     * the related constructor.
     * 
     * @param connectionsPool
     *            Associated connection pool.
     * @param databaseConnection
     * @param hostname
     *            Name of the server or IP.
     * @param portNumber
     *            Port of the instance.
     * @param databaseName
     *            Database name.
     * @param username
     *            Connection user.
     * @param password
     *            Password.
     * @return An object that contains all related properties of the connection.
     * @throws DatabaseConnectionException
     *             If any error occurs that instantiating the constructor.
     */
    public final DatabaseConnection getDatabaseConnection(
            final String connectionsPool, final String databaseConnection,
            String hostname, final int portNumber, final String databaseName,
            final String username, final String password)
            throws DatabaseConnectionException {
        String connKey = username + '@' + hostname + ':' + portNumber + '/'
                + databaseName;
        DatabaseConnection dbConn = this.connectionProps.get(connKey);
        if (dbConn == null || dbConn.getPassword().compareTo(password) != 0) {
            Constructor<DatabaseConnection> constructor = getConstructor(databaseConnection);
            try {
                dbConn = constructor.newInstance(connectionsPool,
                        defaultProperties, hostname, portNumber, databaseName,
                        username, password);
                this.connectionProps.put(connKey, dbConn);
            } catch (InstantiationException e) {
                throw new DatabaseConnectionException(e);
            } catch (IllegalAccessException e) {
                throw new DatabaseConnectionException(e);
            } catch (IllegalArgumentException e) {
                throw new DatabaseConnectionException(e);
            } catch (InvocationTargetException e) {
                throw new DatabaseConnectionException(e);
            }
        }
        return dbConn;
    }
}
