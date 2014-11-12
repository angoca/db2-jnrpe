package com.github.angoca.db2jnrpe.database.pools;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import com.github.angoca.db2jnrpe.database.DatabaseConnection;
import com.github.angoca.db2jnrpe.database.DatabaseConnectionException;

/**
 * Controls the singleton instance for the pool managers. There could be
 * multiple pool managers (c3p0, Hikari, etc.) and this class control the
 * instance for each pool.
 *
 * @author Andres Gomez Casanova (@AngocA)
 * @version 2014-11-03
 */
public final class ConnectionPoolsManager {
    /**
     * Singleton instance.
     */
    private static ConnectionPoolsManager instance;

    /**
     * Instantiate and returns the singleton.
     *
     * @return Returns the singleton instance.
     */
    public static ConnectionPoolsManager getInstance() {
        if (ConnectionPoolsManager.instance == null) {
            ConnectionPoolsManager.instance = new ConnectionPoolsManager();
        }
        return ConnectionPoolsManager.instance;
    }

    /**
     * List of connection pools, matching the name with itself.
     */
    private final Map<String, ConnectionPool> connectionPools;

    /**
     * Creates the singleton.
     */
    private ConnectionPoolsManager() {
        this.connectionPools = new HashMap<String, ConnectionPool>();
    }

    /**
     * Retrieves a connection from the connection pool.
     *
     * @param dbConn
     *            Properties for the connection.
     * @return Connection pool.
     * @throws DatabaseConnectionException
     *             Any exception is wrapped in this exception.
     */
    public  ConnectionPool getConnectionPool(
            final DatabaseConnection dbConn) throws DatabaseConnectionException {
        final String poolName = dbConn.getConnectionsPoolName();
        ConnectionPool connectionPool = this.connectionPools.get(poolName);
        if (connectionPool == null) {
            final Class<?> clazz;
            try {
                clazz = Class.forName(poolName);
            } catch (final ClassNotFoundException e) {
                throw new DatabaseConnectionException(e);
            }
            try {
                connectionPool = (ConnectionPool) clazz.getConstructor()
                        .newInstance();
            } catch (final NoSuchMethodException e) {
                throw new DatabaseConnectionException(e);
            } catch (final SecurityException e) {
                throw new DatabaseConnectionException(e);
            } catch (final IllegalAccessException e) {
                throw new DatabaseConnectionException(e);
            } catch (final IllegalArgumentException e) {
                throw new DatabaseConnectionException(e);
            } catch (final InvocationTargetException e) {
                throw new DatabaseConnectionException(e);
            } catch (final InstantiationException e) {
                throw new DatabaseConnectionException(e);
            }
            connectionPool.initialize(dbConn);
            this.connectionPools.put(poolName, connectionPool);
        }
        return connectionPool;
    }
}
