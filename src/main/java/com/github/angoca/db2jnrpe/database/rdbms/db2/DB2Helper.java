package com.github.angoca.db2jnrpe.database.rdbms.db2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.github.angoca.db2jnrpe.database.DatabaseConnection;
import com.github.angoca.db2jnrpe.database.DatabaseConnectionException;
import com.github.angoca.db2jnrpe.database.DatabaseConnectionsManager;
import com.github.angoca.db2jnrpe.database.pools.ConnectionPoolsManager;
import com.ibm.db2.jcc.DB2Diagnosable;
import com.ibm.db2.jcc.DB2Sqlca;

/**
 * Process a raised DB2 exception. This code was taken from the DB2 manuals.
 *
 * @author Andres Gomez Casanova (@AngocA)
 * @version 2014-11-03
 */
public abstract class DB2Helper {
    /**
     * Returns the corresponding DB2 version of the database server.
     *
     * @param dbConn
     *            Connection to db2.
     * @return Version of the server.
     * @throws DatabaseConnectionException
     *             If any error occurs while accessing the database.
     */
    public static final DB2MajorVersions getDB2MajorVersion(
            final DatabaseConnection dbConn) throws DatabaseConnectionException {
        DB2MajorVersions version = DB2MajorVersions.UNKNOWN;
        final String queryBefore_v9_7 = "SELECT PROD_RELEASE "
                + "FROM SYSIBMADM.ENV_PROD_INFO";
        final String queryAfter_v9_7 = "SELECT PROD_RELEASE "
                + "FROM TABLE(SYSPROC.ENV_GET_PROD_INFO())";
        Connection connection = null;
        try {
            connection = ConnectionPoolsManager.getInstance()
                    .getConnectionPool(dbConn).getConnection(dbConn);
            PreparedStatement stmt = connection
                    .prepareStatement(queryAfter_v9_7);
            ResultSet res = null;
            try {
                res = stmt.executeQuery();
            } catch (final SQLException sqle) {
                final int code = DB2Helper.getSqlCode(sqle);
                if (code == -440) {
                    stmt = connection.prepareStatement(queryBefore_v9_7);
                    res = stmt.executeQuery();
                } else {
                    throw sqle;
                }
            }

            String versionText;
            while (res.next()) {
                versionText = res.getString(1);
                if (versionText == DB2MajorVersions.V8_1.getName()) {
                    version = DB2MajorVersions.V8_1;
                } else if (versionText.compareTo(DB2MajorVersions.V9_1
                        .getName()) == 0) {
                    version = DB2MajorVersions.V9_1;
                } else if (versionText.compareTo(DB2MajorVersions.V9_5
                        .getName()) == 0) {
                    version = DB2MajorVersions.V9_5;
                } else if (versionText.compareTo(DB2MajorVersions.V9_7
                        .getName()) == 0) {
                    version = DB2MajorVersions.V9_7;
                } else if (versionText.compareTo(DB2MajorVersions.V9_8
                        .getName()) == 0) {
                    version = DB2MajorVersions.V9_8;
                } else if (versionText.compareTo(DB2MajorVersions.V10_1
                        .getName()) == 0) {
                    version = DB2MajorVersions.V10_1;
                } else if (versionText.compareTo(DB2MajorVersions.V10_5
                        .getName()) == 0) {
                    version = DB2MajorVersions.V10_5;
                } else {
                    version = DB2MajorVersions.OTHER;
                }
            }
            res.close();
            stmt.close();
            ConnectionPoolsManager.getInstance().getConnectionPool(dbConn)
                    .closeConnection(dbConn, connection);
        } catch (final SQLException sqle) {
            DB2Helper.processException(sqle);
            throw new DatabaseConnectionException(sqle);
        }
        return version;
    }

