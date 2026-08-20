import java.util.EnumSet;

/**
 * Describes a kind of task that can be stored in the task list.
 */
public interface TaskType {
    /**
     * Returns the single-character label used when displaying this task type.
     *
     * @return display character such as {@code T}, {@code D}, or {@code E}
     */
    char getChar();

    /**
     * Returns the flags required when creating this task type.
     *
     * @return required flags; empty for task types with no required flags
     */
    EnumSet<Flag> getFlags();
}
