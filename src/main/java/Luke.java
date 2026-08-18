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
    private static final String DEFAULT_BOT_COLOR = "\u001B[36m"; // cyan
    private static final String ERROR_BOT_COLOR = "\u001B[31m";   // red
    private static final String RESET_BOT_COLOR = "\u001B[0m";    // back to normal

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
        System.out.println(DEFAULT_BOT_COLOR + message + RESET_BOT_COLOR);
        printHoriLine();
    }

    void error(String message) {
        message = message.strip(); // strip trailing \n
        System.out.println(ERROR_BOT_COLOR + message + RESET_BOT_COLOR);
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

            // Split the line into [command] [argument]
            String[] parts = line.split(" ", 2);
            String keyword = parts[0];
            String rest = parts.length > 1 ? parts[1] : "";

            // The enum owns the keyword->command lookup (case-insensitive), so
            // there's no second list here to keep in sync. An unknown keyword
            // comes back as ADD, which treats the whole line as the item text.
            Command cmd = Commands.fromKeyword(keyword);
            String argument = (cmd == Commands.ADD) ? line : rest;

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
