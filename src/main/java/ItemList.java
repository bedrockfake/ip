import java.util.ArrayList;
import java.util.List;

/**
 * Stores the items the user has added and knows how to present them as a
 * numbered list. This class deals only with the data; deciding how to display
 * it (colours, dividers, etc.) is left to the chatbot, so the two concerns can
 * change independently.
 */
public class ItemList {
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

    private class Item {
        String name;
        Checkbox completed;

        Item(String name) {
            this.name = name;
            this.completed = Checkbox.NotDone;
        }

        private void setCompletion(boolean done) {
            this.completed = done ? Checkbox.Done : Checkbox.NotDone;
        }
    };

    private final List<Item> items = new ArrayList<>();

    /** Adds an item to the end of the list. `completed` defaults to NotDone. */
    public void add(String name) {
        items.add(new Item(name));
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

    /** Returns true if no items have been added yet. */
    public boolean isEmpty() {
        return items.isEmpty();
    }

    /**
     * Returns a single item formatted with its checkbox, e.g. "[X] read book".
     *
     * @param item_idx 0-based index of the item to format
     * @return the item's checkbox-and-name string (no leading number)
     */
    public String format_one_item(int item_idx) {
        Item item = items.get(item_idx);
        return "[%c] %s".formatted(
            item.completed.getChar(),
            item.name
        );
    }

    /**
     * Returns the items with checkboxes as a numbered list, one per line,
     * e.g. "1. [X] read book\n2. [ ] write essay".
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
