package com.github.angoca.db2jnrpe.database.rdbms.db2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import com.github.angoca.db2jnrpe.database.AbstractDatabaseConnection;

/**
 * Connection tester.
 *
 * @author Andres Gomez Casanova (@AngocA)
 * @version 2014-11-03
 */
public final class TestDB2Connection {

    /**
     * Tester.
     *
     * @param args
     *            Arguments
     * @throws Exception
     *             Any exception.
     */
    @SuppressWarnings("PMD")
    public static void main(final String[] args) throws Exception {
        System.out.println("Test: Connection");
        final AbstractDatabaseConnection dbConn = new DB2Connection("",
                new Properties(), "localhost", 50000, "sample", "db2inst1",
                "db2inst1");
        Class.forName(dbConn.getDriverClass());
        final Connection conn = DriverManager.getConnection(dbConn.getUrl(),
                dbConn.getUsername(), dbConn.getPassword());
        System.out.println("Client Information: " + conn.getClientInfo());
        conn.close();
    }

    /**
     * Empty constructor.
     */
    private TestDB2Connection() {
        // Nothing.
    }
}
