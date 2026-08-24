package luke;

import java.util.Scanner;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.EnumMap;
import java.util.List;

import luke.commands.AddTaskCommands;
import luke.commands.Command;
import luke.commands.Commands;
import luke.exceptions.InvalidArgumentException;
import luke.exceptions.InvalidCommandException;
import luke.exceptions.InvalidFlagException;
import luke.exceptions.UserInputException;
import luke.tasks.Flag;
import luke.tasks.ItemList;
import luke.tasks.TaskType;
import luke.tasks.TaskTypes;

/**
 * A small command-line chatbot. It greets the user, then reads lines of input
 * in a loop. Each line is parsed into a {@link Command}, an argument string,
 * and any flags such as {@code /by}, {@code /from}, or {@code /to}. Invalid user
 * input is reported as a normal chatbot error. Type {@code bye} to exit.
 */
public class Luke {
    private static final String CHATBOT_NAME = "Luke";

    // ANSI escape codes. These are special strings the terminal reads as
    // "start colouring text" / "stop colouring text" rather than printing them.
    // We use them to make the bot's replies visually distinct from what you type.
    private static final String DEFAULT_BOT_COLOR = "\u001B[36m"; // cyan
    private static final String ERROR_BOT_COLOR = "\u001B[31m";   // red
    private static final String RESET_BOT_COLOR = "\u001B[0m";    // back to normal

    private final ItemList items = new ItemList();

    /**
     * Creates a chatbot with an empty task list.
     */
    public Luke() {
    }

    /**
     * Gives commands access to the shared item list.
     *
     * @return the task list owned by this chatbot
     */
    public ItemList items() {
        return items;
    }

    private static void printHoriLine() {
        System.out.println("-".repeat(60));
    }

    private static void printBanner() {
        String banner = " _         _        \n"
                + "| |  _   _| | _____ \n"
                + "| | | | | | |/ / _ \\\n"
                + "| |_| |_| |   <  __/\n"
                + "|_____\\__,_|_|\\_\\___|\n";
        System.out.println(banner);
    }

    /**
     * Prints a normal chatbot message, followed by a divider.
     *
     * @param message text to show to the user
     */
    public void say(String message) {
        message = message.strip(); // strip trailing \n
        System.out.println(DEFAULT_BOT_COLOR + message + RESET_BOT_COLOR);
        printHoriLine();
    }

    /**
     * Prints a user-facing error message, followed by a divider.
     *
     * @param message error text to show to the user
     */
    public void error(String message) {
        message = message.strip(); // strip trailing \n
        System.out.println(ERROR_BOT_COLOR + message + RESET_BOT_COLOR);
        printHoriLine();
    }

    private void greet() {
        printBanner();
        say("Hello! I'm %s.\nWhat can I do for you?".formatted(CHATBOT_NAME));
    }

    /**
     * A parsed user input line, ready to execute.
     */
    private static class Invocation {
        Command cmd;
        String arg;
        EnumMap<Flag, String> flags;

        Invocation(Command cmd, String arg, EnumMap<Flag, String> flags) {
            this.cmd = cmd;
            this.arg = arg;
            this.flags = flags;
        }
    }

    /**
     * Resolves the first word of an input line into a command.
     *
     * @param keyword the first word typed by the user
     * @return a built-in command or a task-creation command
     * @throws InvalidCommandException if the keyword is not recognised
     */
    private Command parseCommand(String keyword) throws InvalidCommandException {
        Command command = Commands.fromKeyword(keyword);
        if (command != null) {
            return command;
        }

        TaskType taskType = TaskTypes.fromKeyword(keyword);
        if (taskType != null) {
            return new AddTaskCommands(taskType);
        }

        throw new InvalidCommandException(keyword);
    }

    /**
     * Records where one flag appears in the raw argument text.
     */
    private static class FlagMatch {
        private final String keyword;
        private final int start;
        private final int valueStart;

        FlagMatch(String keyword, int start, int valueStart) {
            this.keyword = keyword;
            this.start = start;
            this.valueStart = valueStart;
        }
    }

    /**
     * Splits a raw input line into a command, plain argument text, and parsed flags.
     *
     * @param line the full user input line, without surrounding whitespace
     * @return the parsed invocation to execute
     * @throws UserInputException if the command or flags are invalid
     */
    private Invocation parseUserInput(String line) throws UserInputException {
        
        Command cmd;
        String arg;
        EnumMap<Flag, String> flags = new EnumMap<>(Flag.class);

        // Split the line into [command] [rest]
        String[] parts = line.split(" ", 2);
        String keyword = parts[0];
        String rest = parts.length > 1 ? parts[1] : "";

        cmd = parseCommand(keyword);

        // A flag starts at the beginning of the argument text or after whitespace.
        Pattern flagPattern = Pattern.compile("(^|\\s)/(\\w+)");
        Matcher matcher = flagPattern.matcher(rest);
        List<FlagMatch> flagMatches = new ArrayList<>();

        while (matcher.find()) {
            flagMatches.add(new FlagMatch(
                    matcher.group(2),
                    matcher.start() + matcher.group(1).length(),
                    matcher.end()));
        }

        if (flagMatches.isEmpty()) {
            arg = rest.trim();
        } else {
            arg = rest.substring(0, flagMatches.get(0).start).trim();

            for (int i = 0; i < flagMatches.size(); i++) {
                FlagMatch flagMatch = flagMatches.get(i);
                int valueEnd = i + 1 < flagMatches.size()
                        ? flagMatches.get(i + 1).start
                        : rest.length();
                String value = rest.substring(flagMatch.valueStart, valueEnd).trim();
                Flag flag = Flag.fromKeyword(flagMatch.keyword);

                if (flag == null) {
                    throw InvalidFlagException.unidentified(flagMatch.keyword);
                }
                if (value.isEmpty()) {
                    throw InvalidFlagException.missingValue(flagMatch.keyword);
                }
                if (flags.put(flag, value) != null) {
                    throw InvalidFlagException.duplicate(flagMatch.keyword);
                }
            }
        }

        return new Invocation(cmd, arg, flags);
    }

    /**
     * Reads and runs commands until the user exits or the input ends.
     */
    private void run() {
        greet();

        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.print("> ");
            if (!sc.hasNextLine()) {
                break;
            }
            String line = sc.nextLine().strip();
            if (line.isEmpty()) {
                continue; // ignore blanks
            }
            
            try {
                if (line.chars().anyMatch(Character::isISOControl)) {
                    throw InvalidArgumentException.unsupportedControlCharacter();
                }
                Invocation inv = parseUserInput(line);
                inv.cmd.execute(this, inv.arg, inv.flags);
                if (inv.cmd.isExit()) {
                    return;
                }
            } catch (UserInputException e) {
                this.error(e.getMessage());
            }
        }
    }

    /**
     * Starts the chatbot.
     *
     * @param args command-line arguments; currently unused
     */
    public static void main(String[] args) {
        new Luke().run();
    }
}
