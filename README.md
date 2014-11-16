db2-jnrpe
=========

This is a set of plugins to monitor DB2 via Nagios. It relies on the JNRPE framework, which is a Java implementation of the NRPE server. This framework is very flexible and can be used to:

* Create more plugins to monitor more elements of DB2.
* Create plugins for other RDBMS using the benefitcs of Java.

The difference between this set of plugins and other available in the market is that a Java daemon allows to reduce the overhead on the database. Other scripts, establishes one or multiple connections to query the elements in the database, and this creates a big overhead in the database. This set of plugins keeps the connection active thanks to a connection pool (Hikari or C3p0) and this reduces the overhead of connection establishment.
Also, many plugins queries the same values to monitor similar elements. For example, for each bufferpool, other queries has to query the database several times to retrieve the same information and then filter it according to the parameters. This set of plugins queries the database once, and keep the information in a cache to be used for multiple commands.
With a cache system, this plugin can compares values with the previous measures, in order to give an instant performance. No extra files are required, just a daemon executing.

# Plugins

This is the list of available plugins:

* Check_bufferpool_hit_ratio: Allows to check the performance of the bufferpools, by measuring the hit ratio between the most recent measures.

# Install

The installation process consist in:

* Download JNRPE and installed if it has not been done.
* Download a stable release of this project from https://github.com/angoca/db2-jnrpe/releases
* Download the requiered libraries:
 * Hikari
 * JavaAssist
* Copy the DB2 driver in the same directory.
* Configure the jnrpe.ini file with the plugins.
* Start the JNRPE server.
* Configure Nagios to call the plugins.
* Restart Nagios.
