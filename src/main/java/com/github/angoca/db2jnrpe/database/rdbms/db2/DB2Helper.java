package com.github.angoca.db2jnrpe.database.rdbms.db2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.angoca.db2jnrpe.database.AbstractDatabaseConnection;
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
@SuppressWarnings("PMD.CommentSize")
public final class DB2Helper {
    /**
     * Represents a blankspace.
     */
    private static final char BLANKSPACE = ' ';
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(DB2Helper.class);
    /**
     * Routine was not found SQL0440.
     */
    private static final int NO_ROUTINE = -440;

    /**
     * Returns the corresponding DB2 version of the database server.
     *
     * @param dbConn
     *            Connection to db2.
     * @return Version of the server.
     * @throws DatabaseConnectionException
     *             If any error occurs while accessing the database.
     */
    public static DB2MajorVersion getDB2MajorVersion(
            final AbstractDatabaseConnection dbConn)
            throws DatabaseConnectionException {
        DB2MajorVersion version = DB2MajorVersion.UNKNOWN;
        final String queryBeforeV97 = "SELECT PROD_RELEASE "
                + "FROM SYSIBMADM.ENV_PROD_INFO";
        final String queryAfterV97 = "SELECT PROD_RELEASE "
                + "FROM TABLE(SYSPROC.ENV_GET_PROD_INFO())";
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        try {
            connection = ConnectionPoolsManager.getInstance()
                    .getConnectionPool(dbConn).getConnection(dbConn);
            stmt = connection.prepareStatement(queryAfterV97);
            try {
                res = stmt.executeQuery();
            } catch (final SQLException sqle) {
                final int code = DB2Helper.getSqlCode(sqle);
                if (code == DB2Helper.NO_ROUTINE) {
                    stmt.close();
                    stmt = connection.prepareStatement(queryBeforeV97);
                    res = stmt.executeQuery();
                } else {
                    throw sqle;
                }
            }

            String versionText;
            while (res.next()) {
                versionText = res.getString(1);
                if (versionText.compareTo(DB2MajorVersion.V8_1.getName()) == 0) {
                    version = DB2MajorVersion.V8_1;
                } else if (versionText
                        .compareTo(DB2MajorVersion.V9_1.getName()) == 0) {
                    version = DB2MajorVersion.V9_1;
                } else if (versionText
                        .compareTo(DB2MajorVersion.V9_5.getName()) == 0) {
                    version = DB2MajorVersion.V9_5;
                } else if (versionText
                        .compareTo(DB2MajorVersion.V9_7.getName()) == 0) {
                    version = DB2MajorVersion.V9_7;
                } else if (versionText
                        .compareTo(DB2MajorVersion.V9_8.getName()) == 0) {
                    version = DB2MajorVersion.V9_8;
                } else if (versionText.compareTo(DB2MajorVersion.V10_1
                        .getName()) == 0) {
                    version = DB2MajorVersion.V10_1;
                } else if (versionText.compareTo(DB2MajorVersion.V10_5
                        .getName()) == 0) {
                    version = DB2MajorVersion.V10_5;
                } else {
                    version = DB2MajorVersion.OTHER;
                }
            }
        } catch (final SQLException sqle) {
            DB2Helper.processException(sqle);
            throw new DatabaseConnectionException(sqle);
        } finally {
            try {
                if (res != null) {
                    res.close();
                }
            } catch (SQLException e) {
                throw new DatabaseConnectionException(e);
            }
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException e) {
                throw new DatabaseConnectionException(e);
            }
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                throw new DatabaseConnectionException(e);
            }
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
    public static DB2MinorVersion getDB2MinorVersion(
            final AbstractDatabaseConnection dbConn)
            throws DatabaseConnectionException {
        DB2MinorVersion version = DB2MinorVersion.UNKNOWN;
        final String queryBeforeV97 = "SELECT PROD_RELEASE "
                + "FROM SYSIBMADM.ENV_PROD_INFO";
        final String queryAfterV97 = "SELECT SERVICE_LEVEL "
                + "FROM SYSIBMADM.ENV_INST_INFO";
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        try {
            connection = ConnectionPoolsManager.getInstance()
                    .getConnectionPool(dbConn).getConnection(dbConn);
            stmt = connection.prepareStatement(queryAfterV97);
            try {
                res = stmt.executeQuery();
            } catch (final SQLException sqle) {
                final int code = DB2Helper.getSqlCode(sqle);
                if (code == DB2Helper.NO_ROUTINE) {
                    stmt.close();
                    stmt = connection.prepareStatement(queryBeforeV97);
                    res = stmt.executeQuery();
                } else {
                    throw sqle;
                }
            }
            version = DB2Helper.processValue(res);
        } catch (final SQLException sqle) {
            DB2Helper.processException(sqle);
            throw new DatabaseConnectionException(sqle);
        } finally {
            try {
                if (res != null) {
                    res.close();
                }
            } catch (SQLException e) {
                throw new DatabaseConnectionException(e);
            }
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException e) {
                throw new DatabaseConnectionException(e);
            }
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                throw new DatabaseConnectionException(e);
            }
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
    public static int getSqlCode(final SQLException sqle) {
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
    @SuppressWarnings("PMD")
    public static void main(final String[] args) throws Exception {
        // CHECKSTYLE:OFF
        System.out.println("Test: Pool");
        String hostname;
        int portNumber;
        String databaseName;
        String username;
        String password;
        String databaseConnection;
        String connectionPool;
        AbstractDatabaseConnection dbConn;

        hostname = "localhost";
        portNumber = 50000;
        databaseName = "sample";
        username = "db2inst1";
        password = "db2inst1";

        databaseConnection = DB2Connection.class.getName();
        connectionPool = com.github.angoca.db2jnrpe.database.pools.hikari.DbcpHikari.class
                .getName();
        // connectionPool =
        // com.github.angoca.db2jnrpe.database.pools.c3p0.DbcpC3p0.class
        // .getName();
        dbConn = DatabaseConnectionsManager.getInstance()
                .getDatabaseConnection(connectionPool, databaseConnection,
                        hostname, portNumber, databaseName, username, password);
        System.out.println("DB2 version: "
                + DB2Helper.getDB2MajorVersion(dbConn) + " :: "
                + DB2Helper.getDB2MinorVersion(dbConn));

        hostname = "127.0.0.1";
        portNumber = 50000;
        databaseName = "sample2";
        username = "db2inst1";
        password = "db2inst1";

        databaseConnection = DB2Connection.class.getName();
        connectionPool = com.github.angoca.db2jnrpe.database.pools.hikari.DbcpHikari.class
                .getName();
        // connectionPool =
        // com.github.angoca.db2jnrpe.database.pools.c3p0.DbcpC3p0.class
        // .getName();
        dbConn = DatabaseConnectionsManager.getInstance()
                .getDatabaseConnection(connectionPool, databaseConnection,
                        hostname, portNumber, databaseName, username, password);
        System.out.println("DB2 version: "
                + DB2Helper.getDB2MajorVersion(dbConn) + " :: "
                + DB2Helper.getDB2MinorVersion(dbConn));
        // CHECKSTYLE:ON
    }

    /**
     * Process a given SQLException generated by DB2.
     *
     * @param exexception
     *            Exception to process.
     */
    @SuppressWarnings("PMD.GuardLogStatement")
    public static void processException(final SQLException exexception) {
        SQLException sqle = exexception;
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
                    DB2Helper.LOGGER.error("Processing exception", throwable);
                }
                // Get DB2Sqlca object
                final DB2Sqlca sqlca = diagnosable.getSqlca();
                if (sqlca == null) { // Check that DB2Sqlca is null
                    DB2Helper.LOGGER.error(sqle.getMessage());
                } else {
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
                    DB2Helper.LOGGER.error("Server error message: "
                            + errMessage);
                    DB2Helper.LOGGER
                            .error("--------------- SQLCA ---------------");
                    DB2Helper.LOGGER.error("Error code: " + sqlCode);
                    DB2Helper.LOGGER.error("SQLERRMC: " + sqlErrmc);
                    if (sqlErrmcTokens != null) {
                        for (int i = 0; i < sqlErrmcTokens.length; i++) {
                            DB2Helper.LOGGER.error(" token " + i + ": "
                                    + sqlErrmcTokens[i]);
                        }
                    }
                    DB2Helper.LOGGER.error("SQLERRP: " + sqlErrp);
                    DB2Helper.LOGGER.error("SQLERRD(1): " + sqlErrd[0] + "\n"
                            + "SQLERRD(2): " + sqlErrd[1] + "\n"
                            + "SQLERRD(3): " + sqlErrd[2] + "\n"
                            + "SQLERRD(4): " + sqlErrd[3] + "\n"
                            + "SQLERRD(5): " + sqlErrd[4] + "\n"
                            + "SQLERRD(6): " + sqlErrd[5]);
                    DB2Helper.processWarning(sqlWarn);
                    DB2Helper.LOGGER.error("SQLSTATE: " + sqlState);
                    // portion of SQLException
                }
                // Retrieve next SQLException
                sqle = sqle.getNextException();
            }
        }
    }

    /**
     * Process the values of the result set.
     *
     * @param res
     *            Result set:
     * @return The corresponding DB2 minor version.
     * @throws SQLException
     *             If there is any error processing the loop.
     */
    private static DB2MinorVersion processValue(final ResultSet res)
            throws SQLException {
        DB2MinorVersion version = DB2MinorVersion.UNKNOWN;
        String versionText;
        while (res.next()) {
            versionText = res.getString(1);
            if (versionText.compareTo(DB2MinorVersion.V9_7_GA.getName()) == 0) {
                version = DB2MinorVersion.V9_7_GA;
            } else if (versionText.compareTo(DB2MinorVersion.V9_7_1.getName()) == 0) {
                version = DB2MinorVersion.V9_7_1;
            } else if (versionText.compareTo(DB2MinorVersion.V9_7_2.getName()) == 0) {
                version = DB2MinorVersion.V9_7_2;
            } else if (versionText.compareTo(DB2MinorVersion.V9_7_3.getName()) == 0) {
                version = DB2MinorVersion.V9_7_3;
            } else if (versionText.compareTo(DB2MinorVersion.V9_7_4.getName()) == 0) {
                version = DB2MinorVersion.V9_7_4;
            } else if (versionText.compareTo(DB2MinorVersion.V9_7_5.getName()) == 0) {
                version = DB2MinorVersion.V9_7_5;
            } else if (versionText.compareTo(DB2MinorVersion.V9_7_6.getName()) == 0) {
                version = DB2MinorVersion.V9_7_6;
            } else if (versionText.compareTo(DB2MinorVersion.V9_7_7.getName()) == 0) {
                version = DB2MinorVersion.V9_7_7;
            } else if (versionText.compareTo(DB2MinorVersion.V9_7_8.getName()) == 0) {
                version = DB2MinorVersion.V9_7_8;
            } else if (versionText.compareTo(DB2MinorVersion.V9_7_9.getName()) == 0) {
                version = DB2MinorVersion.V9_7_9;
            } else if (versionText.compareTo(DB2MinorVersion.V9_8_GA.getName()) == 0) {
                version = DB2MinorVersion.V9_8_GA;
            } else if (versionText.compareTo(DB2MinorVersion.V9_8_1.getName()) == 0) {
                version = DB2MinorVersion.V9_8_1;
            } else if (versionText.compareTo(DB2MinorVersion.V9_8_2.getName()) == 0) {
                version = DB2MinorVersion.V9_8_2;
            } else if (versionText.compareTo(DB2MinorVersion.V9_8_3.getName()) == 0) {
                version = DB2MinorVersion.V9_8_3;
            } else if (versionText.compareTo(DB2MinorVersion.V9_8_4.getName()) == 0) {
                version = DB2MinorVersion.V9_8_4;
            } else if (versionText.compareTo(DB2MinorVersion.V9_8_5.getName()) == 0) {
                version = DB2MinorVersion.V9_8_5;
            } else if (versionText
                    .compareTo(DB2MinorVersion.V10_1_GA.getName()) == 0) {
                version = DB2MinorVersion.V10_1_GA;
            } else if (versionText.compareTo(DB2MinorVersion.V10_1_1.getName()) == 0) {
                version = DB2MinorVersion.V10_1_1;
            } else if (versionText.compareTo(DB2MinorVersion.V10_1_2.getName()) == 0) {
                version = DB2MinorVersion.V10_1_2;
            } else if (versionText.compareTo(DB2MinorVersion.V10_1_3.getName()) == 0) {
                version = DB2MinorVersion.V10_1_3;
            } else if (versionText.compareTo(DB2MinorVersion.V10_1_4.getName()) == 0) {
                version = DB2MinorVersion.V10_1_4;
            } else if (versionText
                    .compareTo(DB2MinorVersion.V10_5_GA.getName()) == 0) {
                version = DB2MinorVersion.V10_5_GA;
            } else if (versionText.compareTo(DB2MinorVersion.V10_5_1.getName()) == 0) {
                version = DB2MinorVersion.V10_5_1;
            } else if (versionText.compareTo(DB2MinorVersion.V10_5_2.getName()) == 0) {
                version = DB2MinorVersion.V10_5_2;
            } else if (versionText.compareTo(DB2MinorVersion.V10_5_3.getName()) == 0) {
                version = DB2MinorVersion.V10_5_3;
            } else if (versionText.compareTo(DB2MinorVersion.V10_5_4.getName()) == 0) {
                version = DB2MinorVersion.V10_5_4;
            } else {
                version = DB2MinorVersion.OTHER;
            }
        }
        return version;
    }

    /**
     * Process a single warning.
     * 
     * @param sqlWarn
     *            Array that contains all warnings.
     * @param index
     *            Position of the warning to analyze.
     */
    @SuppressWarnings("PMD.GuardLogStatement")
    private static void processWarning(final char[] sqlWarn, final int index) {
        if (sqlWarn[index] != DB2Helper.BLANKSPACE) {
            DB2Helper.LOGGER.error("SQLWARN" + (index + 1) + ": '"
                    + sqlWarn[index] + "'");
        }
    }

    /**
     * Process the warning part of the DB2CA.
     *
     * @param sqlWarn
     *            Warning part.
     */
    @SuppressWarnings({ "PMD.GuardLogStatement", "PMD.LawOfDemeter" })
    private static void processWarning(final char[] sqlWarn) {
        for (int i = 0; i < 9; i++) {
            processWarning(sqlWarn, i);
        }
        if (sqlWarn[9] != DB2Helper.BLANKSPACE) {
            DB2Helper.LOGGER.error("SQLWARNA: '" + sqlWarn[9] + "'");
        }
    }

    /**
     * Nothing.
     */
    private DB2Helper() {
        // Nothing.
    }
}
