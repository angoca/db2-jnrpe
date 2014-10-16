package com.github.angoca.db2_jnrpe.database.rdbms.db2;

import java.util.Properties;

import com.github.angoca.db2_jnrpe.database.DatabaseConnection;

public class DB2Connection extends DatabaseConnection {

    final protected String driverClass = "com.ibm.db2.jcc.DB2Driver";

    public DB2Connection(final String connectionsPool,
            final Properties defaultProperties, final String hostname,
            final int portNumber, final String databaseName,
            final String username, final String password) {
        super(connectionsPool, defaultProperties, username, password);
        // Changes the application name.
        this.connectionProperties.put("clientProgramName", "db2-jnrpe");
        // Shows descriptive message when errors.
        this.connectionProperties.put("retrieveMessagesFromServerOnGetMessage",
                "true");

        this.setURL("jdbc:db2://" + hostname + ":" + portNumber + "/"
                + databaseName);
    }

    @Override
    public String getDriverClass() {
        return this.driverClass;
    }

}
