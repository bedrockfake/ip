/**
 * All the commands the chatbot understands, in one place. Each constant provides
 * its own {@link Command#execute} body, so a command's behaviour lives right next
 * to it without needing a separate file per command. If one command later grows
 * large, it can be promoted to its own class implementing {@link Command}.
 */
public enum Commands implements Command {
    /** Says goodbye and ends the program. */
    BYE {
        @Override
        public void execute(Luke bot, String argument) {
            bot.say("Bye. Hope to see you again soon!");
        }

        @Override
        public boolean isExit() {
            return true;
        }
    },

    /** Shows all items, or a placeholder message if there are none yet. */
    LIST {
        @Override
        public void execute(Luke bot, String argument) {
            ItemList items = bot.items();
            if (items.isEmpty()) {
                bot.say("No items added.");
            } else {
                bot.say(items.format());
            }
        }
    },

    /** Fallback: any input that isn't a known keyword is added as an item. */
    ADD {
        @Override
        public void execute(Luke bot, String argument) {
            bot.items().add(argument);
            bot.say("added: %s".formatted(argument));
        }
    }
}
