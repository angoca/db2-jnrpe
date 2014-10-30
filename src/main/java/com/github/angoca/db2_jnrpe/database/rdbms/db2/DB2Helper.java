package com.github.angoca.db2_jnrpe.database.rdbms.db2;

import java.sql.SQLException;

import com.github.angoca.db2_jnrpe.database.DatabaseConnection;
import com.github.angoca.db2_jnrpe.database.DatabaseConnectionsManager;
import com.github.angoca.db2_jnrpe.database.pools.c3p0.DBBroker_c3p0;
import com.ibm.db2.jcc.DB2Diagnosable;
import com.ibm.db2.jcc.DB2Sqlca;

public abstract class DB2Helper {
    static int getSQLCode(final SQLException sqle) {
        int ret = 0;
        if (sqle instanceof DB2Diagnosable) {
            DB2Diagnosable diagnosable = (DB2Diagnosable) sqle;
            DB2Sqlca sqlca = diagnosable.getSqlca();
            if (sqlca != null) {
                ret = sqlca.getSqlCode();
            }
        }
        return ret;
    }

    public static void main(final String[] args) {
        System.out.println("Test: c3p0 Pool");
        String hostname = "localhost";
        int portNumber = 50000;
        String databaseName = "sample";
        String username = "db2inst1";
        String password = "db2inst1";

        String databaseConnection = DB2Connection.class.getName();
        String connectionPool = DBBroker_c3p0.class.getName();
        DatabaseConnection dbConn = DatabaseConnectionsManager.getInstance()
                .getDatabaseConnection(connectionPool, databaseConnection,
                        hostname, portNumber, databaseName, username, password);
        System.out.println("DB2 version: " + DB2Versions.getDB2Version(dbConn));
    }

    public static void processException(SQLException sqle) {
        // Check whether there are more SQLExceptions to process
        while (sqle != null) {
            if (sqle instanceof DB2Diagnosable) {
                // Check if IBM Data Server Driver for JDBC and SQLJ-only
                // information exists
                DB2Diagnosable diagnosable = (DB2Diagnosable) sqle;
                // PrintWriter printWriter = new PrintWriter(System.out, true);
                // diagnosable.printTrace(printWriter, "");
                java.lang.Throwable throwable = diagnosable.getThrowable();
                if (throwable != null) {
                    // Extract java.lang.Throwable information
                    // such as message or stack trace.
                    throwable.printStackTrace();
                }
                // Get DB2Sqlca object
                DB2Sqlca sqlca = diagnosable.getSqlca();
                if (sqlca != null) { // Check that DB2Sqlca is not null
                    // Get the SQL error code
                    int sqlCode = sqlca.getSqlCode();
                    // Get the entire SQLERRMC
                    String sqlErrmc = sqlca.getSqlErrmc();
                    // You can also retrieve the individual SQLERRMC tokens
                    String[] sqlErrmcTokens = sqlca.getSqlErrmcTokens();
                    // Get the SQLERRP
                    String sqlErrp = sqlca.getSqlErrp();
                    // Get SQLERRD fields
                    int[] sqlErrd = sqlca.getSqlErrd();
                    // Get SQLWARN fields
                    char[] sqlWarn = sqlca.getSqlWarn();
                    // Get SQLSTATE
                    String sqlState = sqlca.getSqlState();
                    // Get error message
                    String errMessage;
                    try {
                        errMessage = sqlca.getMessage();
                    } catch (SQLException e) {
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
