import java.util.Map;
import java.util.Scanner;

/**
 * A small command-line chatbot. It greets the user, then reads lines of input
 * in a loop: recognised commands run their own action, and anything else is
 * simply echoed back. Type "bye" to exit.
 */
public class Luke {
    private static final String CHATBOT_NAME = "Luke";

    // ANSI escape codes. These are special strings the terminal reads as
    // "start colouring text" / "stop colouring text" rather than printing them.
    // We use them to make the bot's replies visually distinct from what you type.
    private static final String BOT_COLOR = "\u001B[36m"; // cyan
    private static final String RESET_BOT_COLOR = "\u001B[0m";      // back to normal

    // List of commands for the chatbot, anything not implemented defaults to `echo`.
    private static final Map<String, Runnable> commands = Map.of(
        "bye", Luke::bye
    );


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


    public static void echo(String msg) {
        say(msg);
    }


    public static void main(String[] args) {
        greet();

        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.print("> ");
            if (!sc.hasNextLine()) {
                break;
            }
            String line = sc.nextLine();

            // Look up the command, fall back to echoing the line.
            // Case sensitivity does not matter for commands, but is kept for messages (e.g. `echo`)
            Runnable cmd = commands.getOrDefault(line.toLowerCase(), () -> echo(line));
            cmd.run();

            if (line.toLowerCase().equals("bye")) {
                return;
            }
        }
    }
}
