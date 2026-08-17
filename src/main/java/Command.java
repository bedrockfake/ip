/**
 * A single action the chatbot can perform in response to one line of input.
 * Commands are looked up by keyword and given the chatbot (for output and shared
 * state) plus the argument text — the part of the line after the keyword.
 */
public interface Command {
    /**
     * Runs this command.
     *
     * @param bot the chatbot, giving access to output ({@code say}) and the item list
     * @param argument the text after the command keyword; may be empty
     */
    void execute(Luke bot, String argument);

    /**
     * Whether the chatbot should stop reading input after this command runs.
     * Most commands don't end the program, so the default is {@code false}.
     */
    default boolean isExit() {
        return false;
    }
}
