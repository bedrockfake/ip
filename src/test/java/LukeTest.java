import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * Simple dependency-free tests for Luke's command-line behaviour.
 * Run with:
 * javac -d out $(find src/main/java src/test/java -name "*.java")
 * java -cp out LukeTest
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
        runTest("accepts case-insensitive keywords and flags", this::acceptsCaseInsensitiveKeywordsAndFlags);
        runTest("rejects malformed task input", this::rejectsMalformedTaskInput);
        runTest("rejects missing flag values", this::rejectsMissingFlagValues);
        runTest("rejects extra arguments for no-argument commands", this::rejectsExtraArguments);
        runTest("marks and unmarks existing task", this::marksAndUnmarksExistingTask);
        runTest("rejects bad mark indexes", this::rejectsBadMarkIndexes);
        runTest("shows placeholder for empty list", this::showsPlaceholderForEmptyList);
        runTest("rejects unsupported control characters", this::rejectsUnsupportedControlCharacters);

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
        PrintStream originalOut = System.out;
        java.io.InputStream originalIn = System.in;
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try {
            System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
            System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
            Luke.main(new String[0]);
            return output.toString(StandardCharsets.UTF_8);
        } finally {
            System.setIn(originalIn);
            System.setOut(originalOut);
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
