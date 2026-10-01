-- ==========================================
-- MySQL / MariaDB Sample Schema & Queries
-- ==========================================

CREATE DATABASE IF NOT EXISTS `birthdays`;
USE `birthdays`;

DROP TABLE IF EXISTS `tourneys`;
CREATE TABLE `tourneys` (
    name VARCHAR(30),
    wins REAL,
    best REAL,
    size REAL
);

INSERT INTO `tourneys` (name, wins, best, size) VALUES
    ('Dolly', 7, 245, 8.5),
    ('Etta', 4, 283, 9),
    ('Irma', 9, 266, 7),
    ('Barbara', 2, 197, 7.5),
    ('Gladys', 13, 273, 8);

DROP TABLE IF EXISTS `dinners`;
CREATE TABLE `dinners` (
    name VARCHAR(30),
    birthdate DATE,
    entree VARCHAR(30),
    side VARCHAR(30),
    dessert VARCHAR(30)
);

INSERT INTO `dinners` (name, birthdate, entree, side, dessert) VALUES
    ('Dolly', '1946-01-19', 'steak', 'salad', 'cake'),
    ('Etta', '1938-01-25', 'chicken', 'fries', 'ice cream'),
    ('Irma', '1941-02-18', 'tofu', 'fries', 'cake'),
    ('Barbara', '1948-12-25', 'tofu', 'salad', 'ice cream'),
    ('Gladys', '1944-05-28', 'steak', 'fries', 'ice cream');

-- Pet Schema Sample
DROP TABLE IF EXISTS `pet`;
CREATE TABLE `pet` (
    name VARCHAR(20),
    owner VARCHAR(20),
    species VARCHAR(20),
    sex CHAR(1),
    birth DATE,
    death DATE
);

INSERT INTO `pet` VALUES
    ('Puffball', 'Diane', 'hamster', 'f', '1999-03-30', NULL);
