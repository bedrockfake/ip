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
    TO("to"),

    /** List sorting mode, written as {@code /sort}. */
    SORT("sort");

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
    public static Flag findByKeyword(String word) {
        for (Flag flag : values()) {
            if (word.equalsIgnoreCase(flag.keyword)) {
                return flag;
            }
        }
        return null;
    }
}
