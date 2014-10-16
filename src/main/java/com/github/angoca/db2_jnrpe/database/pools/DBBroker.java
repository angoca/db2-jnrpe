package com.github.angoca.db2_jnrpe.database.pools;

import java.sql.Connection;
import java.sql.SQLException;

import com.github.angoca.db2_jnrpe.database.DatabaseConnection;

public abstract class DBBroker {

    public abstract void closeConnection(final DatabaseConnection dbConn)
            throws SQLException;

    public abstract Connection getConnection(final DatabaseConnection dbConn)
            throws SQLException;
}
