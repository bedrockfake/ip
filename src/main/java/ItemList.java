import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Stores the items the user has added and knows how to present them as a
 * numbered list. This class deals only with the data; deciding how to display
 * it (colours, dividers, etc.) is left to the chatbot, so the two concerns can
 * change independently.
 */
public class ItemList {
    /**
     * Completion state shown in the second display bracket.
     */
    private enum Checkbox {
        Done, 
        NotDone;

        private char getChar() {
            switch (this) {
                case Done:
                    return 'X';
                case NotDone:
                    return ' ';
                default:
                    throw new AssertionError("Unknown Checkbox value");
            }
        }
    };

    /**
     * One stored task and its display state.
     */
    private static class Item {
        String name;
        TaskType tasktype;
        Checkbox completed;
        EnumMap<Flag, String> flags;

        Item(String name, TaskType tasktype, EnumMap<Flag, String> flags) {
            this.name = name;
            this.tasktype = tasktype;
            this.completed = Checkbox.NotDone;
            this.flags = flags;
        }

        private void setCompletion(boolean done) {
            this.completed = done ? Checkbox.Done : Checkbox.NotDone;
        }
    };

    private final List<Item> items = new ArrayList<>();

    /**
     * Creates an empty task list.
     */
    public ItemList() {
    }

    /**
     * Adds an item to the end of the list. Completion defaults to not done.
     *
     * @param name task description
     * @param tasktype task type such as todo, deadline, or event
     * @param flags flag values associated with the task
     */
    public void add(String name, TaskType tasktype, EnumMap<Flag, String> flags) {
        items.add(new Item(name, tasktype, flags));
    }

    /**
     * Removes the item at the given position.
     *
     * @param item_idx 0-based index of the item to remove
     */
    public void remove(int item_idx) {
        items.remove(item_idx);
    }

    /**
     * Marks the item at the given position as done or not done.
     *
     * @param item_idx 0-based index of the item (0 is the first item)
     * @param done     true to mark it done, false to mark it not done
     */
    public void setCompletion(int item_idx, boolean done) {
        items.get(item_idx).setCompletion(done);
    }

    /**
     * Returns whether the list has no items.
     *
     * @return {@code true} if no tasks have been added
     */
    public boolean isEmpty() {
        return items.isEmpty();
    }

    /**
     * Returns the number of stored items.
     *
     * @return task count
     */
    public int size() {
        return items.size();
    }

    /**
     * Formats flags for display after the task description.
     *
     * @param flags flag values to format
     * @return an empty string if there are no flags; otherwise a parenthesised string
     */
    public String format_flags(EnumMap<Flag, String> flags) {
        if (flags.isEmpty()) {
            return "";
        } else {
            return " (%s)".formatted(
                flags.entrySet().stream()
                .map(entry -> "%s: %s".formatted(
                        entry.getKey().name().toLowerCase(),
                        entry.getValue()))
                .collect(Collectors.joining(" "))
            );
        }
    }

    /**
     * Returns a single item formatted with its type, checkbox, description, and flags.
     *
     * @param item_idx 0-based index of the item to format
     * @return the formatted item without a leading list number
     */
    public String format_one_item(int item_idx) {
        Item item = items.get(item_idx);
        return "[%c][%c] %s%s".formatted(
            item.tasktype.getChar(),
            item.completed.getChar(),
            item.name,
            format_flags(item.flags)
        );
    }

    /**
     * Returns the items with checkboxes as a numbered list, one per line,
     * e.g. "1. [X] read book\n2. [ ] write essay".
     *
     * @return all items formatted as a numbered list
     */
    public String format_all_items() {
        List<String> lines = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            lines.add("%d. %s".formatted(
                i + 1,
                format_one_item(i)
            ));
        }
        return String.join("\n", lines);
    }
}
