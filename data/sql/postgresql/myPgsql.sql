-- sudo su - postgres #logged in as user postgres
-- psql -d postgres -U postgres 

$EDITOR $PGDATA/pg_hba.conf -- ... modify the file as you wish ...
sudo -u postgres pg_ctl reload -D $PGDATA -- server signaled

psql

select pg_reload_conf(); -- pg_ctl reload
SELECT current_database();
SELECT current_role;
SELECT current_time;
SELECT current_time \g <ENTER>

\e --opens last-edited query in editor
\e test.sql --edits file
\i test.sql --runs commands in file

\? --shows help for psql commands
\h SELECT --shows help for select command

\du --describe users

CREATE ROLE
ALTER ROLE
DROP ROLE

CREATE ROLE name [ [ WITH ] option [ ... ] ]

#Creating new roles
#In order to create a new role, either a single user account or a group container, you need to use the
CREATE ROLE statement.
CREATE ROLE name [ [ WITH ] option [ ... ] ]

CREATE ROLE luca WITH PASSWORD 'xxx'; --role with no login
CREATE ROLE luca WITH LOGIN PASSWORD 'xxx'; --role with login
CREATE ROLE luca WITH LOGIN PASSWORD 'xxx' VALID UNTIL '2030-12-25 23:59:59'; #login expires after set date

#Using a role as a group
#A group is a role that contains other roles.
CREATE ROLE book_authors WITH NOLOGIN;

CREATE ROLE luca WITH LOGIN PASSWORD 'xxx' IN ROLE book_authors;
CREATE ROLE enrico WITH LOGIN PASSWORD 'xxx' IN ROLE book_authors;
GRANT book_authors TO enrico;

CREATE ROLE book_reviewers WITH NOLOGIN ADMIN luca;

GRANT book_reviewers TO enrico WITH ADMIN OPTION;

#Removing an existing role
#In order to remove an existing role, you need to use the DROP ROLE statement.
DROP ROLE [ IF EXIST ] name [, ...]

DROP ROLE this_role_does_not_exist;
#ERROR: role "this_role_does_not_exist" does not exist

DROP ROLE IF EXIST this_role_does_not_exist;
#NOTICE: role "this_role_does_not_exist" does not exist, skipping

SELECT current_role;
\du --describe users
\drg --describe groups the role is part of

SELECT rolname, rolcanlogin,rolconnlimit, rolpassword
FROM pg_roles WHERE rolname = 'luca';

SELECT rolname, rolcanlogin, rolconnlimit, rolpassword
FROM pg_authid WHERE rolname = 'luca';

CREATE DATABASE databasename;

#postgres=#
create database myforumdb;
#CREATE DATABASE
#postgres=#
\c myforumdb
#You are now connected to database "myforumdb" as user "postgres".
#myforumdb=#
create user myforum with password 'SuperSecret' login;
#CREATE ROLE
#myforumdb=#
create schema myforum authorization myforum;
#CREATE SCHEMA
#postgres@learn_postgresql:/$
psql -U myforum myforumdb
#myforumdb=>
create table mytable(id integer);
#CREATE TABLE

DROP TABLE #: This is used to drop a table in the database.
DROP DATABASE #: This is used to drop a database in the cluster.

create database forumdb2 template forumdb;

select pg_database_size('postgres');
select pg_size_pretty(pg_database_size('postgres'));

select * from pg_database where datname='postgres';

CREATE TABLE myusers (
pk int GENERATED ALWAYS AS IDENTITY
, username text NOT NULL
, gecos text
, email text NOT NULL
, PRIMARY KEY( pk )
, UNIQUE ( username )
);
# CREATE TABLE



CREATE TABLE myusers (
    pk int GENERATED ALWAYS AS IDENTITY
, username text NOT NULL
, gecos text
, email text NOT NULL
, PRIMARY KEY( pk )
, UNIQUE ( username )
);
-- CREATE TABLE

drop table myusers ;
-- DROP TABLE

create table if not exists users (
    pk int GENERATED ALWAYS AS IDENTITY
,username text NOT NULL
,gecos text
,email text NOT NULL
,PRIMARY KEY( pk )
,UNIQUE ( username )
);
-- NOTICE:
-- relation "users" already exists, skipping
-- CREATE TABLE

drop table if exists myusers;
-- NOTICE:
-- table "myusers" does not exist, skipping
-- DROP TABLE

create temp table if not exists temp_users
    pk int GENERATED ALWAYS AS IDENTITY
,username text NOT NULL
,gecos text
,email text NOT NULL
,PRIMARY KEY( pk )
,UNIQUE ( username )
);
-- CREATE TABLE
-- The preceding command will create the temp_users table, which will only be visible within the
-- session where the table was created.

