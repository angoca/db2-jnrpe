package com.github.angoca.db2_jnrpe.database.rdbms.db2;

import java.util.Properties;

import com.github.angoca.db2_jnrpe.database.DatabaseConnection;

public class DB2Connection extends DatabaseConnection {

    final protected String driverClass = "com.ibm.db2.jcc.DB2Driver";

    public DB2Connection(Properties defaultProperties, String hostname,
            int portNumber, String databaseName, String username,
            String password) {
        super(defaultProperties, hostname, portNumber, databaseName, username,
                password);
    }

    @Override
    public String getDriverClass() {
        return this.driverClass;
    }

}

