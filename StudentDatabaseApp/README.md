# Student Database Application

A Java console application for a university database administrator to manage
student enrollments and tuition balances. Students are created with a
5-digit ID (the first digit is their grade level), enroll in courses at
$600 each, and can view and pay their tuition balance.

Students and their enrollments are **stored in a MySQL database** (via JDBC),
so they persist between runs. If no database is reachable, the app still runs
in memory-only mode, exactly as it did before.

## Tech stack

- Java 17+ (plain Java, no framework, no build tool)
- MySQL 8 (or MariaDB 10+) — MySQL Connector/J 8.3.0
- JDBC — plain `PreparedStatement` SQL, no ORM
- JUnit 5 — unit tests for the student logic, integration tests for the database layer

## Project layout

```
StudentDatabaseApp/
├── README.md                     This file
├── schema.sql                    Database and tables (run once)
├── db.properties.example         Example database config (copy to db.properties)
├── compile.sh                    Compile the app and run the tests
├── run.sh                        Run the app
├── src/StudentDatabaseApp/
│   ├── StudentDatabaseApp.java   Console UI (main)
│   ├── Student.java              Student model: ID, enrollment, tuition
│   └── Database.java             MySQL persistence (JDBC)
└── test/StudentDatabaseApp/
    ├── StudentTest.java          Unit tests (no database needed)
    └── DatabaseTest.java         Integration tests (needs MySQL)
```

## Database schema

- `students` — `id`, `first_name`, `last_name`, `grade_year`,
  `student_id` (unique, e.g. `21001`), `tuition_balance`
- `enrollments` — `id`, `student_id` (foreign key to `students`),
  `course_name`, `cost`

## Setup

### 1. Create the database

```sh
mysql -u root -p < schema.sql
```

This creates the `student_db` database and both tables. It is safe to run
more than once. (The app also creates the tables itself on startup, so this
step is only needed if you want the schema in place first.)

### 2. Download the libraries (not included in the repo)

- MySQL Connector/J 8.3.0 — `mysql-connector-j-8.3.0.jar`
  from https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.3.0/
- JUnit Platform Console Standalone 1.10.3 — only needed to run the tests
  from https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/1.10.3/

Put both jars in a `lib/` folder inside this project:

```sh
mkdir -p lib
# download the two jars above into lib/
```

### 3. Configure the connection (no password is hard-coded)

Set environment variables, or copy `db.properties.example` to
`db.properties` (it is git-ignored — never commit real credentials):

| Variable      | db.properties key | Default                  |
| ------------- | ----------------- | ------------------------ |
| `DB_HOST`     | `db.host`         | `localhost`              |
| `DB_PORT`     | `db.port`         | `3306`                   |
| `DB_NAME`     | `db.name`         | `student_db`             |
| `DB_USER`     | `db.user`         | `root`                   |
| `DB_PASSWORD` | `db.password`     | *(empty)*                |

Example:

```sh
export DB_HOST=localhost DB_USER=root DB_PASSWORD=yourpassword
```

## Run it

```sh
chmod +x compile.sh run.sh   # first time only (executable bits are not stored by the GitHub upload)
./compile.sh   # compile the app (and the tests)
./run.sh       # run the app
```

Or by hand:

```sh
javac -cp "lib/mysql-connector-j-8.3.0.jar" -d bin $(find src -name "*.java")
java -cp "bin:lib/mysql-connector-j-8.3.0.jar" StudentDatabaseApp.StudentDatabaseApp
```

On startup the app connects to MySQL and loads any students already saved.
If it cannot connect, it prints a warning and runs in memory-only mode.

## Example session

```
Connected to the MySQL database.
Enter number of new students to enroll: 1
Enter student first name: Ada
Enter student last name: Lovelace
1 - Freshman
2 - Sophomore
3 - Junior
4 - Senior
Enter student class level: 2
Enter course to enroll (Q to quit): History 101
Enter course to enroll (Q to quit): Mathematics 101
Enter course to enroll (Q to quit): Q
Your balance is: $1200
Enter your payment: $600
Thank you for your payment of $600
Your balance is: $600
Name: Ada Lovelace
Grade Level: 2
Student ID: 21001
Courses Enrolled:
 History 101
 Mathematics 101
Balance: $600
```

Run it again and Ada is still there — loaded from the database — and the
next student gets the next ID (`21002`), because IDs come from the database
instead of a counter that resets on every restart.

## Tests

```sh
./compile.sh   # compiles the tests and runs them
```

- `StudentTest` — pure logic, no database needed: ID generation, course
  costs ($600 per course), payments, and the status output.
- `DatabaseTest` — needs a running MySQL server. It uses its own
  `student_db_test` database so it never touches real data, and it is
  skipped automatically when no database is reachable. Configure it with
  `DB_TEST_URL` / `DB_USER` / `DB_PASSWORD`.
