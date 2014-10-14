package com.github.angoca.db2_jnrpe;

import java.sql.Connection;
import java.sql.SQLException;

public interface DBBroker {
    Connection getConnection() throws SQLException;

    void closeConnection() throws SQLException;
}