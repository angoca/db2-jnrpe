package com.github.angoca.db2jnrpe.database;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Controls the constructors for the different Database Connection. Each RDBMS
 * can have a different properties to connect to the database, and this class
 * wraps these properties to pass them to the connection pool.
 *
 * @author Andres Gomez Casanova (@AngocA)
 * @version 2014-11-03
 */
@SuppressWarnings("PMD.CommentSize")
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
    public static DatabaseConnectionsManager getInstance() {
        if (DatabaseConnectionsManager.instance == null) {
            DatabaseConnectionsManager.instance = new DatabaseConnectionsManager();
        }
        return DatabaseConnectionsManager.instance;
    }

    /**
     * Set of connections to the database.
     */
    @SuppressWarnings("PMD.FieldDeclarationsShouldBeAtStartOfClass")
    private final transient Map<String, AbstractDatabaseConnection> connectionProps;

    /**
     * Constructors to access the database.
     */
    @SuppressWarnings("PMD.FieldDeclarationsShouldBeAtStartOfClass")
    private final transient Map<String, Constructor<AbstractDatabaseConnection>> constructors;

    /**
     * Properties to connect to the databases.
     */
    @SuppressWarnings("PMD.FieldDeclarationsShouldBeAtStartOfClass")
    private final transient Properties defaultProperties;

    /**
     * Creates the singleton instance.
     */
    public DatabaseConnectionsManager() {
        this.connectionProps = new HashMap<String, AbstractDatabaseConnection>();
        this.constructors = new HashMap<String, Constructor<AbstractDatabaseConnection>>();
        this.defaultProperties = new Properties();
    }

    /**
     * Returns a constructor for a given classname.
     *
     * @param dbConnName
     *            Name of the class to instantiate.
     * @return Constructor of the class.
     * @throws DatabaseConnectionException
     *             If any error occurs while retrieving the constructor.
     */
    @SuppressWarnings("unchecked")
    private Constructor<AbstractDatabaseConnection> getConstructor(
            final String dbConnName) throws DatabaseConnectionException {
        Constructor<AbstractDatabaseConnection> constructor = this.constructors
                .get(dbConnName);
        if (constructor == null) {
            Class<?> clazz;
            try {
                clazz = Class.forName(dbConnName);
                constructor = (Constructor<AbstractDatabaseConnection>) clazz
                        .getConstructor(String.class, Properties.class,
                                String.class, Integer.TYPE, String.class,
                                String.class, String.class);
                this.constructors.put(dbConnName, constructor);
                constructor = this.constructors.get(dbConnName);
            } catch (final ClassNotFoundException e) {
                throw new DatabaseConnectionException(e);
            } catch (final NoSuchMethodException e) {
                throw new DatabaseConnectionException(e);
            } catch (final SecurityException e) {
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
     * @param dbConnn
     *            Name of the class that contains the properties for the
     *            database.
     * @param hostname
     *            Name of the server or IP.
     * @param portNumber
     *            Port of the instance.
     * @param databaseName
     *            DB2Database name.
     * @param username
     *            Connection user.
     * @param password
     *            Password.
     * @return An object that contains all related properties of the connection.
     * @throws DatabaseConnectionException
     *             If any error occurs that instantiating the constructor.
     */
    public AbstractDatabaseConnection getDatabaseConnection(
            final String connectionsPool, final String dbConnn,
            final String hostname, final int portNumber,
            final String databaseName, final String username,
            final String password) throws DatabaseConnectionException {
        final String connKey = username + '@' + hostname + ':' + portNumber
                + '/' + databaseName;
        AbstractDatabaseConnection dbConn = this.connectionProps.get(connKey);
        if (dbConn == null || dbConn.getPassword().compareTo(password) != 0) {
            final Constructor<AbstractDatabaseConnection> constructor = this
                    .getConstructor(dbConnn);
            try {
                dbConn = constructor.newInstance(connectionsPool,
                        this.defaultProperties, hostname, portNumber,
                        databaseName, username, password);
                this.connectionProps.put(connKey, dbConn);
            } catch (final InstantiationException e) {
                throw new DatabaseConnectionException(e);
            } catch (final IllegalAccessException e) {
                throw new DatabaseConnectionException(e);
            } catch (final IllegalArgumentException e) {
                throw new DatabaseConnectionException(e);
            } catch (final InvocationTargetException e) {
                throw new DatabaseConnectionException(e);
            }
        }
        return dbConn;
    }
}
