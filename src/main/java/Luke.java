public class Luke {
    private static String CHATBOT_NAME = "Luke";

    private static void print_hori_line() {
        System.out.println("-".repeat(60));
    }

    private static void print_banner() {
        String banner = " _         _        \n"
                + "| |  _   _| | _____ \n"
                + "| | | | | | |/ / _ \\\n"
                + "| |_| |_| |   <  __/\n"
                + "|_____\\__,_|_|\\_\\___|\n";
        System.out.println(banner);
    }

    private static void greet() {
        print_banner();
        String greet = "Hello! I'm %s.\n".formatted(CHATBOT_NAME)
                     + "What can I do for you?";
        System.out.println(greet);
        print_hori_line();
    }

    private static void bye() {
        String bye = "Bye. Hope to see you again soon!";
        System.out.println(bye);
        print_hori_line();
    }

    public static void main(String[] args) {
        greet();
        bye();
    }
}
