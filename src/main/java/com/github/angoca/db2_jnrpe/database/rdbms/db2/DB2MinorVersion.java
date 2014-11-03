package com.github.angoca.db2_jnrpe.database.rdbms.db2;

/**
 * Different list of DB2 minor versions.
 * 
 * @author Andres Gomez Casanova (@AngocA)
 * @version 2014-11-03
 */
public enum DB2MinorVersion {

    /**
     * Other version which is unknown for the system.
     */
    OTHER("", 99900),
    /**
     * Null version, notably an error.
     */
    UNKNOWN(null, 0),
    /**
     * 10.1 version GA.
     */
    V10_1_GA("DB2 v10.1.0.0", 10100),
    /**
     * 10.1 version Fixpack 1.
     */
    V10_1_1("DB2 v10.1.0.1", 10101),
    /**
     * 10.1 version Fixpack 2.
     */
    V10_1_2("DB2 v10.1.0.2", 10102),
    /**
     * 10.1 version Fixpack 3.
     */
    V10_1_3("DB2 v10.1.0.3", 10103),
    /**
     * 10.1 version Fixpack 4.
     */
    V10_1_4("DB2 v10.1.0.4", 10104),
    /**
     * 10.5 version.
     */
    V10_5_GA("DB2 v10.5.0.0", 10500),
    /**
     * 10.5 version Fixpack 1.
     */
    V10_5_1("DB2 v10.5.100.64", 10501),
    /**
     * 10.5 version Fixpack 2.
     */
    V10_5_2("DB2 v10.5.0.200", 10501),
    /**
     * 10.5 version Fixpack 3.
     */
    V10_5_3("DB2 v10.5.0.300", 10501),
    /**
     * 10.5 version Fixpack 4.
     */
    V10_5_4("DB2 v10.5.0.400", 10501),
    /**
     * 9.7 version GA.
     */
    V9_7_GA("DB2 v9.7.0.0", 9700),
    /**
     * 9.7 version Fixpack 1.
     */
    V9_7_1("DB2 v9.7.0.1", 9701),
    /**
     * 9.7 version Fixpack 2.
     */
    V9_7_2("DB2 v9.7.0.2", 9702),
    /**
     * 9.7 version Fixpack 3.
     */
    V9_7_3("DB2 v9.7.0.3", 9703),
    /**
     * 9.7 version Fixpack 4.
     */
    V9_7_4("DB2 v9.7.0.4", 9704),
    /**
     * 9.7 version Fixpack 5.
     */
    V9_7_5("DB2 v9.7.0.5", 9705),
    /**
     * 9.7 version Fixpack 6.
     */
    V9_7_6("DB2 v9.7.0.6", 9706),
    /**
     * 9.7 version Fixpack 7.
     */
    V9_7_7("DB2 v9.7.0.7", 9707),
    /**
     * 9.7 version Fixpack 8.
     */
    V9_7_8("DB2 v9.7.0.8", 9708),
    /**
     * 9.7 version Fixpack 9.
     */
    V9_7_9("DB2 v9.7.0.9", 9709),
    /**
     * 9.8 version GA.
     */
    V9_8_GA("DB2 v9.8.0.0", 9800),
    /**
     * 9.8 version Fixpack 1.
     */
    V9_8_1("DB2 v9.8.0.1", 9801),
    /**
     * 9.8 version Fixpack 2.
     */
    V9_8_2("DB2 v9.8.0.2", 9802),
    /**
     * 9.8 version Fixpack 3.
     */
    V9_8_3("DB2 v9.8.0.3", 9803),
    /**
     * 9.8 version Fixpack 4.
     */
    V9_8_4("DB2 v9.8.0.4", 9804),
    /**
     * 9.8 version Fixpack 5.
     */
    V9_8_5("DB2 v9.8.0.5", 9805);

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
     * @param name
     *            Name of the DB2 version.
     * @param value
     *            Value that identifies the version.
     */
    private DB2MinorVersion(final String name, final int value) {
        this.name = name;
        this.value = value;
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
    private final int getValue() {
        return this.value;
    }

    /**
     * Compares a given DB2 version with another.
     * 
     * @param version
     *            Version to compare.
     * @return true if the given version is older.
     */
    public final boolean isEqualOrMoreRecentThan(final DB2MinorVersion version) {
        boolean ret = false;
        if (this.getValue() >= version.getValue()) {
            ret = true;
        }
        return ret;
    }
}