    /**
     * Returns the corresponding DB2 version of the database server.
     *
     * @param dbConn
     *            Connection to db2.
     * @return Version of the server.
     * @throws DatabaseConnectionException
     *             If any error occurs while accessing the database.
     */
    public static final DB2MinorVersion getDB2MinorVersion(
            final DatabaseConnection dbConn) throws DatabaseConnectionException {
        DB2MinorVersion version = DB2MinorVersion.UNKNOWN;
        final String queryBefore_v9_7 = "SELECT PROD_RELEASE "
                + "FROM SYSIBMADM.ENV_PROD_INFO";
        final String queryAfter_v9_7 = "SELECT SERVICE_LEVEL "
                + "FROM SYSIBMADM.ENV_INST_INFO";
        Connection connection = null;
        try {
            connection = ConnectionPoolsManager.getInstance()
                    .getConnectionPool(dbConn).getConnection(dbConn);
            PreparedStatement stmt = connection
                    .prepareStatement(queryAfter_v9_7);
            ResultSet res = null;
            try {
                res = stmt.executeQuery();
            } catch (final SQLException sqle) {
                final int code = DB2Helper.getSqlCode(sqle);
                if (code == -440) {
                    stmt = connection.prepareStatement(queryBefore_v9_7);
                    res = stmt.executeQuery();
                } else {
                    throw sqle;
                }
            }

            String versionText;
            while (res.next()) {
                versionText = res.getString(1);
                if (versionText.compareTo(DB2MinorVersion.V9_7_GA.getName()) == 0) {
                    version = DB2MinorVersion.V9_7_GA;
                } else if (versionText.compareTo(DB2MinorVersion.V9_7_1
                        .getName()) == 0) {
                    version = DB2MinorVersion.V9_7_1;
                } else if (versionText.compareTo(DB2MinorVersion.V9_7_2
                        .getName()) == 0) {
                    version = DB2MinorVersion.V9_7_2;
                } else if (versionText.compareTo(DB2MinorVersion.V9_7_3
                        .getName()) == 0) {
                    version = DB2MinorVersion.V9_7_3;
                } else if (versionText.compareTo(DB2MinorVersion.V9_7_4
                        .getName()) == 0) {
                    version = DB2MinorVersion.V9_7_4;
                } else if (versionText.compareTo(DB2MinorVersion.V9_7_5
                        .getName()) == 0) {
                    version = DB2MinorVersion.V9_7_5;
                } else if (versionText.compareTo(DB2MinorVersion.V9_7_6
                        .getName()) == 0) {
                    version = DB2MinorVersion.V9_7_6;
                } else if (versionText.compareTo(DB2MinorVersion.V9_7_7
                        .getName()) == 0) {
                    version = DB2MinorVersion.V9_7_7;
                } else if (versionText.compareTo(DB2MinorVersion.V9_7_8
                        .getName()) == 0) {
                    version = DB2MinorVersion.V9_7_8;
                } else if (versionText.compareTo(DB2MinorVersion.V9_7_9
                        .getName()) == 0) {
                    version = DB2MinorVersion.V9_7_9;
                } else if (versionText.compareTo(DB2MinorVersion.V9_8_GA
                        .getName()) == 0) {
                    version = DB2MinorVersion.V9_8_GA;
                } else if (versionText.compareTo(DB2MinorVersion.V9_8_1
                        .getName()) == 0) {
                    version = DB2MinorVersion.V9_8_1;
                } else if (versionText.compareTo(DB2MinorVersion.V9_8_2
                        .getName()) == 0) {
                    version = DB2MinorVersion.V9_8_2;
                } else if (versionText.compareTo(DB2MinorVersion.V9_8_3
                        .getName()) == 0) {
                    version = DB2MinorVersion.V9_8_3;
                } else if (versionText.compareTo(DB2MinorVersion.V9_8_4
                        .getName()) == 0) {
                    version = DB2MinorVersion.V9_8_4;
                } else if (versionText.compareTo(DB2MinorVersion.V9_8_5
                        .getName()) == 0) {
                    version = DB2MinorVersion.V9_8_5;
                } else if (versionText.compareTo(DB2MinorVersion.V10_1_GA
                        .getName()) == 0) {
                    version = DB2MinorVersion.V10_1_GA;
                } else if (versionText.compareTo(DB2MinorVersion.V10_1_1
                        .getName()) == 0) {
                    version = DB2MinorVersion.V10_1_1;
                } else if (versionText.compareTo(DB2MinorVersion.V10_1_2
                        .getName()) == 0) {
                    version = DB2MinorVersion.V10_1_2;
                } else if (versionText.compareTo(DB2MinorVersion.V10_1_3
                        .getName()) == 0) {
                    version = DB2MinorVersion.V10_1_3;
                } else if (versionText.compareTo(DB2MinorVersion.V10_1_4
                        .getName()) == 0) {
                    version = DB2MinorVersion.V10_1_4;
                } else if (versionText.compareTo(DB2MinorVersion.V10_5_GA
                        .getName()) == 0) {
                    version = DB2MinorVersion.V10_5_GA;
                } else if (versionText.compareTo(DB2MinorVersion.V10_5_1
                        .getName()) == 0) {
                    version = DB2MinorVersion.V10_5_1;
                } else if (versionText.compareTo(DB2MinorVersion.V10_5_2
                        .getName()) == 0) {
                    version = DB2MinorVersion.V10_5_2;
                } else if (versionText.compareTo(DB2MinorVersion.V10_5_3
                        .getName()) == 0) {
                    version = DB2MinorVersion.V10_5_3;
                } else if (versionText.compareTo(DB2MinorVersion.V10_5_4
                        .getName()) == 0) {
                    version = DB2MinorVersion.V10_5_4;
                } else {
                    version = DB2MinorVersion.OTHER;
                }
            }
            res.close();
            stmt.close();
            ConnectionPoolsManager.getInstance().getConnectionPool(dbConn)
                    .closeConnection(dbConn, connection);
        } catch (final SQLException sqle) {
            DB2Helper.processException(sqle);
            throw new DatabaseConnectionException(sqle);
        }
        return version;
    }

