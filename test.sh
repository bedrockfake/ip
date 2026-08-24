set -e

javac -d out $(find src/main/java src/test/java -name "*.java")
java -cp out luke.LukeTest