-- Start the transaction with the following code:
begin work;
-- BEGIN
-- forumdb=*>
-- The * symbol means that we are inside a transaction block.
-- Create a table visible only inside the transaction:
create temp table if not exists temp_users_transaction (
    pk int GENERATED ALWAYS AS IDENTITY
,username text NOT NULL
,gecos text
,email text NOT NULL
,PRIMARY KEY( pk )
,UNIQUE ( username )
) on commit drop;
-- CREATE TABLE
-- Now check that the table is present inside the transaction and not outside the transaction:
forumdb=*> \d temp_users_transaction
commit work;
-- COMMIT
-- If you re-execute the DESCRIBE command \d temp_users_transaction,
-- PostgreSQL responds in this way:
forumdb=> \d temp_users_transaction
-- Did not find any relation named "temp_users_transaction".
-- This happens because the on commit drop option drops the table once the transaction is completed.

create unlogged table if not exists unlogged_users (
    pk int GENERATED ALWAYS AS IDENTITY
,username text NOT NULL
,gecos text
,email text NOT NULL
,PRIMARY KEY( pk )
,UNIQUE ( username )
);
-- CREATE TABLE
-- Unlogged tables are a fast alternative to permanent and temporary tables. 
-- This performance increase comes at the expense of losing data in the event of a server crash.
-- If the server crashes after the reboot, the table will be empty. This is something you
-- may be able to afford under certain circumstances.

-- a system table called pg_class, which collects information about
-- all the tables that are present in the database.
select oid,relname from pg_class where relname='users';

-- In PostgreSQL, each table or index is stored in one or more files. When a table or
-- index exceeds 1 GB, it is divided into gigabyte-sized segments.

insert into users (username,gecos,email) values
    ('myusername','mygecos','myemail');
-- INSERT 0 1
-- This result shows that PostgreSQL has inserted one record into the users table. The first
-- number is the OID of the row that has been inserted; newer versions of PostgreSQL by
-- default have tables created without OIDs on the rows, so you just get a 0 returned.

select pk,username,gecos,email from users;
select * from users;
select pk,username,gecos,email from users 
    order by username;
-- The SQL language, without the ORDER BY option, does not return the data
-- in an orderly manner.
select pk,username,gecos,email from users 
    order by 2;
-- PostgreSQL also accepts field positions on a query as sorting options.

insert into categories (title,description) values 
    ('CLanguage', 'Languages'), ('Python Language','Languages');
-- INSERT 0 2


select * from categories 
    where description ='Database related discussions';
select * from categories 
    where description = 'Languages'
and title='C Language';
select * from categories 
    where description ='Languages'
order by title desc;
select * from categories 
    where description ='Languages'
order by 2 desc;
-- The ASC and DESC options sort the query in ascending or descending order;
-- if nothing is specified, ASC is the default.

-- In order to see the NULL values present in the table, execute the following command:
forumdb=> \pset null NULL
-- Null display is "NULL".
-- This tells psql to show NULL values t​​hat are present in the table as NULL:
-- if we want to see all records that have NULL values in the description field,
-- we have to use the IS NULL operator:
select title,description from categories 
    where description is null;
select title,description from categories 
    where description is not null;
-- To perform searches on NULL fields, we have to use the operators
-- IS NULL / IS NOT NULL. An empty string is different from a NULL value.
select * from categories 
    order by description NULLS first;
-- If not specified, the following are the default actions for ORDER BY type queries:
-- ORDER BY NULLS LAST is the default for ASC (which is also the default) 
-- and NULLS FIRST is the default for DESC.

create temp table temp_categories 
    as select * from categories;
-- SELECT 6
-- This command creates a table called temp_data with the same data structure and
-- data as the table called categories:

update temp_categories set title='Linux' 
    where pk = 2;
-- UPDATE 1
update temp_categories set title = 'no title' 
    where description = 'Languages';
-- UPDATE 2
-- You must be careful when using the UPDATE command. If you work in auto-commit mode, there
-- is no chance of turning back after the update is complete. Auto-commit is the default in psql.

delete from temp_categories 
    where pk=5;
-- DELETE 1
delete from temp_categories 
    where description is null;
-- DELETE 1
delete from temp_categories ;
-- DELETE 4
-- Be very careful when you use this command,
-- all records present in the table will be deleted!


insert into temp_categories 
    select * from categories;
-- INSERT 0 6

truncate table temp_categories ;
-- TRUNCATE TABLE
-- TRUNCATE deletes all the records in a table similar to the DELETE command.
-- In the TRUNCATE command, it is not possible to use where conditions.
-- The TRUNCATE command deletes records much faster than the DELETE command.

select * from categories 
    where title like 'Prog%';
select * from categories 
    where title like '%Languages';
select * from categories 
    where description like '%discuss%';
-- like searches are case-sensitive.
select upper('prog');
-- In PostgreSQL, it is possible to call functions without writing FROM. PostgreSQL does
-- not need dummy tables to perform the SELECT function. If we were in Oracle DB, the
-- same query would have to be written this way: select upper('prog') from DUAL;
select * from categories 
    where upper(description) like '%DISCUSS%';