    /**
     * Returns the SQL code that is in a SQL exception.
     *
     * @param sqle
     *            Exception to process.
     * @return SQLcode.
     */
    static final int getSqlCode(final SQLException sqle) {
        int ret = 0;
        if (sqle instanceof DB2Diagnosable) {
            final DB2Diagnosable diagnosable = (DB2Diagnosable) sqle;
            final DB2Sqlca sqlca = diagnosable.getSqlca();
            if (sqlca != null) {
                ret = sqlca.getSqlCode();
            }
        }
        return ret;
    }

    /**
     * Tester.
     *
     * @param args
     *            Arguments to pass.
     * @throws Exception
     *             Any exception.
     */
    public static final void main(final String[] args) throws Exception {
        System.out.println("Test: Pool");
        String hostname;
        int portNumber;
        String databaseName;
        String username;
        String password;
        String databaseConnection;
        String connectionPool;
        DatabaseConnection dbConn;

        hostname = "localhost";
        portNumber = 50000;
        databaseName = "sample";
        username = "db2inst1";
        password = "db2inst1";

        databaseConnection = DB2Connection.class.getName();
        // connectionPool =
        // com.github.angoca.db2jnrpe.database.pools.hikari.DbcpHikari.class
        // .getName();
        connectionPool = com.github.angoca.db2jnrpe.database.pools.c3p0.DbcpC3p0.class
                .getName();
        dbConn = DatabaseConnectionsManager.getInstance()
                .getDatabaseConnection(connectionPool, databaseConnection,
                        hostname, portNumber, databaseName, username, password);
        System.out.println("DB2 version: "
                + DB2Helper.getDB2MajorVersion(dbConn) + " :: "
                + DB2Helper.getDB2MinorVersion(dbConn));

        hostname = "127.0.0.1";
        portNumber = 50002;
        databaseName = "sample";
        username = "db2inst2";
        password = "db2inst2";

        databaseConnection = DB2Connection.class.getName();
        // connectionPool =
        // com.github.angoca.db2jnrpe.database.pools.hikari.DbcpHikari.class
        // .getName();
        connectionPool = com.github.angoca.db2jnrpe.database.pools.c3p0.DbcpC3p0.class
                .getName();
        dbConn = DatabaseConnectionsManager.getInstance()
                .getDatabaseConnection(connectionPool, databaseConnection,
                        hostname, portNumber, databaseName, username, password);
        System.out.println("DB2 version: "
                + DB2Helper.getDB2MajorVersion(dbConn) + " :: "
                + DB2Helper.getDB2MinorVersion(dbConn));
    }

