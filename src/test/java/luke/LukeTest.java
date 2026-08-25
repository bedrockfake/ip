package luke;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Simple dependency-free tests for Luke's command-line behavior.
 * Run with:
 * javac -d out $(find src/main/java src/test/java -name "*.java")
 * java -cp out luke.LukeTest
 */
public class LukeTest {
    private int passed = 0;
    private int failed = 0;

    public static void main(String[] args) {
        LukeTest tests = new LukeTest();
        tests.runAll();
    }

    private void runAll() {
        runTest("adds and lists supported task types", this::addsAndListsSupportedTaskTypes);
        runTest("accepts case-insensitive keywords and flags",
                this::acceptsCaseInsensitiveKeywordsAndFlags);
        runTest("rejects malformed task input", this::rejectsMalformedTaskInput);
        runTest("rejects missing flag values", this::rejectsMissingFlagValues);
        runTest("rejects extra arguments for no-argument commands", this::rejectsExtraArguments);
        runTest("marks and unmarks existing task", this::marksAndUnmarksExistingTask);
        runTest("deletes existing task", this::deletesExistingTask);
        runTest("persists tasks between sessions", this::persistsTasksBetweenSessions);
        runTest("reports storage failures as chatbot errors", this::reportsStorageFailuresAsChatbotErrors);
        runTest("reports malformed storage data as chatbot error",
                this::reportsMalformedStorageDataAsChatbotError);
        runTest("reports invalid stored task type as chatbot error",
                this::reportsInvalidStoredTaskTypeAsChatbotError);
        runTest("rejects bad mark indexes", this::rejectsBadMarkIndexes);
        runTest("shows placeholder for empty list", this::showsPlaceholderForEmptyList);
        runTest("rejects unsupported control characters",
                this::rejectsUnsupportedControlCharacters);

        System.out.println("%d passed, %d failed".formatted(passed, failed));
        if (failed > 0) {
            System.exit(1);
        }
    }

    private void runTest(String name, TestCase testCase) {
        try {
            testCase.run();
            passed++;
            System.out.println("PASS: " + name);
        } catch (AssertionError e) {
            failed++;
            System.out.println("FAIL: " + name);
            System.out.println("  " + e.getMessage());
        }
    }

    private void addsAndListsSupportedTaskTypes() {
        String output = runLuke("""
                todo read book
                deadline return book /by Sunday
                event project meeting /from Mon 2pm /to 4pm
                list
                bye
                """);

        assertContains(output, "[T][ ] read book");
        assertContains(output, "[D][ ] return book (by: Sunday)");
        assertContains(output, "[E][ ] project meeting (from: Mon 2pm to: 4pm)");
        assertContains(output, "1. [T][ ] read book");
        assertContains(output, "2. [D][ ] return book (by: Sunday)");
        assertContains(output, "3. [E][ ] project meeting (from: Mon 2pm to: 4pm)");
    }

    private void acceptsCaseInsensitiveKeywordsAndFlags() {
        String output = runLuke("""
                ToDo Case Test
                DEADLINE return book /BY Sunday
                EVENT reversed /TO 5pm /FROM 4pm
                list
                bye
                """);

        assertContains(output, "1. [T][ ] Case Test");
        assertContains(output, "2. [D][ ] return book (by: Sunday)");
        assertContains(output, "3. [E][ ] reversed (from: 4pm to: 5pm)");
    }

    private void rejectsMalformedTaskInput() {
        String output = runLuke("""
                todo
                deadline /by someday
                deadline return book /by Sunday /by Monday
                deadline return book /er idk
                todo read book /by Sunday
                deadline return book /to Sunday
                event call /from now /by later
                bye
                """);

        assertContains(output, "Missing description for todo task.");
        assertContains(output, "Missing description for deadline task.");
        assertContains(output, "Duplicate flag: /by");
        assertContains(output, "Unidentified flag: /er");
        assertContains(output, "Unsupported flag: /by");
        assertContains(output, "Unsupported flag: /to");
    }

    private void rejectsMissingFlagValues() {
        String output = runLuke("""
                deadline return book /by
                event call /from /to later
                event call /from now /to
                bye
                """);

        assertContains(output, "Missing value for flag: /by");
        assertContains(output, "Missing value for flag: /from");
        assertContains(output, "Missing value for flag: /to");
    }

    private void rejectsExtraArguments() {
        String output = runLuke("""
                list ijdad
                bye jiadjiajd
                bye
                """);

        assertContains(output, "`list` command does not take arguments: ijdad");
        assertContains(output, "`bye` command does not take arguments: jiadjiajd");
    }

    private void marksAndUnmarksExistingTask() {
        String output = runLuke("""
                todo read book
                mark 1
                unmark 1
                bye
                """);

        assertContains(output, "Nice! I've marked this task as done:");
        assertContains(output, "[T][X] read book");
        assertContains(output, "OK! I've marked this task as not done yet:");
        assertContains(output, "[T][ ] read book");
    }

