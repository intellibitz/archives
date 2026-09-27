#

# https://dev.mysql.com/doc/refman/8.0/en/linux-installation-native.html
# https://dev.mysql.com/doc/mysql-apt-repo-quick-guide/en/#apt-repo-fresh-install

## https://dev.mysql.com/doc/refman/8.0/en/binary-installation.html

# https://dev.mysql.com/doc/mysql-apt-repo-quick-guide/en/
sudo apt-get install mysql-server
systemctl status mysql

# https://dev.mysql.com/doc/refman/8.0/en/starting-server.html
systemctl start mysqld

#https://dev.mysql.com/doc/refman/8.0/en/testing-server.html
mysqladmin version
mysqladmin variables
mysqladmin -u root -p version
mysqladmin -u root shutdown
mysqld_safe --user=mysql &
mysqlshow
mysqlshow mysql
mysql -e "SELECT User, Host, plugin FROM mysql.user" mysql

# https://dev.mysql.com/doc/refman/8.0/en/creating-accounts.html

# To install and use a MySQL binary distribution, the command sequence looks like this:
groupadd mysql
useradd -r -g mysql -s /bin/false mysql
cd /usr/local
tar xvf /path/to/mysql-VERSION-OS.tar.xz
ln -s full-path-to-mysql-VERSION-OS mysql
cd mysql
mkdir mysql-files
chown mysql:mysql mysql-files
chmod 750 mysql-files
bin/mysqld --initialize --user=mysql
bin/mysql_ssl_rsa_setup
bin/mysqld_safe --user=mysql &
# Next command is optional
cp support-files/mysql.server /etc/init.d/mysql.server

#https://dev.mysql.com/doc/refman/8.0/en/creating-accounts.html
mysql -u root -p
mysql -u root --skip-password
# mysql> ALTER USER 'root'@'localhost' IDENTIFIED BY 'root-password';
mysqladmin -u root -p shutdown

#Creating Accounts and Granting Privileges
CREATE USER 'username'@'host' IDENTIFIED WITH authentication_plugin BY 'password';
CREATE USER 'sammy'@'localhost' IDENTIFIED BY 'password';
CREATE USER 'sammy'@'localhost' IDENTIFIED WITH mysql_native_password BY 'password';
ALTER USER 'sammy'@'localhost' IDENTIFIED WITH caching_sha2_plugin BY 'password';
GRANT PRIVILEGE ON database.table TO 'username'@'host';
GRANT CREATE, ALTER, DROP, INSERT, UPDATE, INDEX, DELETE, SELECT, REFERENCES, RELOAD
on *.*
TO 'sammy'@'localhost'
WITH GRANT OPTION;
GRANT ALL PRIVILEGES ON *.* TO 'sammy'@'localhost' WITH GRANT OPTION;
FLUSH PRIVILEGES;

CREATE USER 'finley'@'localhost'
  IDENTIFIED BY 'password';
GRANT ALL
  ON *.*
  TO 'finley'@'localhost'
  WITH GRANT OPTION;

CREATE USER 'finley'@'%.example.com'
  IDENTIFIED BY 'password';
GRANT ALL
  ON *.*
  TO 'finley'@'%.example.com'
  WITH GRANT OPTION;

CREATE USER 'admin'@'localhost'
  IDENTIFIED BY 'password';
GRANT RELOAD,PROCESS
  ON *.*
  TO 'admin'@'localhost';

CREATE USER 'dummy'@'localhost';

CREATE USER 'custom'@'localhost'
  IDENTIFIED BY 'password';
GRANT ALL
  ON bankaccount.*
  TO 'custom'@'localhost';

CREATE USER 'custom'@'host47.example.com'
  IDENTIFIED BY 'password';
GRANT SELECT,INSERT,UPDATE,DELETE,CREATE,DROP
  ON expenses.*
  TO 'custom'@'host47.example.com';

CREATE USER 'custom'@'%.example.com'
  IDENTIFIED BY 'password';
GRANT SELECT,INSERT,UPDATE,DELETE,CREATE,DROP
  ON customer.addresses
  TO 'custom'@'%.example.com';

#Checking Account Privileges and Properties
# mysql> SHOW GRANTS FOR 'admin'@'localhost';

# mysql> SET print_identified_with_as_hex = ON;
# mysql> SHOW CREATE USER 'admin'@'localhost'\G

#Revoking Account Privileges
REVOKE ALL
  ON *.*
#   FROM 'finley'@'%.example.com';

REVOKE RELOAD
  ON *.*
#   FROM 'admin'@'localhost';

REVOKE CREATE,DROP
  ON expenses.*
#   FROM 'custom'@'host47.example.com';

REVOKE INSERT,UPDATE,DELETE
  ON customer.addresses
#   FROM 'custom'@'%.example.com';