    /**
     * Process a given SQLException generated by DB2.
     *
     * @param sqle
     *            Exception to process.
     */
    public static final void processException(SQLException sqle) {
        // Check whether there are more SQLExceptions to process
        while (sqle != null) {
            if (sqle instanceof DB2Diagnosable) {
                // Check if IBM Data Server Driver for JDBC and SQLJ-only
                // information exists
                final DB2Diagnosable diagnosable = (DB2Diagnosable) sqle;
                // PrintWriter printWriter = new PrintWriter(System.out, true);
                // diagnosable.printTrace(printWriter, "");
                final java.lang.Throwable throwable = diagnosable
                        .getThrowable();
                if (throwable != null) {
                    // Extract java.lang.Throwable information
                    // such as message or stack trace.
                    throwable.printStackTrace();
                }
                // Get DB2Sqlca object
                final DB2Sqlca sqlca = diagnosable.getSqlca();
                if (sqlca != null) { // Check that DB2Sqlca is not null
                    // Get the SQL error code
                    final int sqlCode = sqlca.getSqlCode();
                    // Get the entire SQLERRMC
                    final String sqlErrmc = sqlca.getSqlErrmc();
                    // You can also retrieve the individual SQLERRMC tokens
                    final String[] sqlErrmcTokens = sqlca.getSqlErrmcTokens();
                    // Get the SQLERRP
                    final String sqlErrp = sqlca.getSqlErrp();
                    // Get SQLERRD fields
                    final int[] sqlErrd = sqlca.getSqlErrd();
                    // Get SQLWARN fields
                    final char[] sqlWarn = sqlca.getSqlWarn();
                    // Get SQLSTATE
                    final String sqlState = sqlca.getSqlState();
                    // Get error message
                    String errMessage;
                    try {
                        errMessage = sqlca.getMessage();
                    } catch (final SQLException e) {
                        errMessage = "Exception while getting message";
                    }
                    System.err.println("Server error message: " + errMessage);
                    System.err.println("--------------- SQLCA ---------------");
                    System.err.println("Error code: " + sqlCode);
                    System.err.println("SQLERRMC: " + sqlErrmc);
                    if (sqlErrmcTokens != null) {
                        for (int i = 0; i < sqlErrmcTokens.length; i++) {
                            System.err.println(" token " + i + ": "
                                    + sqlErrmcTokens[i]);
                        }
                    }
                    System.err.println("SQLERRP: " + sqlErrp);
                    System.err.println("SQLERRD(1): " + sqlErrd[0] + "\n"
                            + "SQLERRD(2): " + sqlErrd[1] + "\n"
                            + "SQLERRD(3): " + sqlErrd[2] + "\n"
                            + "SQLERRD(4): " + sqlErrd[3] + "\n"
                            + "SQLERRD(5): " + sqlErrd[4] + "\n"
                            + "SQLERRD(6): " + sqlErrd[5]);
                    if (sqlWarn[0] != ' ') {
                        System.err.println("SQLWARN1: '" + sqlWarn[0] + "'");
                    }
                    if (sqlWarn[0] != ' ') {
                        System.err.println("SQLWARN2: '" + sqlWarn[1] + "'");
                    }
                    if (sqlWarn[0] != ' ') {
                        System.err.println("SQLWARN3: '" + sqlWarn[2] + "'");
                    }
                    if (sqlWarn[0] != ' ') {
                        System.err.println("SQLWARN4: '" + sqlWarn[3] + "'");
                    }
                    if (sqlWarn[0] != ' ') {
                        System.err.println("SQLWARN5: '" + sqlWarn[4] + "'");
                    }
                    if (sqlWarn[0] != ' ') {
                        System.err.println("SQLWARN6: '" + sqlWarn[5] + "'");
                    }
                    if (sqlWarn[0] != ' ') {
                        System.err.println("SQLWARN7: '" + sqlWarn[6] + "'");
                    }
                    if (sqlWarn[0] != ' ') {
                        System.err.println("SQLWARN8: '" + sqlWarn[7] + "'");
                    }
                    if (sqlWarn[0] != ' ') {
                        System.err.println("SQLWARN9: '" + sqlWarn[8] + "'");
                    }
                    if (sqlWarn[0] != ' ') {
                        System.err.println("SQLWARNA: '" + sqlWarn[9] + "'");
                    }
                    System.err.println("SQLSTATE: " + sqlState);
                    // portion of SQLException
                } else {
                    System.err.println(sqle.getMessage());
                }
                // Retrieve next SQLException
                sqle = sqle.getNextException();
            }
        }
    }
}
