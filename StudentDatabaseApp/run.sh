#!/bin/sh
# Run the Student Database App. Run ./compile.sh first.
# Needs lib/mysql-connector-j-8.3.0.jar (see README).
set -e
cd "$(dirname "$0")"
java -cp "bin:lib/mysql-connector-j-8.3.0.jar" StudentDatabaseApp.StudentDatabaseApp
