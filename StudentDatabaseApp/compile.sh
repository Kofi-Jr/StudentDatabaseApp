#!/bin/sh
# Compile the app and the tests, then run the tests.
# Needs lib/mysql-connector-j-8.3.0.jar and
# lib/junit-platform-console-standalone-1.10.3.jar (see README).
set -e
cd "$(dirname "$0")"
mkdir -p bin bin-test
javac -cp "lib/mysql-connector-j-8.3.0.jar" -d bin $(find src -name "*.java")
javac -cp "bin:lib/mysql-connector-j-8.3.0.jar:lib/junit-platform-console-standalone-1.10.3.jar" \
  -d bin-test $(find test -name "*.java")
java -jar lib/junit-platform-console-standalone-1.10.3.jar \
  --class-path "bin:bin-test" --scan-class-path
