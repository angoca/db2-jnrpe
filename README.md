db2-jnrpe
=========

This is a set of plugins to monitor DB2 via Nagios. It relies on the JNRPE
framework, which is a Java implementation of the NRPE server.
This framework is very flexible and it can be used not only to monitor DB2
database, but also to:

* Create more plugins to monitor other elements of DB2.
* Create plugins for other RDBMS using the benefits of Java.

The difference between this set of plugins and other available in the market is
that a Java daemon allows to reduce the overhead on the database.
Other scripts, establishes one or multiple connections to query the elements in
the database each time they are executed, and this creates a big overhead in
the database.
This set of plugins keeps the connection active thanks to a connection pool
(Hikari or C3p0) and this reduces the overhead of connection establishment.

Also, many plugins queries the same values to monitor similar elements.
For example, for each bufferpool, other plugins has to query the database
several times to retrieve the same information and then filter it according to
the given parameters (different commands).
This set of plugins queries the database once, and keep the information in a
cache to be used for multiple commands.

With a cache system, this plugin can compare values with the previous
measures, in order to give an instant performance.
No extra files are required, just a daemon executing in a server.

Because the JNRPE is a pure Java application, it can be executed in any
platform, Linux/UNIX/MAC OS X or Windows. This is a very important difference
with the other plugins in the market that require bash on Linux, or Perl.


# Plugins

This is the list of available DB2 plugins:

* Check_bufferpool_hit_ratio: Allows to check the performance of the
bufferpools, by measuring the hit ratio between the most recent measures.
* Check_Database_Load: Checks the database load according to the quantity
of modifications (UID: Update, insert, delete), commits and selects.
* Check_Physical_IO_Per_Transaction: Measures the quantity of physical reads
per transaction.

# Prerequisites

In order to run this set of plugins, it is necessary to have Java 6 or 7.

# Install

The installation process consist in:

* Download JNRPE and installed if it has not been done.
* Download a stable release of this project from
    https://github.com/angoca/db2-jnrpe/releases
* Download the required libraries:
 * Hikari
 * JavaAssist
 * SFL4J API
* Copy the DB2 driver in the same directory.
* Configure the jnrpe.ini file with the plugins.
* Start the JNRPE server.
* Configure Nagios to call the plugins.
* Restart Nagios.

A more detailed installation guide can be found in the wiki:
  https://github.com/angoca/db2-jnrpe/wiki/Install


# References

* Nagios - http://www.nagios.org/
* JNRPE - http://www.jnrpe.it/
* DB2 - http://www.ibm.com/software/data/db2/

