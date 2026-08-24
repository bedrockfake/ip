package luke.tasks;

/**
 * Supported flag names that can appear after a task description.
 */
public enum Flag {
    /** Deadline due time, written as {@code /by}. */
    BY("by"),

    /** Event start time, written as {@code /from}. */
    FROM("from"),

    /** Event end time, written as {@code /to}. */
    TO("to");

    private final String keyword;

    Flag(String keyword) {
        this.keyword = keyword;
    }

    /**
     * Returns the flag matching a user-typed keyword such as {@code by}.
     *
     * @param word the flag keyword without the leading slash
     * @return the matching flag, or {@code null} if none matches
     */
    public static Flag fromKeyword(String word) {
        for (Flag f : values()) {
            if (word.equalsIgnoreCase(f.keyword)) {
                return f;
            }
        }
        return null;
    }
}
