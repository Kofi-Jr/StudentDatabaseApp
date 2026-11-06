-- StudentDatabaseApp schema
-- Run with:  mysql -u root -p < schema.sql
-- Creates the database and tables used by the app. Safe to run more
-- than once: everything uses IF NOT EXISTS.

CREATE DATABASE IF NOT EXISTS student_db;
USE student_db;

CREATE TABLE IF NOT EXISTS students (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    first_name       VARCHAR(100) NOT NULL,
    last_name        VARCHAR(100) NOT NULL,
    grade_year       INT NOT NULL,
    student_id       VARCHAR(10) NOT NULL UNIQUE,
    tuition_balance  INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS enrollments (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    student_id  VARCHAR(10) NOT NULL,
    course_name VARCHAR(100) NOT NULL,
    cost        INT NOT NULL,
    FOREIGN KEY (student_id) REFERENCES students (student_id)
);
