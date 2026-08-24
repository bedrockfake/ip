set -e

javac -cp "lib/gson-2.14.0.jar" -d out $(find src/main/java src/test/java -name "*.java")
java -cp "out:lib/gson-2.14.0.jar" luke.LukeTest