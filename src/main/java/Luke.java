import java.util.Map;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * A small command-line chatbot. It greets the user, then reads lines of input
 * in a loop: recognised commands run their own action, and anything else is
 * added to a running list of items. Type "bye" to exit.
 */
public class Luke {
    private static final String CHATBOT_NAME = "Luke";

    // ANSI escape codes. These are special strings the terminal reads as
    // "start colouring text" / "stop colouring text" rather than printing them.
    // We use them to make the bot's replies visually distinct from what you type.
    private static final String BOT_COLOR = "\u001B[36m"; // cyan
    private static final String RESET_BOT_COLOR = "\u001B[0m";      // back to normal

    // List of commands for the chatbot, anything not implemented defaults to `addItem`.
    private final Map<String, Runnable> commands = Map.of(
        "bye", Luke::bye,
        "list", this::list
    );

    private ArrayList<String> itemList = new ArrayList<>();


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
    private static void say(String message) {
        message = message.strip(); // strip trailing \n
        System.out.println(BOT_COLOR + message + RESET_BOT_COLOR);
        printHoriLine();
    }


    private static void greet() {
        printBanner();
        say("Hello! I'm %s.\nWhat can I do for you?".formatted(CHATBOT_NAME));
    }


    private static void bye() {
        say("Bye. Hope to see you again soon!");
    }


    private void addItem(String item) {
        itemList.add(item);
        say("added: %s".formatted(item));
    }
    
    // Print all items in an ordered format
    private void list() {
        if (itemList.size() == 0) {
            say("No items added.");
            return;
        }

        ArrayList<String> lines = new ArrayList<>();
        for (int i = 0; i < itemList.size(); i++) {
            lines.add("%d. %s".formatted(i + 1, itemList.get(i)));
        }
        say(String.join("\n", lines));
    }


    public static void main(String[] args) {
        Luke chatbot = new Luke();
        chatbot.greet();

        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.print("> ");
            if (!sc.hasNextLine()) {
                break;
            }
            String line = sc.nextLine().strip();
            if (line.equals("")) {
                continue; // ignore blanks
            }

            // Look up the command; anything unrecognised is added as an item.
            // Command lookup is case-insensitive, but the item keeps its original case.
            Runnable cmd = chatbot.commands.getOrDefault(
                line.toLowerCase(),
                () -> chatbot.addItem(line)
            );
            cmd.run();

            if (line.toLowerCase().equals("bye")) {
                return;
            }
        }
    }
}
