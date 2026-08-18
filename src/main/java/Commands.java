/**
 * All the commands the chatbot understands, in one place. Each constant carries
 * the keyword that triggers it and provides its own {@link Command#execute} body,
 * so a command's keyword and behaviour live together. Looking a keyword up is done
 * here via {@link #fromKeyword}, so the rest of the program has no second list to
 * keep in sync.
 */
public enum Commands implements Command {
    /** Says goodbye and ends the program. */
    BYE("bye") {
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
    LIST("list") {
        @Override
        public void execute(Luke bot, String argument) {
            ItemList items = bot.items();
            if (items.isEmpty()) {
                bot.say("No items added.");
            } else {
                bot.say(items.format_all_items());
            }
        }
    },

    /** Marks a specific task as done. */
    MARK("mark") {
        @Override
        public void execute(Luke bot, String argument) {
            try {
                int item_idx = Integer.parseInt(argument) - 1; // 1-based to 0-based
                ItemList items = bot.items();
                items.setCompletion(item_idx, true);
                bot.say("Nice! I've marked this task as done:\n %s".formatted(
                    items.format_one_item(item_idx)
                ));
            } catch (NumberFormatException e) {
                bot.error("`mark` command received invalid index: %s".formatted(argument));
            } catch (IndexOutOfBoundsException e) {
                bot.error("`mark` command received out-of-bounds index: %s".formatted(argument));
            }
        }
    },

    /** Marks a specific task as not done. */
    UNMARK("unmark") {
        @Override
        public void execute(Luke bot, String argument) {
            try {
                int item_idx = Integer.parseInt(argument) - 1;
                ItemList items = bot.items();
                items.setCompletion(item_idx, false);
                bot.say("OK! I've marked this task as not done yet:\n %s".formatted(
                    items.format_one_item(item_idx)
                ));
            } catch (NumberFormatException e) {
                bot.error("`unmark` command received invalid index: %s".formatted(argument));
            } catch (IndexOutOfBoundsException e) {
                bot.error("`unmark` command received out-of-bounds index: %s".formatted(argument));
            }
        }
    },

    /**
     * Fallback: any input that isn't a known keyword is added as an item.
     * Its keyword is {@code null} so it's never matched by {@link #fromKeyword}
     * directly — it's only ever returned as the default.
     */
    ADD(null) {
        @Override
        public void execute(Luke bot, String argument) {
            bot.items().add(argument);
            bot.say("added: %s".formatted(argument));
        }
    };

    /** The word the user types to trigger this command (null for the fallback). */
    private final String keyword;

    Commands(String keyword) {
        this.keyword = keyword;
    }

    /**
     * Returns the command whose keyword matches the given word (case-insensitive),
     * or {@link #ADD} if none matches.
     */
    static Command fromKeyword(String word) {
        for (Commands command : values()) {
            if (word.equalsIgnoreCase(command.keyword)) {
                return command;
            }
        }
        return ADD;
    }
}
