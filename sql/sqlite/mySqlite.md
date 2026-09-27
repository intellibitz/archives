#

    https://www.sqlite.org/index.html

    https://sqlite.org/src/doc/trunk/README.md

    https://www.sqlite.org/features.html

Features Of SQLite
--

Transactions are atomic, consistent, isolated, and durable (ACID) even after system crashes and power failures.
Zero-configuration - no setup or administration needed.
Full-featured SQL implementation with advanced capabilities like partial indexes, indexes on expressions, JSON, common table expressions, and window functions. (Omitted features)
A complete database is stored in a single cross-platform disk file. Great for use as an application file format.
Supports terabyte-sized databases and gigabyte-sized strings and blobs. (See limits.html.)
Small code footprint: less than 750KiB fully configured or much less with optional features omitted.
Simple, easy to use API.
Fast: In some cases, SQLite is faster than direct filesystem I/O
Written in ANSI-C. TCL bindings included. Bindings for dozens of other languages available separately.
Well-commented source code with 100% branch test coverage.
Available as a single ANSI-C source-code file that is easy to compile and hence is easy to add into a larger project.
Self-contained: no external dependencies.
Cross-platform: Android, *BSD, iOS, Linux, Mac, Solaris, VxWorks, and Windows (Win32, WinCE, WinRT) are supported out of the box. Easy to port to other systems.
Sources are in the public domain. Use for any purpose.
Comes with a standalone command-line interface (CLI) client that can be used to administer SQLite databases.
Suggested Uses For SQLite:
Database For The Internet Of Things. SQLite is popular choice for the database engine in cellphones, PDAs, MP3 players, set-top boxes, and other electronic gadgets. SQLite has a small code footprint, makes efficient use of memory, disk space, and disk bandwidth, is highly reliable, and requires no maintenance from a Database Administrator.

Application File Format. Rather than using fopen() to write XML, JSON, CSV, or some proprietary format into disk files used by your application, use an SQLite database. You'll avoid having to write and troubleshoot a parser, your data will be more easily accessible and cross-platform, and your updates will be transactional. (more...)

Website Database. Because it requires no configuration and stores information in ordinary disk files, SQLite is a popular choice as the database to back small to medium-sized websites.

Stand-in For An Enterprise RDBMS. SQLite is often used as a surrogate for an enterprise RDBMS for demonstration purposes or for testing. SQLite is fast and requires no setup, which takes a lot of the hassle out of testing and which makes demos perky and easy to launch.

    https://www.sqlite.org/appfileformat.html
    https://www.sqlite.org/whentouse.html

Appropriate Uses For SQLite
--

SQLite is not directly comparable to client/server SQL database engines such as MySQL, Oracle, PostgreSQL, or SQL Server since SQLite is trying to solve a different problem.

Client/server SQL database engines strive to implement a shared repository of enterprise data. They emphasize scalability, concurrency, centralization, and control. SQLite strives to provide local data storage for individual applications and devices. SQLite emphasizes economy, efficiency, reliability, independence, and simplicity.

SQLite does not compete with client/server databases. SQLite competes with fopen().

    https://www.sqlite.org/zeroconf.html

SQLite Is A Zero-Configuration Database
--

SQLite does not need to be "installed" before it is used. There is no "setup" procedure. There is no server process that needs to be started, stopped, or configured. There is no need for an administrator to create a new database instance or assign access permissions to users. SQLite uses no configuration files. Nothing needs to be done to tell the system that SQLite is running. No actions are required to recover after a system crash or power failure. There is nothing to troubleshoot.

SQLite just works.

Other database engines may run great once you get them going. But doing the initial installation and configuration can often be intimidating.

#
