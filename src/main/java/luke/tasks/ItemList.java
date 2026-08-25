package luke.tasks;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Stores the items the user has added and knows how to present them as a
 * numbered list. This class deals only with the data; deciding how to display
 * it (colors, dividers, etc.) is left to the chatbot, so the two concerns can
 * change independently.
 */
public class ItemList {
    /**
     * Completion state shown in the second display bracket.
     */
    private enum Checkbox {
        DONE,
        NOT_DONE;

        private char getChar() {
            switch (this) {
                case DONE:
                    return 'X';
                case NOT_DONE:
                    return ' ';
                default:
                    throw new AssertionError("Unknown Checkbox value");
            }
        }
    }

    /**
     * One stored task and its display state.
     */
    private static class Item {
        String name;
        TaskTypes taskType;
        Checkbox completed;
        EnumMap<Flag, String> flags;

        Item(String name, TaskTypes taskType, EnumMap<Flag, String> flags) {
            this.name = name;
            this.taskType = taskType;
            this.completed = Checkbox.NOT_DONE;
            this.flags = flags;
        }

        private void setCompletion(boolean done) {
            this.completed = done ? Checkbox.DONE : Checkbox.NOT_DONE;
        }
    }

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
     * @param taskType task type such as todo, deadline, or event
     * @param flags flag values associated with the task
     */
    public void add(String name, TaskTypes taskType, EnumMap<Flag, String> flags) {
        items.add(new Item(name, taskType, flags));
    }

    /**
     * Removes the item at the given position.
     *
     * @param itemIndex 0-based index of the item to remove
     */
    public void remove(int itemIndex) {
        items.remove(itemIndex);
    }

    /**
     * Marks the item at the given position as done or not done.
     *
     * @param itemIndex 0-based index of the item (0 is the first item)
     * @param done     true to mark it done, false to mark it not done
     */
    public void setCompletion(int itemIndex, boolean done) {
        items.get(itemIndex).setCompletion(done);
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
     * Returns the description of the item at the given position.
     *
     * @param itemIndex 0-based index of the item
     * @return item description
     */
    public String getName(int itemIndex) {
        return items.get(itemIndex).name;
    }

    /**
     * Returns the task type of the item at the given position.
     *
     * @param itemIndex 0-based index of the item
     * @return item task type
     */
    public TaskTypes getTaskType(int itemIndex) {
        return items.get(itemIndex).taskType;
    }

    /**
     * Returns whether the item at the given position is marked as done.
     *
     * @param itemIndex 0-based index of the item
     * @return {@code true} if the item is done
     */
    public boolean isDone(int itemIndex) {
        return items.get(itemIndex).completed == Checkbox.DONE;
    }

    /**
     * Returns the flag values of the item at the given position.
     *
     * @param itemIndex 0-based index of the item
     * @return copy of the item's flag values
     */
    public EnumMap<Flag, String> getFlags(int itemIndex) {
        return new EnumMap<>(items.get(itemIndex).flags);
    }

    /**
     * Adds an item loaded from storage.
     *
     * @param name task description
     * @param taskType task type such as todo, deadline, or event
     * @param flags flag values associated with the task
     * @param isDone whether the loaded item is marked as done
     */
    public void addLoadedItem(
            String name,
            TaskTypes taskType,
            EnumMap<Flag, String> flags,
            boolean isDone) {
        add(name, taskType, flags);
        setCompletion(size() - 1, isDone);
    }

    /**
     * Formats flags for display after the task description.
     *
     * @param flags flag values to format
     * @return an empty string if there are no flags; otherwise a parenthesized string
     */
    public String formatFlags(EnumMap<Flag, String> flags) {
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
     * @param itemIndex 0-based index of the item to format
     * @return the formatted item without a leading list number
     */
    public String formatOneItem(int itemIndex) {
        Item item = items.get(itemIndex);
        return "[%c][%c] %s%s".formatted(
                item.taskType.getChar(),
                item.completed.getChar(),
                item.name,
                formatFlags(item.flags)
        );
    }

    /**
     * Returns the items with checkboxes as a numbered list, one per line,
     * e.g. "1. [X] read book\n2. [ ] write essay".
     *
     * @return all items formatted as a numbered list
     */
    public String formatAllItems() {
        List<String> lines = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            lines.add("%d. %s".formatted(
                    i + 1,
                    formatOneItem(i)
            ));
        }
        return String.join("\n", lines);
    }
}
