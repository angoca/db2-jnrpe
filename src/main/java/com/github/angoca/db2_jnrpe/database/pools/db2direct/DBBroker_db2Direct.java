package com.github.angoca.db2_jnrpe.database.pools.db2direct;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import com.github.angoca.db2_jnrpe.database.DatabaseConnection;
import com.github.angoca.db2_jnrpe.database.DatabaseConnectionException;
import com.github.angoca.db2_jnrpe.database.pools.ConnectionPool;
import com.github.angoca.db2_jnrpe.database.rdbms.db2.DB2Connection;

/**
 * Connection pool using direct DB2 driver.
 * 
 * @author Andres Gomez Casanova (@AngocA)
 * @version 2014-11-04
 */
public class DBBroker_db2Direct extends ConnectionPool {

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.github.angoca.db2_jnrpe.database.pools.ConnectionPool#closeConnection
     * (com.github.angoca.db2_jnrpe.database.DatabaseConnection)
     */
    @Override
    public void closeConnection(DatabaseConnection dbConn)
            throws DatabaseConnectionException {
        // Nothing
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.github.angoca.db2_jnrpe.database.pools.ConnectionPool#getConnection
     * (com.github.angoca.db2_jnrpe.database.DatabaseConnection)
     */
    @Override
    public Connection getConnection(DatabaseConnection dbConn)
            throws DatabaseConnectionException {
        Connection connection = null;
        if (dbConn instanceof DB2Connection) {
            DB2Connection db2conn = (DB2Connection) dbConn;
            try {
                Class.forName("com.ibm.db2.jcc.DB2Driver");
                Properties props = db2conn.getConnectionProperties();
                connection = DriverManager.getConnection(db2conn.getURL(),
                        props);
            } catch (Exception e) {
                throw new DatabaseConnectionException(e);
            }
        } else {
            throw new DatabaseConnectionException(new Exception(
                    "Invalid connection properties (DatabaseConnection)"));
        }
        return connection;
    }
}
