#!/usr/bin/env python3
"""Create IntelliGeek schema tables."""
from __future__ import annotations

import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from _cgiutil import (  # noqa: E402
    echo,
    html_start,
    mysql_close,
    mysql_connect,
    mysql_error,
    mysql_query,
    mysql_select_db,
)

html_start("text/plain")
connect = mysql_connect("192.168.1.6", "geek", "geek")
echo("connected to mysql")
mysql_select_db("geek", connect)
echo("connected to db")

tables = [
    "create table  IF NOT EXISTS  faq(faqid int primary key AUTO_INCREMENT,question varchar(350),answer varchar(400),emailid varchar(40),emailidans varchar(40),date varchar(20))",
    "create table  IF NOT EXISTS  user(user_id int Primary key AUTO_INCREMENT,username varchar(25) UNIQUE NOT NULL ,password varchar(20)NOT NULL,email varchar(30) unique,mobile varchar(15), gender varchar(10), dob varchar(10))",
    "CREATE TABLE  IF NOT EXISTS  upload(id INT NOT NULL AUTO_INCREMENT,name VARCHAR(30) NOT NULL,type VARCHAR(30) NOT NULL,size INT NOT NULL,content MEDIUMBLOB NOT NULL,username varchar(25) unique,PRIMARY KEY(id))",
    "CREATE TABLE  IF NOT EXISTS  question(qb_id bigint(20) unsigned PRIMARY KEY auto_increment,sub_id int(10) unsigned NOT NULL,level tinyint(3) unsigned NOT NULL,q tinytext NOT NULL,a tinytext ,b tinytext ,c tinytext,d tinytext,ans tinyint(3) unsigned NOT NULL)",
    "CREATE TABLE  IF NOT EXISTS  subject(sub_id int(10) unsigned PRIMARY KEY  auto_increment,sub varchar(50) NOT NULL )",
    "CREATE TABLE   IF NOT EXISTS usl_score(user_id int(11) NOT NULL,sub_id int(10) unsigned NOT NULL,level bit(1) NOT NULL,score_gt int(10) unsigned default NULL,score_yr int(10) unsigned default NULL,score_mth int(10) unsigned default NULL,score_wk int(10) unsigned default NULL,score_dy int(10) unsigned default NULL)",
    "CREATE TABLE  IF NOT EXISTS  emailid(eid int(5) primary key AUTO_INCREMENT,emailid varchar(50))",
    "CREATE TABLE IF NOT EXISTS newsfeed(id int Primary key AUTO_INCREMENT,category tinytext,title tinytext,summary tinytext, date varchar(25))",
    "CREATE TABLE IF NOT EXISTS onlineuser(userid int primary key auto_increment, username varchar(30), datetime varchar(50))",
]
names = ["FAQ", "user", "upload", "question", "subject", "user score", "Emailid", "newsfeed", "onlineuser"]
for sql, name in zip(tables, names):
    try:
        mysql_query(sql, connect)
    except Exception:
        echo(f"Cannot create Table {name}")
        raise SystemExit(1)
echo("Tables Created")
mysql_close(connect)