-- In PostgreSQL, it is possible to perform a case-insensitive
-- like query by using the ilike operator.
select * from categories 
    where description ilike '%DISCUSS%';

-- The coalesce function, given two or more parameters,
-- returns the first value that is not NULL.
select coalesce(NULL,'test');
select pk,title,
    coalesce(description,'No description') from categories;
select pk,title,
    coalesce(description,'No description') 
    as description from categories;
select pk,title,
    coalesce(description,'No description') 
    as "Description" from categories;

select 
    distinct title from categories order by title;
-- The select distinct statement is used to return only distinct (different) values. 
-- Internally, the distinct statement involves a data sort for large tables, which
-- means that if a query uses the distinct statement, the query may
-- become slower as the number of records increases.

select * from categories order by pk 
    limit 2;
select * from categories order by pk 
    offset 1 limit 1;

create table new_categories 
    as select * from categories 
limit 0;
-- SELECT 0
-- This statement will copy into the new_categories table only
--  the data structure of the table categories.
-- The SELECT 0 clause means that no data has been copied into
--  the new_categories table; only
-- the data structure has been replicated

select * from categories 
    where pk=1 or pk=2;
select * from categories where pk 
    in (1,2);
select * from categories where pk 
    not in (1,2);

-- SUBQUERIES
-- Subqueries can be described as nested queries – we
-- can nest a query inside another query using parentheses. 
-- Subqueries can return a single value
-- or a recordset, just like regular queries.
select pk,title,content,author,category from posts 
    where category in 
    (select pk from categories where title ='Database');
select pk,title,content,author,category from posts 
    where category not in 
    (select pk from categories where title ='Database');
select pk,title,content,author,category from posts 
    where exists
    (select 1 from categories where title ='Database' and posts.category=pk);
select pk,title,content,author,category from posts 
    where not exists 
    (select 1 from categories where title ='Database' and posts.category=pk);

-- JOINS
-- CROSS JOIN
-- think of a join as a combination of rows from two or more tables.
select c.pk,c.title,p.pk,p.category,p.title from 
-- This query makes a Cartesian product between the category table and the posts table.
-- It can also be called a cross join:
    categories c,
    posts p;
-- The above query can also be written in the following way:    
select c.pk,c.title,p.pk,p.category,p.title from 
    categories c
    CROSS JOIN posts p;

-- INNER JOIN
-- keyword selects records that have matching values in both tables.
select c.pk,c.title,p.pk,p.category,p.title from 
    categories c,posts p 
    where c.pk=p.category;
-- the same query using the explicit JOIN operation:
select c.pk,c.title,p.pk,p.category,p.title from 
    categories c inner join posts p 
    on c.pk=p.category;
-- Using the INNER JOIN condition, we can rewrite all queries that
-- can be written using the IN or EXISTS condition.
select c.pk,c.title,p.pk,p.category,p.title from 
    categories c inner join posts p
    on c.pk=p.category 
    where c.title='Database';
-- It is preferable to use JOIN conditions whenever possible
-- instead of IN or EXISTS conditions, because they
-- perform better in terms of execution speed

-- LEFT JOIN
-- The left join keyword returns all records from the left table (table1), and all
-- the records from the right table (table2). The result is NULL from the right side if
-- there is no match.
select c.*,p.category,p.title from 
    categories c left join posts p 
    on c.pk=p.category;
-- Using the left join condition, we can rewrite some queries that can be written
-- using the IN or EXISTS condition.
select c.*,p.category from 
    categories c left join posts p 
on p.category=c.pk;
select c.* from 
    categories c left join posts p 
    on p.category=c.pk 
where p.category is null;

-- RIGHT JOIN
-- The right join is the twin of the left join, so we would have the same result if we wrote table A
-- left join table B, or table B right join table A.
select c.*,p.category from categories c 
    left join posts p on p.category=c.pk;
-- The RIGHT JOIN keyword returns all records from the right table (table2) and all
-- records from the left table (table1) that match the right table (table2). The result is
-- NULL from the left side when there is no match.
select c.*,p.category,p.title from posts p 
    right join categories c on c.pk=p.category;

-- FULL OUTER JOIN 
-- is the combination of what we would have if we put together the right
-- join and the left join.
select c.pk,c.title,p.pk,p.title from 
    categories c full outer join new_posts p 
    on p.category=c.pk;

-- LATERAL JOIN 
-- is a type of join in SQL that allows you to join a table with a subquery, where the
-- subquery is run for each row of the main table. The subquery is executed before joining the rows
-- and the result is used to join the rows. With this join mode, you can use information from one
-- table to filter or process data from another table.
select u.username,q.* from 
    users u join lateral 
    (select author,title,likes from posts p where u.pk=p.author and likes > 2 )
     as q on true;


