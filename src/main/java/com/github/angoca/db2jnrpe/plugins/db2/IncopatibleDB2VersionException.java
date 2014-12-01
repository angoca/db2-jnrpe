package com.github.angoca.db2jnrpe.plugins.db2;

import com.github.angoca.db2jnrpe.database.rdbms.db2.DB2MinorVersion;

/**
 * Exception that describes the current version and the minimum required
 * version.
 *
 * @author Andres Gomez Casanova (@AngocA)
 * @version 2014-11-28
 */
public class IncopatibleDB2VersionException extends Exception {
    /**
     * Creates an exception indicating the current version and the minimun
     * required version.
     * 
     * @param currentVersion
     *            Current version of the DB2 installation.
     * @param requiredVersion
     *            Minimum required DB2 version.
     */
    public IncopatibleDB2VersionException(final DB2MinorVersion currentVersion,
            final DB2MinorVersion requiredVersion) {
        super("The current version is " + currentVersion.toString()
                + " and the plugin requires at least "
                + requiredVersion.toString());
    }

    /**
     * Generate ID.
     */
    private static final long serialVersionUID = -2632738379083321219L;

}
