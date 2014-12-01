package com.github.angoca.db2jnrpe.database.rdbms.db2;

/**
 * Different list of DB2 major versions.
 *
 * @author Andres Gomez Casanova (@AngocA)
 * @version 2014-11-03
 */
public enum DB2MajorVersion {
    /**
     * Other version which is unknown for the system.
     */
    OTHER("", 999),
    /**
     * Null version, notably an error.
     */
    UNKNOWN(null, 0),
    /**
     * 10.1 version.
     */
    V10_1("10.1", 101),
    /**
     * 10.5 version.
     */
    V10_5("10.5", 105),
    /**
     * 8.1 Version.
     */
    V8_1("8.1", 81),
    /**
     * 9.1 version.
     */
    V9_1("9.1", 91),
    /**
     * 9.5 version.
     */
    V9_5("9.5", 95),
    /**
     * 9.7 version.
     */
    V9_7("9.7", 97),
    /**
     * 9.8 version.
     */
    V9_8("9.8", 98);

    /**
     * Name of the DB2 version.
     */
    private final String name;

    /**
     * Identifier of the DB2 version.
     */
    private final int value;

    /**
     * Constructor for DB2 versions.
     *
     * @param versionName
     *            Name of the DB2 version.
     * @param val
     *            Value that identifies the version.
     */
    private DB2MajorVersion(final String versionName, final int val) {
        this.name = versionName;
        this.value = val;
    }

    /**
     * Descriptive name of the DB2 version.
     *
     * @return DB2 major version.
     */
    final String getName() {
        return this.name;
    }

    /**
     * Returns the identifier of the DB2 version.
     *
     * @return Id of the version.
     */
    private int getValue() {
        return this.value;
    }

    /**
     * Compares a given DB2 version with another.
     *
     * @param version
     *            Version to compare.
     * @return true if the given version is older.
     */
    public final boolean isEqualOrMoreRecentThan(final DB2MajorVersion version) {
        boolean ret = false;
        if (this.getValue() >= version.getValue()) {
            ret = true;
        }
        return ret;
    }

    /**
     * Compares the DB2 version with a specific one.
     *
     * @param version
     *            Version to compare to.
     * @return true if the given version is equal. False otherwise.
     */
    public final boolean isEqualThan(final DB2MajorVersion version) {
        boolean ret = false;
        if (this.getValue() == version.getValue()) {
            ret = true;
        }
        return ret;
    }
}
