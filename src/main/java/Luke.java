import java.util.Map;
import java.util.Scanner;

/**
 * A small command-line chatbot. It greets the user, then reads lines of input
 * in a loop. Each line is turned into a {@link Command}: a recognised keyword
 * runs its own command, and anything else is added as an item. Type "bye" to exit.
 */
public class Luke {
    private static final String CHATBOT_NAME = "Luke";

    // ANSI escape codes. These are special strings the terminal reads as
    // "start colouring text" / "stop colouring text" rather than printing them.
    // We use them to make the bot's replies visually distinct from what you type.
    private static final String BOT_COLOR = "\u001B[36m";       // cyan
    private static final String RESET_BOT_COLOR = "\u001B[0m";  // back to normal

    // Keyword -> command. Anything not listed here falls back to defaultCommand.
    private final Map<String, Command> commands = Map.of(
        "bye", Commands.BYE,
        "list", Commands.LIST
    );

    // Used when the input matches no known keyword: treat the whole line as a new item.
    private final Command defaultCommand = Commands.ADD;

    private final ItemList items = new ItemList();

    /** Gives commands access to the shared item list. */
    ItemList items() {
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

    /** Prints a message as the chatbot: in the bot's colour, followed by a divider. */
    void say(String message) {
        message = message.strip(); // strip trailing \n
        System.out.println(BOT_COLOR + message + RESET_BOT_COLOR);
        printHoriLine();
    }

    private void greet() {
        printBanner();
        say("Hello! I'm %s.\nWhat can I do for you?".formatted(CHATBOT_NAME));
    }

    /** Reads and runs commands until the user exits or the input ends. */
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

            // Split into the keyword (first word) and the rest of the line.
            // Command lookup is case-insensitive; the argument keeps its original case.
            String[] parts = line.split(" ", 2);
            String keyword = parts[0].toLowerCase();
            String rest = parts.length > 1 ? parts[1] : "";

            Command cmd = commands.get(keyword);
            String argument;
            if (cmd == null) {
                cmd = defaultCommand;  // unrecognised: add the whole line as an item
                argument = line;
            } else {
                argument = rest;
            }

            cmd.execute(this, argument);
            if (cmd.isExit()) {
                return;
            }
        }
    }

    public static void main(String[] args) {
        new Luke().run();
    }
}