#Dropping Accounts
DROP USER 'finley'@'localhost';
DROP USER 'finley'@'%.example.com';
DROP USER 'admin'@'localhost';
DROP USER 'dummy'@'localhost';

#https://dev.mysql.com/doc/refman/8.0/en/connecting-disconnecting.html
#Connecting to and Disconnecting from the Server
mysql -h host -u user -p

#Entering Queries
# mysql> SELECT VERSION(), CURRENT_DATE;
# mysql> SELECT SIN(PI()/4), (4+1)*5;
# mysql> SELECT VERSION(); SELECT NOW();
# mysql> SELECT
#     -> USER()
#     -> ,
#     -> CURRENT_DATE;
# mysql> SELECT
#     -> USER()
#     -> \c

#https://dev.mysql.com/doc/refman/8.0/en/database-use.html
#Creating and Using a Database
# mysql> SHOW DATABASES;

#Getting Information About Databases and Tables
# mysql> SELECT DATABASE();
# mysql> SHOW TABLES;
# mysql> DESCRIBE pet;

#https://dev.mysql.com/doc/refman/8.0/en/creating-database.html
# mysql> CREATE DATABASE menagerie;
# mysql> USE menagerie
mysql -h host -u user -p menagerie

# Sample SQL schema and data moved to myMySQL.sql
# mysql -u root -p < myMySQL.sql

#https://dev.mysql.com/doc/refman/8.0/en/creating-tables.html
# mysql> SHOW TABLES;
# mysql> CREATE TABLE pet (name VARCHAR(20), owner VARCHAR(20),
#        species VARCHAR(20), sex CHAR(1), birth DATE, death DATE);
# mysql> DESCRIBE pet;
# mysql> LOAD DATA LOCAL INFILE '/path/pet.txt' INTO TABLE pet;
# mysql> LOAD DATA LOCAL INFILE '/path/pet.txt' INTO TABLE pet
#        LINES TERMINATED BY '\r\n';
# mysql> INSERT INTO pet
#        VALUES ('Puffball','Diane','hamster','f','1999-03-30',NULL);

#https://dev.mysql.com/doc/refman/8.0/en/retrieving-data.html
# SELECT what_to_select
# FROM which_table
# WHERE conditions_to_satisfy;


#Using mysql in Batch Mode
# mysql < batch-file
mysql -e "source batch-file"
# mysql -h host -u user -p < batch-file
# mysql < batch-file | more
# mysql < batch-file > mysql.out
# mysql> source filename;
# mysql> \. filename

#https://dev.mysql.com/doc/refman/8.0/en/programs-overview.html
#Invoking MySQL Programs
mysql --user=root test
mysqladmin extended-status variables
mysqlshow --help
mysqldump -u root personnel

mysql --column-names --skip-column-names

mysql -ptest
mysql -p test
#The first command instructs mysql to use a password value of test, but
# specifies no default database. The second instructs mysql to prompt for the password value and
# to use test as the default database.

#https://dev.mysql.com/doc/refman/8.0/en/command-line-options.html
#The MySQL server has certain command options that may be specified only at startup, and
# a set of system variables, some of which may be set at startup, at runtime, or both.
# System variable names use underscores rather than dashes, and when referenced at runtime
# (for example, using SET or SELECT statements), must be written using underscores:
# SET GLOBAL general_log = ON;
# SELECT @@GLOBAL.general_log;

mysql -u root -p -e "SELECT VERSION();SELECT NOW()"

#https://dev.mysql.com/doc/refman/8.0/en/connecting.html
mysql --host=localhost --user=myname --password=password mydb
mysql -h localhost -u myname -ppassword mydb

mysql --host=localhost --user=myname --password mydb
mysql -h localhost -u myname -p mydb

#To ensure that the client makes a TCP/IP connection to the local server, use
# --host or -h to specify a host name value of 127.0.0.1 (instead of localhost), or
# the IP address or name of the local server. You can also specify the transport protocol
# explicitly, even for localhost, by using the --protocol=TCP option.
mysql --host=127.0.0.1
mysql --protocol=TCP

# For this command, the program uses a socket file on Unix and the --port option is ignored:
mysql --port=13306 --host=localhost

#To cause the port number to be used, force a TCP/IP connection. For example,
# invoke the program in either of these ways:
mysql --port=13306 --host=127.0.0.1
mysql --port=13306 --protocol=TCP

#https://dev.mysql.com/doc/refman/8.0/en/programs-client.html

#https://dev.mysql.com/doc/refman/8.0/en/programs-development.html
my_print_defaults client mysql
# --port=3306
# --socket=/tmp/mysql.sock
# --no-auto-rehash

#https://dev.mysql.com/doc/refman/8.0/en/security.html

#https://dev.mysql.com/doc/refman/8.0/en/sql-statements.html

#https://dev.mysql.com/doc/refman/8.0/en/access-control.html

#