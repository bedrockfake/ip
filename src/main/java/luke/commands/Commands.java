package luke.commands;

import java.util.EnumMap;

import luke.Luke;
import luke.exceptions.InvalidArgumentException;
import luke.exceptions.InvalidFlagException;
import luke.exceptions.UserInputException;
import luke.tasks.Flag;
import luke.tasks.ItemList;

/**
 * All the commands the chatbot understands, in one place. Each constant carries
 * the keyword that triggers it and provides its own {@link Command#execute} body,
 * so a command's keyword and behavior live together. Looking a keyword up is done
 * here via {@link #findByKeyword}, so the rest of the program has no second list to
 * keep in sync.
 */
public enum Commands implements Command {
    /** Says goodbye and ends the program. */
    BYE("bye") {
        @Override
        public void execute(Luke bot, String argument, EnumMap<Flag, String> flags)
                throws UserInputException {
            requireNoArgument("bye", argument);
            requireNoFlags(flags);
            bot.say("Bye. Hope to see you again soon!");
        }

        @Override
        public boolean shouldExit() {
            return true;
        }
    },

    /** Shows all items, or a placeholder message if there are none yet. */
    LIST("list") {
        @Override
        public void execute(Luke bot, String argument, EnumMap<Flag, String> flags)
                throws UserInputException {
            requireNoArgument("list", argument);
            requireOnlySortFlag(flags);

            ItemList items = bot.getItems();
            if (items.isEmpty()) {
                bot.say("No items added.");
            } else if (flags.containsKey(Flag.SORT)) {
                bot.say(items.formatAllItemsSortedByTime());
            } else {
                bot.say(items.formatAllItems());
            }
        }
    },

    /** Marks a specific task as done. */
    MARK("mark") {
        @Override
        public void execute(Luke bot, String argument, EnumMap<Flag, String> flags)
                throws UserInputException {
            requireNoFlags(flags);

            try {
                int itemIndex = Integer.parseInt(argument) - 1; // 1-based to 0-based
                ItemList items = bot.getItems();
                items.setCompletion(itemIndex, true);
                bot.say("Nice! I've marked this task as done:\n %s".formatted(
                        items.formatOneItem(itemIndex)
                ));
            } catch (NumberFormatException e) {
                throw InvalidArgumentException.invalidIndex("mark", argument);
            } catch (IndexOutOfBoundsException e) {
                throw InvalidArgumentException.outOfBoundsIndex("mark", argument);
            }
        }

        @Override
        public boolean shouldSaveItemList() {
            return true;
        }
    },

    /** Marks a specific task as not done. */
    UNMARK("unmark") {
        @Override
        public void execute(Luke bot, String argument, EnumMap<Flag, String> flags)
                throws UserInputException {
            requireNoFlags(flags);

            try {
                int itemIndex = Integer.parseInt(argument) - 1; // 1-based to 0-based
                ItemList items = bot.getItems();
                items.setCompletion(itemIndex, false);
                bot.say("OK! I've marked this task as not done yet:\n %s".formatted(
                        items.formatOneItem(itemIndex)
                ));
            } catch (NumberFormatException e) {
                throw InvalidArgumentException.invalidIndex("unmark", argument);
            } catch (IndexOutOfBoundsException e) {
                throw InvalidArgumentException.outOfBoundsIndex("unmark", argument);
            }
        }

        @Override
        public boolean shouldSaveItemList() {
            return true;
        }
    },

    /** Deletes a specific task from the list. */
    DELETE("delete") {
        @Override
        public void execute(Luke bot, String argument, EnumMap<Flag, String> flags)
                throws UserInputException {
            requireNoFlags(flags);

            try {
                int itemIndex = Integer.parseInt(argument) - 1; // 1-based to 0-based
                ItemList items = bot.getItems();
                String removedItem = items.formatOneItem(itemIndex);
                items.remove(itemIndex);
                bot.say("Noted. I've removed this task:\n"
                        + " %s\n".formatted(removedItem)
                        + "Now you have %d tasks in the list.".formatted(items.size())
                );
            } catch (NumberFormatException e) {
                throw InvalidArgumentException.invalidIndex("delete", argument);
            } catch (IndexOutOfBoundsException e) {
                throw InvalidArgumentException.outOfBoundsIndex("delete", argument);
            }
        }

        @Override
        public boolean shouldSaveItemList() {
            return true;
        }
    };

    /** The word the user types to trigger this command. */
    private final String keyword;

    Commands(String keyword) {
        this.keyword = keyword;
    }

    /**
     * Rejects text after commands that do not accept arguments.
     *
     * @param command the command name used in the error message
     * @param argument the text after the command keyword
     * @throws InvalidArgumentException if {@code argument} is not blank
     */
    private static void requireNoArgument(String command, String argument)
            throws InvalidArgumentException {
        if (!argument.isBlank()) {
            throw InvalidArgumentException.unexpected(command, argument);
        }
    }

    /**
     * Rejects flags for commands that do not accept any flags.
     *
     * @param flags parsed flags from the user input
     * @throws InvalidFlagException if any flag is present
     */
    private static void requireNoFlags(EnumMap<Flag, String> flags)
            throws InvalidFlagException {
        if (!flags.isEmpty()) {
            Flag flag = flags.keySet().iterator().next();
            throw InvalidFlagException.unsupported(flag.name().toLowerCase());
        }
    }

    /**
     * Rejects flags other than {@code /sort time} for the list command.
     *
     * @param flags parsed flags from the user input
     * @throws InvalidFlagException if the sort flag is invalid or another flag is present
     */
    private static void requireOnlySortFlag(EnumMap<Flag, String> flags)
            throws InvalidFlagException {
        if (flags.isEmpty()) {
            return;
        }
        if (!flags.containsKey(Flag.SORT)) {
            Flag flag = flags.keySet().iterator().next();
            throw InvalidFlagException.unsupported(flag.name().toLowerCase());
        }
        if (flags.size() > 1) {
            for (Flag flag : flags.keySet()) {
                if (flag != Flag.SORT) {
                    throw InvalidFlagException.unsupported(flag.name().toLowerCase());
                }
            }
        }
        if (!flags.get(Flag.SORT).equalsIgnoreCase("time")) {
            throw InvalidFlagException.unsupportedValue("sort", flags.get(Flag.SORT));
        }
    }

    /**
     * Returns the command whose keyword matches the given word (case-insensitive).
     *
     * @param word the first word typed by the user
     * @return the matching command, or {@code null} if none matches
     */
    public static Command findByKeyword(String word) {
        for (Commands command : values()) {
            if (word.equalsIgnoreCase(command.keyword)) {
                return command;
            }
        }
        return null;
    }
}