    private void deletesExistingTask() {
        String output = runLuke("""
                todo idk
                deadline ddl /by 1
                mark 2
                delete 2
                list
                delete 1
                list
                bye
                """);

        assertContains(output, "Noted. I've removed this task:");
        assertContains(output, "[D][X] ddl (by: 1)");
        assertContains(output, "Now you have 1 tasks in the list.");
        assertContains(output, "1. [T][ ] idk");
        assertContains(output, "Now you have 0 tasks in the list.");
        assertContains(output, "No items added.");
    }

    private void persistsTasksBetweenSessions() {
        Path storageFile = createTempStorageFile();
        try {
            runLuke("""
                    todo remember me
                    bye
                    """, storageFile);

            String output = runLuke("""
                    list
                    bye
                    """, storageFile);

            assertContains(output, "1. [T][ ] remember me");
        } finally {
            deleteTempStorageFile(storageFile);
        }
    }

    private void reportsStorageFailuresAsChatbotErrors() {
        Path storageDirectory = createTempStorageDirectory();
        try {
            String output = runLuke("""
                    todo cannot save
                    bye
                    """, storageDirectory);

            assertContains(output, "Failed to load task list from data:");
            assertContains(output, "Failed to save task list to data:");
        } finally {
            deleteTempStorageDirectory(storageDirectory);
        }
    }

    private void reportsMalformedStorageDataAsChatbotError() {
        Path storageFile = createTempStorageFile();
        try {
            writeString(storageFile, "not enough fields");

            String output = runLuke("""
                    list
                    bye
                    """, storageFile);

            assertContains(output, "Failed to load task list from data:");
            assertContains(output, "Storage line has missing fields.");
            assertContains(output, "No items added.");
        } finally {
            deleteTempStorageFile(storageFile);
        }
    }

    private void reportsInvalidStoredTaskTypeAsChatbotError() {
        Path storageFile = createTempStorageFile();
        try {
            writeString(storageFile, "NOPE\t0\tbad");

            String output = runLuke("""
                    list
                    bye
                    """, storageFile);

            assertContains(output, "Failed to load task list from data:");
            assertContains(output, "Storage line has invalid task type.");
            assertContains(output, "No items added.");
        } finally {
            deleteTempStorageFile(storageFile);
        }
    }

    private void rejectsBadMarkIndexes() {
        String output = runLuke("""
                mark banana
                mark 0
                mark -1
                mark 1
                bye
                """);

        assertContains(output, "`mark` command received invalid index: banana");
        assertContains(output, "`mark` command received out-of-bounds index: 0");
        assertContains(output, "`mark` command received out-of-bounds index: -1");
        assertContains(output, "`mark` command received out-of-bounds index: 1");
    }

    private void showsPlaceholderForEmptyList() {
        String output = runLuke("""
                list
                bye
                """);

        assertContains(output, "No items added.");
    }

    private void rejectsUnsupportedControlCharacters() {
        String output = runLuke("event project meeting /from Mon /to Tue\u001B[D\nbye\n");

        assertContains(output, "Input contains unsupported control characters.");
    }

    private String runLuke(String input) {
        Path storageFile = createTempStorageFile();
        try {
            return runLuke(input, storageFile);
        } finally {
            deleteTempStorageFile(storageFile);
        }
    }

    private String runLuke(String input, Path storageFile) {
        PrintStream originalOut = System.out;
        java.io.InputStream originalIn = System.in;
        String originalStoragePath = System.getProperty("luke.storage.path");
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try {
            System.setProperty("luke.storage.path", storageFile.toString());
            System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
            System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
            Luke.main(new String[0]);
            return output.toString(StandardCharsets.UTF_8);
        } finally {
            if (originalStoragePath == null) {
                System.clearProperty("luke.storage.path");
            } else {
                System.setProperty("luke.storage.path", originalStoragePath);
            }
            System.setIn(originalIn);
            System.setOut(originalOut);
        }
    }

    private Path createTempStorageFile() {
        try {
            return Files.createTempDirectory("luke-test").resolve("itemlist.txt");
        } catch (IOException e) {
            throw new AssertionError("Could not create temp storage path", e);
        }
    }

    private Path createTempStorageDirectory() {
        try {
            return Files.createTempDirectory("luke-test-storage-dir");
        } catch (IOException e) {
            throw new AssertionError("Could not create temp storage directory", e);
        }
    }

    private void writeString(Path path, String content) {
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new AssertionError("Could not write temp storage data", e);
        }
    }

    private void deleteTempStorageFile(Path storageFile) {
        try {
            Files.deleteIfExists(storageFile);
            Files.deleteIfExists(storageFile.getParent());
        } catch (IOException e) {
            throw new AssertionError("Could not delete temp storage path", e);
        }
    }

    private void deleteTempStorageDirectory(Path storageDirectory) {
        try {
            Files.deleteIfExists(storageDirectory);
        } catch (IOException e) {
            throw new AssertionError("Could not delete temp storage directory", e);
        }
    }

    private void assertContains(String output, String expected) {
        if (!output.contains(expected)) {
            throw new AssertionError("Expected output to contain: " + expected);
        }
    }

    private interface TestCase {
        void run();
    }
}
