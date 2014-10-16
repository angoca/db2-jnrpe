package com.github.angoca.db2_jnrpe.database;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class DatabaseConnectionsManager {
    private static DatabaseConnectionsManager instance;

    public static DatabaseConnectionsManager getInstance() {
        if (instance == null) {
            instance = new DatabaseConnectionsManager();
        }
        return instance;
    }

    final private Map<String, DatabaseConnection> connectionProps;

    final private Map<String, Constructor<DatabaseConnection>> constructors;

    final protected Properties defaultProperties;

    public DatabaseConnectionsManager() {
        this.connectionProps = new HashMap<String, DatabaseConnection>();
        this.constructors = new HashMap<String, Constructor<DatabaseConnection>>();
        this.defaultProperties = new Properties();
    }

    @SuppressWarnings("unchecked")
    private Constructor<DatabaseConnection> getConstructor(
            final String databaseConnectionName) {
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
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (SecurityException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return constructor;
    }

    public DatabaseConnection getDatabaseConnection(
            final String connectionsPool, final String databaseConnection,
            String hostname, final int portNumber, final String databaseName,
            final String username, final String password) {
        String connKey = hostname + ':' + portNumber + '/' + databaseName;
        DatabaseConnection dbConn = this.connectionProps.get(connKey);
        if (dbConn == null) {
            Constructor<DatabaseConnection> constructor = getConstructor(databaseConnection);
            try {
                dbConn = constructor.newInstance(connectionsPool,
                        defaultProperties, hostname, portNumber, databaseName,
                        username, password);
            } catch (InstantiationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            this.connectionProps.put(connKey, dbConn);
        }
        return dbConn;
    }
}
