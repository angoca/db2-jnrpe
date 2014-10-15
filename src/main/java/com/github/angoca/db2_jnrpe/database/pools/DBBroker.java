package com.github.angoca.db2_jnrpe.database.pools;

import java.sql.Connection;
import java.sql.SQLException;

import com.github.angoca.db2_jnrpe.database.DatabaseConnection;

public abstract class DBBroker {

    protected abstract Connection getConnection(final DatabaseConnection dbConn)
            throws SQLException;

    protected abstract void closeConnection(final DatabaseConnection dbConn)
            throws SQLException;
}

