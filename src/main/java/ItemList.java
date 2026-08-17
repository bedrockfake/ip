import java.util.ArrayList;
import java.util.List;

/**
 * Stores the items the user has added and knows how to present them as a
 * numbered list. This class deals only with the data; deciding how to display
 * it (colours, dividers, etc.) is left to the chatbot, so the two concerns can
 * change independently.
 */
public class ItemList {
    private final List<String> items = new ArrayList<>();

    /** Adds an item to the end of the list. */
    public void add(String item) {
        items.add(item);
    }

    /** Returns true if no items have been added yet. */
    public boolean isEmpty() {
        return items.isEmpty();
    }

    /**
     * Returns the items as a numbered list, one per line,
     * e.g. "1. read book\n2. write essay".
     */
    public String format() {
        List<String> lines = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            lines.add("%d. %s".formatted(i + 1, items.get(i)));
        }
        return String.join("\n", lines);
    }
}
