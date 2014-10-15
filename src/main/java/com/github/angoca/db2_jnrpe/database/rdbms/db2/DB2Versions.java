package com.github.angoca.db2_jnrpe.database.rdbms.db2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.github.angoca.db2_jnrpe.database.DatabaseConnection;
import com.github.angoca.db2_jnrpe.database.pools.c3p0.DBBroker_c3p0;

public enum DB2Versions {
    UNKNOWN(null, 0), V8_2("8.1", 81), V9_1("9.1", 91), V9_5("9.5", 95), V9_7(
            "9.7", 97), V9_8("9.8", 98), V10_1("10.1", 101), V10_5("10.5", 105), OTHER(
            "", 999);

    private final String db2Connection = "DB2Connection";
    private final String name;
    private final int value;

    private DB2Versions(final String name, final int value) {
        this.name = name;
        this.value = value;
    }

    String getName() {
        return this.name;
    }

    private int getValue() {
        return this.value;
    }

    public boolean isEqualOrMoreRecentThan(final DB2Versions version) {
        boolean ret = false;
        if (this.getValue() >= version.getValue()) {
            ret = true;
        }
        return ret;
    }

    public static DB2Versions getDB2Version(final DatabaseConnection dbConn) {
        DB2Versions version = DB2Versions.UNKNOWN;
        String queryBefore_v9_7 = "SELECT PROD_RELEASE "
                + "FROM SYSIBMADM.ENV_PROD_INFO";
        String queryAfter_v9_7 = "SELECT PROD_RELEASE "
                + "FROM TABLE(SYSPROC.ENV_GET_PROD_INFO())";
        Connection connection = null;
        try {
            connection = DBBroker_c3p0.getInstance().getConnection(dbConn);
            PreparedStatement stmt = connection
                    .prepareStatement(queryAfter_v9_7);
            ResultSet res = null;
            try {
                res = stmt.executeQuery();
            } catch (SQLException sqle) {
                int code = DB2Helper.getSQLCode(sqle);
                if (code == -440) {
                    stmt = connection.prepareStatement(queryBefore_v9_7);
                    res = stmt.executeQuery();
                } else {
                    throw sqle;
                }
            }

            String version_text;
            while (res.next()) {
                version_text = res.getString(1);
                if (version_text == DB2Versions.V8_2.getName()) {
                    version = DB2Versions.V8_2;
                } else if (version_text.compareTo(DB2Versions.V9_1.getName()) == 0) {
                    version = DB2Versions.V9_1;
                } else if (version_text.compareTo(DB2Versions.V9_5.getName()) == 0) {
                    version = DB2Versions.V9_5;
                } else if (version_text.compareTo(DB2Versions.V9_7.getName()) == 0) {
                    version = DB2Versions.V9_7;
                } else if (version_text.compareTo(DB2Versions.V9_8.getName()) == 0) {
                    version = DB2Versions.V9_8;
                } else if (version_text.compareTo(DB2Versions.V10_1.getName()) == 0) {
                    version = DB2Versions.V10_1;
                } else if (version_text.compareTo(DB2Versions.V10_5.getName()) == 0) {
                    version = DB2Versions.V10_5;
                } else {
                    version = DB2Versions.OTHER;
                }
            }
            res.close();
            stmt.close();
            DBBroker_c3p0.getInstance().closeConnection(dbConn);
        } catch (SQLException sqle) {
            DB2Helper.processException(sqle);
        }
        return version;
    }

    public String getDatabaseConnection() {
        return this.db2Connection;
    }
}

