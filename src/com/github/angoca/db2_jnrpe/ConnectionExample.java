package com.github.angoca.db2_jnrpe;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ConnectionExample {

    public static void main(String[] args) throws PropertyVetoException {

        Connection connection = null;
        String query = "WITH BPMETRICS AS (SELECT BP_NAME, "
                + " POOL_DATA_L_READS + POOL_TEMP_DATA_L_READS + "
                + " POOL_XDA_L_READS + POOL_TEMP_XDA_L_READS + "
                + " POOL_INDEX_L_READS + POOL_TEMP_INDEX_L_READS "
                + " AS LOGICAL_READS, "
                + " POOL_DATA_P_READS + POOL_TEMP_DATA_P_READS + "
                + " POOL_INDEX_P_READS + POOL_TEMP_INDEX_P_READS + "
                + " POOL_XDA_P_READS + POOL_TEMP_XDA_P_READS "
                + " AS PHYSICAL_READS, MEMBER "
                + " FROM TABLE(MON_GET_BUFFERPOOL('', -2)) AS METRICS "
                + " WHERE BP_NAME NOT LIKE 'IBMSYSTEMBP%') "
                + "SELECT BP_NAME, LOGICAL_READS, PHYSICAL_READS, "
                + "CASE WHEN LOGICAL_READS > 0 "
                + " THEN DEC((1 - (FLOAT(PHYSICAL_READS) "
                + " / FLOAT(LOGICAL_READS))) * 100, 5, 2) ELSE NULL "
                + "END AS HIT_RATIO, MEMBER FROM BPMETRICS";
        try {
            // Establish connection
            connection = DBBroker_c3p0.getInstance().getConnection();
            PreparedStatement stmt = connection.prepareStatement(query);
            ResultSet res = stmt.executeQuery();

            while (res.next()) {
                System.out.println(res.getString(1));
                System.out.println(res.getInt(2));
                System.out.println(res.getInt(3));
                System.out.println(res.getFloat(4));
                System.out.println(res.getInt(5));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}